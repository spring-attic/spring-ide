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
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
class BeanAspectDefinitionParserUtils {

	private static final String AJC_MAGIC = "ajc$";

	/**
	 * Class modelling an AspectJ annotation, exposing its type enumeration and pointcut String.
	 */
	protected static class AspectJAnnotation<A extends Annotation> {

		private static Map<Class<?>, AspectJAnnotationType> annotationTypes = null;

		static {
			annotationTypes = new HashMap<Class<?>, AspectJAnnotationType>();
			annotationTypes.put(Pointcut.class, AspectJAnnotationType.AtPointcut);
			annotationTypes.put(After.class, AspectJAnnotationType.AtAfter);
			annotationTypes.put(AfterReturning.class, AspectJAnnotationType.AtAfterReturning);
			annotationTypes.put(AfterThrowing.class, AspectJAnnotationType.AtAfterThrowing);
			annotationTypes.put(Around.class, AspectJAnnotationType.AtAround);
			annotationTypes.put(Before.class, AspectJAnnotationType.AtBefore);
		}

		private static final String[] EXPRESSION_PROPERTIES = new String[] { "value", "pointcut" };

		private final A annotation;

		private AspectJAnnotationType annotationType;

		private final String argNames;

		private final String expression;

		public AspectJAnnotation(A aspectjAnnotation) {
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

	protected static boolean isAspect(Class<?> clazz) {
		boolean couldBeAtAspectJAspect = AjTypeSystem.getAjType(clazz).isAspect();
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
	 */
	@SuppressWarnings("unchecked")
	protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method aMethod) {
		Class<? extends Annotation>[] classesToLookFor = (Class<? extends Annotation>[]) new Class[] {
				Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class,
				Pointcut.class };
		for (Class<? extends Annotation> c : classesToLookFor) {
			AspectJAnnotation foundAnnotation = findAnnotation(aMethod, c);
			if (foundAnnotation != null) {
				return foundAnnotation;
			}
		}
		return null;
	}

	protected static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method,
			Class<A> toLookFor) {
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
		AspectJAnnotation<?> aspectJAnnotation = findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			return null;
		}
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,
				new String[0], new Class[0]);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		return ajexp;
	}
	
	protected static boolean validate(Class<?> aspectClass) throws AopConfigException {
		// If the parent has the annotation and isn't abstract it's an error
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null
				&& !Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " cannot
			// extend concrete aspect " +
			// aspectClass.getSuperclass().getName());
		}

		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
		if (!ajType.isAspect()) {
			return false;
			// throw new NotAnAtAspectException(aspectClass);
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " uses
			// percflow instantiation model: " +
			// "This is not supported in Spring AOP");
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " uses
			// percflowbelow instantiation model: " +
			// "This is not supported in Spring AOP");
		}
		return true;
	}


}
