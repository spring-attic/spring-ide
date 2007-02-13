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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.aop.core.parser.asm.AspectAnnotationVisitor;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
public class BeansAopModelUtils {

    private static final String AJC_MAGIC = "ajc$";

    /** The ".class" file suffix */
    private static final String CLASS_FILE_SUFFIX = ".class";

    public static boolean validateAspect(String className) throws Throwable {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(BeansAopModelUtils
                .getClassFileName(className));

        // check if class exists on class path
        if (is == null) {
            return false;
        }

        ClassReader reader = new ClassReader(is);
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
            else if (m.getName().equals("setParameterNames")
                    && !(info instanceof JavaAspectDefinition)) {
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
