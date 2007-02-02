/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.aop.core.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.PerClauseKind;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.parser.asm.AspectAnnotationVisitor;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
public class BeansAopModelUtils {

    private static final String AJC_MAGIC = "ajc$";

    /** The ".class" file suffix */
    private static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Class modelling an AspectJ annotation, exposing its type enumeration and pointcut String.
     */
    protected static class AspectJAnnotation<A extends Annotation> {

        private Map<Class<?>, AspectJAnnotationType> annotationTypes = null;

        private void init() throws ClassNotFoundException {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            annotationTypes = new HashMap<Class<?>, AspectJAnnotationType>();
            annotationTypes.put(cl.loadClass(Pointcut.class.getName()),
                    AspectJAnnotationType.AtPointcut);
            annotationTypes.put(cl.loadClass(After.class.getName()), AspectJAnnotationType.AtAfter);
            annotationTypes.put(cl.loadClass(AfterReturning.class.getName()),
                    AspectJAnnotationType.AtAfterReturning);
            annotationTypes.put(cl.loadClass(AfterThrowing.class.getName()),
                    AspectJAnnotationType.AtAfterThrowing);
            annotationTypes.put(cl.loadClass(Around.class.getName()),
                    AspectJAnnotationType.AtAround);
            annotationTypes.put(cl.loadClass(Before.class.getName()),
                    AspectJAnnotationType.AtBefore);
        }

        private static final String[] EXPRESSION_PROPERTIES = new String[] { "value", "pointcut" };

        private final A annotation;

        private AspectJAnnotationType annotationType;

        private final String argNames;

        private final String expression;

        public AspectJAnnotation(A aspectjAnnotation) throws ClassNotFoundException {
            init();
            this.annotation = aspectjAnnotation;
            for (Class<?> c : annotationTypes.keySet()) {
                if (c.isInstance(this.annotation)) {
                    this.annotationType = annotationTypes.get(c);
                    break;
                }
            }
            if (this.annotationType == null) {
                throw new IllegalStateException("unknown annotation type: "
                        + this.annotation.toString());
            }

            // We know these methods exist with the same name on each object,
            // but need to invoke them reflectively as there isn't a common
            // interfaces
            try {
                this.expression = resolveExpression();
                this.argNames = (String) annotation.getClass()
                        .getMethod("argNames", (Class[]) null).invoke(this.annotation);
            }
            catch (Exception ex) {
                throw new IllegalArgumentException(aspectjAnnotation
                        + " cannot be an AspectJ annotation", ex);
            }
        }

        public A getAnnotation() {
            return this.annotation;
        }

        public AspectJAnnotationType getAnnotationType() {
            return this.annotationType;
        }

        public String getArgNames() {
            return this.argNames;
        }

        public String getPointcutExpression() {
            return this.expression;
        }

        private String resolveExpression() throws IllegalAccessException,
                InvocationTargetException, NoSuchMethodException {
            String expression = null;
            for (int i = 0; i < EXPRESSION_PROPERTIES.length; i++) {
                String methodName = EXPRESSION_PROPERTIES[i];
                Method method;
                try {
                    method = annotation.getClass().getDeclaredMethod(methodName);
                }
                catch (NoSuchMethodException ex) {
                    method = null;
                }

                if (method != null) {
                    String candidate = (String) method.invoke(this.annotation);

                    if (StringUtils.hasText(candidate)) {
                        expression = candidate;
                    }
                }
            }
            return expression;
        }

        public String toString() {
            return this.annotation.toString();
        }
    }

    public enum AspectJAnnotationType {
        AtAfter, AtAfterReturning, AtAfterThrowing, AtAround, AtBefore, AtPointcut
    }

    protected static boolean validateAspect(String className) throws Throwable {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassReader reader = new ClassReader(classLoader.getResourceAsStream(BeansAopModelUtils
                .getClassFileName(className)));
        AspectAnnotationVisitor v = new AspectAnnotationVisitor();
        reader.accept(v, false);

        if (!v.getClassInfo().hasAspectAnnotation()) {
            return false;
        }
        else {
            // we know it's an aspect, but we don't know whether it is an
            // @AspectJ aspect or a code style aspect.
            // This is an *unclean* test whilst waiting for AspectJ to provide
            // us with something better
            for (String m : v.getClassInfo().getMethodNames()) {
                if (m.startsWith(AJC_MAGIC)) {
                    // must be a code style aspect
                    return false;
                }
            }
            // validate supported instantiation models
            if (v.getClassInfo().getAspectAnnotation().getValue() != null) {
                if (v.getClassInfo().getAspectAnnotation().getValue().toUpperCase().equals(
                        PerClauseKind.PERCFLOW.toString())) {
                    return false;
                }
                if (v.getClassInfo().getAspectAnnotation().getValue().toUpperCase().toString()
                        .equals(PerClauseKind.PERCFLOWBELOW.toString())) {
                    return false;
                }
            }

            // check if super class is Aspect as well and abstract
            if (v.getClassInfo().getSuperType() != null) {
                reader = new ClassReader(classLoader.getResourceAsStream(BeansAopModelUtils
                        .getClassFileName(v.getClassInfo().getSuperType())));
                AspectAnnotationVisitor sv = new AspectAnnotationVisitor();
                reader.accept(sv, false);

                if (sv.getClassInfo().getAspectAnnotation() != null
                        && !((sv.getClassInfo().getModifier() & Opcodes.ACC_ABSTRACT) != 0)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Find and return the first AspectJ annotation on the given method (there <i>should</i> only
     * be one anyway...)
     * 
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method aMethod) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<? extends Annotation>[] classesToLookFor;
        try {
            classesToLookFor = (Class<? extends Annotation>[]) new Class[] {
                    classLoader.loadClass(Before.class.getName()),
                    classLoader.loadClass(Around.class.getName()),
                    classLoader.loadClass(After.class.getName()),
                    classLoader.loadClass(AfterReturning.class.getName()),
                    classLoader.loadClass(AfterThrowing.class.getName()),
                    classLoader.loadClass(Pointcut.class.getName()) };
            for (Class<? extends Annotation> c : classesToLookFor) {
                AspectJAnnotation foundAnnotation = findAnnotation(aMethod, c);
                if (foundAnnotation != null) {
                    return foundAnnotation;
                }
            }
        }
        catch (ClassNotFoundException e) {
            Activator.log(e);
        }
        return null;
    }

    protected static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method,
            Class<A> toLookFor) throws ClassNotFoundException {
        A result = AnnotationUtils.findAnnotation(method, toLookFor);
        if (result != null) {
            return new AspectJAnnotation<A>(result);
        }
        else {
            return null;
        }
    }

    protected static AspectJExpressionPointcut getPointcut(Class<?> candidateAspectClass,
            String pointcut) {
        AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,
                new String[0], new Class[0]);
        ajexp.setExpression(pointcut);
        return ajexp;
    }

    protected static AspectJExpressionPointcut getPointcut(Method candidateAspectJAdviceMethod,
            Class<?> candidateAspectClass) {
        AspectJAnnotation<?> aspectJAnnotation;
        aspectJAnnotation = findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }
        AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,
                new String[0], new Class[0]);
        ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
        return ajexp;
    }

    public static Object initAspectJExpressionPointcut(IAspectDefinition info)
            throws InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException, NoSuchMethodException {
        IType jdtAspectType = BeansModelUtils.getJavaType(info.getResource().getProject(), info
                .getAspectClassName());
        Class<?> expressionPointcutClass = loadClass(AspectJExpressionPointcut.class.getName());
        Object pc = expressionPointcutClass.newInstance();
        for (Method m : expressionPointcutClass.getMethods()) {
            if (m.getName().equals("setExpression")) {
                m.invoke(pc, info.getPointcutExpression());
            }
            else if (m.getName().equals("setParameterNames")) {
                m.invoke(pc, new Object[] { new JdtParameterNameDiscoverer(jdtAspectType)
                        .getParameterNames(info.getAdviceMethod()) });
            }
        }
        Method setDeclarationScopeMethod = expressionPointcutClass.getMethod(
                "setPointcutDeclarationScope", Class.class);
        setDeclarationScopeMethod.invoke(pc, loadClass(info.getAspectClassName()));
        return pc;
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.loadClass(className);
    }

    public static Class<?> getAspectJAdviceClass(IAspectDefinition info)
            throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> aspectJAdviceClass = null;
        if (info.getType() == ADVICE_TYPES.AROUND) {
            aspectJAdviceClass = loader.loadClass(AspectJAroundAdvice.class.getName());
        }
        else if (info.getType() == ADVICE_TYPES.AFTER) {
            aspectJAdviceClass = loader.loadClass(AspectJAfterAdvice.class.getName());
        }
        else if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
            aspectJAdviceClass = loader.loadClass(AspectJAfterReturningAdvice.class.getName());
        }
        else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
            aspectJAdviceClass = loader.loadClass(AspectJAfterThrowingAdvice.class.getName());
        }
        else if (info.getType() == ADVICE_TYPES.BEFORE) {
            aspectJAdviceClass = loader.loadClass(AspectJMethodBeforeAdvice.class.getName());
        }
        return aspectJAdviceClass;
    }

    public static Object createAspectJAdvice(IAspectDefinition info, Class<?> aspectJAdviceClass,
            Object pc) throws Throwable {
        try {
            Constructor<?> ctor = aspectJAdviceClass.getConstructors()[0];
            Method afterPropertiesSetMethod = aspectJAdviceClass.getMethod("afterPropertiesSet",
                    (Class[]) null);
            Object aspectJAdvice = ctor
                    .newInstance(new Object[] { info.getAdviceMethod(), pc, null });
            if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
                if (info.getReturning() != null) {
                    Method setReturningNameMethod = aspectJAdviceClass.getMethod(
                            "setReturningName", String.class);
                    setReturningNameMethod.invoke(aspectJAdvice, info.getReturning());
                }
            }
            else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
                if (info.getThrowing() != null) {
                    Method setThrowingNameMethod = aspectJAdviceClass.getMethod("setThrowingName",
                            String.class);
                    setThrowingNameMethod.invoke(aspectJAdvice, info.getThrowing());
                }
            }

            if (info.getArgNames() != null && info.getArgNames().length > 0) {
                Method setArgumentNamesFromStringArrayMethod = aspectJAdviceClass.getMethod(
                        "setArgumentNamesFromStringArray", String[].class);
                setArgumentNamesFromStringArrayMethod.invoke(aspectJAdvice, new Object[] { info
                        .getArgNames() });
            }

            afterPropertiesSetMethod.invoke(aspectJAdvice, (Object[]) null);
            return aspectJAdvice;
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public static String methodToSignatureString(Method method) {
        StringBuffer buf = new StringBuffer(method.getName());
        if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            buf.append("(");
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> cls = method.getParameterTypes()[i];
                buf.append(cls.getName());
                if (i < (method.getParameterTypes().length - 1)) {
                    buf.append(", ");
                }
            }
            buf.append(")");
        }
        return buf.toString();
    }

    static class JdtParameterNameDiscoverer implements ParameterNameDiscoverer {

        private IType type;

        public JdtParameterNameDiscoverer(IType type) {
            this.type = type;
        }

        public String[] getParameterNames(Method method) {
            if (method != null) {
                String methodName = method.getName();
                int argCount = method.getParameterTypes().length;
                IMethod jdtMethod;
                try {
                    jdtMethod = BeansAopUtils.getMethod(type, methodName, argCount);
                    if (jdtMethod != null) {
                        return jdtMethod.getParameterNames();
                    }
                }
                catch (JavaModelException e) {
                    // suppress this
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public String[] getParameterNames(Constructor ctor) {
            return null;
        }
    }

    /**
     * Determine the name of the class file, relative to the containing package: e.g. "String.class"
     * @param clazz the class
     * @return the file name of the ".class" file
     */
    public static String getClassFileName(String className) {
        className = StringUtils.replace(className, ".", "/");
        return className + CLASS_FILE_SUFFIX;
    }
}
