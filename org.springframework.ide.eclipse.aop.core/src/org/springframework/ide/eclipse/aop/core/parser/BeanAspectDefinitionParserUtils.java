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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
class BeanAspectDefinitionParserUtils {

	private static final String AJC_MAGIC = "ajc$";

	/**
	 * Class modelling an AspectJ annotation, exposing its type enumeration and pointcut String.
	 */
	protected static class AspectJAnnotation<A extends Annotation> {

		private Map<Class<?>, AspectJAnnotationType> annotationTypes = null;

		private void init() throws ClassNotFoundException  {
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
			} catch (Exception ex) {
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
				} catch (NoSuchMethodException ex) {
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

	protected static boolean isAspect(Class<?> clazz) throws Throwable {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<?> aspectTypeSystemClass = classLoader.loadClass(AjTypeSystem.class.getName());
		Method getAjTypeMethod = aspectTypeSystemClass.getMethod("getAjType", Class.class);
		Object aspectJTypeSystem = getAjTypeMethod.invoke(null, clazz);

		Method isAspectMethod = aspectJTypeSystem.getClass().getMethod("isAspect", (Class[]) null);
		boolean couldBeAtAspectJAspect = (Boolean) isAspectMethod.invoke(aspectJTypeSystem, (Object[]) null);
		if (!couldBeAtAspectJAspect) {
			return false;
		} else {
			// we know it's an aspect, but we don't know whether it is an
			// @AspectJ aspect or a code style aspect.
			// This is an *unclean* test whilst waiting for AspectJ to provide
			// us with something better
			Method[] methods = clazz.getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().startsWith(AJC_MAGIC)) {
					// must be a code style aspect
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
		} catch (ClassNotFoundException e) {
			Activator.log(e);
		}
		return null;
	}

	protected static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method,
			Class<A> toLookFor) throws ClassNotFoundException {
		A result = AnnotationUtils.findAnnotation(method, toLookFor);
		if (result != null) {
			return new AspectJAnnotation<A>(result);
		} else {
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

	@SuppressWarnings("unchecked")
	protected static boolean validate(Class<?> aspectClass) throws Throwable {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class annotationClass = classLoader.loadClass(Aspect.class.getName());
		if (aspectClass.getSuperclass().getAnnotation(annotationClass) != null
				&& !Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			return false;
		}
		Class aspectTypeSystemClass = classLoader.loadClass(AjTypeSystem.class.getName());
		Method getAjTypeMethod = aspectTypeSystemClass.getMethod("getAjType", Class.class);
		Object aspectJType = getAjTypeMethod.invoke(null, aspectClass);

		Method perClauseMethod = aspectJType.getClass().getMethod("getPerClause", (Class[]) null);
		Object perClauseType = perClauseMethod.invoke(aspectJType, (Object[]) null);
		Method getKindMethod = perClauseType.getClass().getMethod("getKind", (Class[]) null);
		Object getKind = getKindMethod.invoke(perClauseType, (Object[]) null);

		if (getKind.toString().equals(PerClauseKind.PERCFLOW.toString())) {
			return false;
		}
		if (getKind.toString().equals(PerClauseKind.PERCFLOWBELOW.toString())) {
			return false;
		}
		return true;
	}

}
