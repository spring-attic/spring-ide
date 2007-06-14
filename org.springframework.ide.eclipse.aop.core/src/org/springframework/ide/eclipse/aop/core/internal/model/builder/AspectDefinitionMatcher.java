/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.ProxyCreationContext;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Utility class that tries to locate matches of Spring AOP configurations given
 * by {@link IAspectDefinition}.
 * <p>
 * Uses Spring AOP's {@link AspectJExpressionPointcut} infrastructure to
 * determine matches.
 * <p>
 * With Spring 2.1 this class is already ready to support the bean pointcut
 * primitive as well.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AspectDefinitionMatcher {

	/**
	 * Utility helper class that exposes the current beanName to the
	 * {@link ProxyCreationContext} introduced by Spring 2.1
	 * @throws ClassNotFoundException
	 */
	private static class BeanNameExposingReflectionUtils {
		public static void doWithMethods(String targetBeanName,
				Class targetClass, MethodCallback mc) throws Throwable { 
			// expose bean name on thread local
			Object proxyCreationContext = ClassUtils.loadClass(
					ProxyCreationContext.class).newInstance();
			ClassUtils.invokeMethod(proxyCreationContext,
					"notifyProxyCreationStart", targetBeanName);
			try {
				ReflectionUtils.doWithMethods(targetClass, mc);
			}
			finally {
				ClassUtils.invokeMethod(proxyCreationContext,
						"notifyProxyCreationComplete");
			}
		}
	}

	private boolean isInfrastructureClass(Class<?> beanClass)
			throws ClassNotFoundException {
		Class<?> advisorClass = ClassUtils.loadClass(Advisor.class);
		Class<?> adviceClass = ClassUtils.loadClass(Advice.class);
		Class<?> aopInfrastructureBean = ClassUtils
				.loadClass(AopInfrastructureBean.class);
		return advisorClass.isAssignableFrom(beanClass)
				|| adviceClass.isAssignableFrom(beanClass)
				|| aopInfrastructureBean.isAssignableFrom(beanClass);
	}

	/**
	 * Checks if the given matching candidate method is a legal match for Spring
	 * AOP.
	 * <p>
	 * Legal matches need to be public and either defined on the class and/or
	 * interface depending on the <code>allMethods</code>.
	 */
	private boolean checkMethod(Class targetClass, Method targetMethod,
			boolean allMethods) {
		Assert.notNull(targetClass);
		Assert.notNull(targetMethod);

		if (!Modifier.isPublic(targetMethod.getModifiers())) {
			return false;
		}
		else if (allMethods) {
			return true;
		}
		else {
			Class[] targetInterfaces = org.springframework.util.ClassUtils
					.getAllInterfacesForClass(targetClass);
			for (Class targetInterface : targetInterfaces) {
				Method[] targetInterfaceMethods = targetInterface.getMethods();
				for (Method targetInterfaceMethod : targetInterfaceMethods) {
					Method targetMethodGuess = AopUtils.getMostSpecificMethod(
							targetInterfaceMethod, targetClass);
					if (targetMethod.equals(targetMethodGuess)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Creates {@link AspectJExpressionPointcut} instances based on
	 * {@link IAspectDefinition}.
	 */
	private Object createAspectJPointcutExpression(IAspectDefinition info)
			throws Throwable {
		try {
			Object pc = initAspectJExpressionPointcut(info);
			Class<?> aspectJAdviceClass = AspectJAdviceClassFactory
					.getAspectJAdviceClass(info);
			if (aspectJAdviceClass != null) {
				Constructor<?> ctor = aspectJAdviceClass.getConstructors()[0];
				Object aspectJAdvice = ctor.newInstance(new Object[] {
						info.getAdviceMethod(), pc, null });
				if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
					if (info.getReturning() != null) {
						ClassUtils.invokeMethod(aspectJAdvice,
								"setReturningName", info.getReturning());
					}
				}
				else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
					if (info.getThrowing() != null) {
						ClassUtils.invokeMethod(aspectJAdvice,
								"setThrowingName", info.getThrowing());
					}
				}
				if (info.getArgNames() != null && info.getArgNames().length > 0) {
					ClassUtils.invokeMethod(aspectJAdvice,
							"setArgumentNamesFromStringArray",
							new Object[] { info.getArgNames() });
				}
				return ClassUtils.invokeMethod(aspectJAdvice, "getPointcut");
			}
			else {
				return pc;
			}
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public List<IMethod> matches(final Class<?> targetClass,
			final String targetBeanName, final IAspectDefinition info,
			final IProject project) throws Throwable {
		final List<IMethod> matchingMethod = new ArrayList<IMethod>();

		// check if bean is an infrastructure class
		if (isInfrastructureClass(targetClass)) {
			return matchingMethod;
		}

		final Object aspectJExpressionPointcut = createAspectJPointcutExpression(info);
		final IType jdtTargetType = JdtUtils.getJavaType(project, targetClass
				.getName());

		BeanNameExposingReflectionUtils.doWithMethods(targetBeanName, targetClass,
				new ReflectionUtils.MethodCallback() {
					public void doWith(Method method)
							throws IllegalArgumentException,
							IllegalAccessException {

						if (checkMethod(targetClass, method, info
								.isProxyTargetClass())) {
							try {
								boolean matches = (Boolean) ClassUtils
										.invokeMethod(
												aspectJExpressionPointcut,
												"matches", method, targetClass);
								if (matches) {
									IMethod jdtMethod = JdtUtils.getMethod(
											jdtTargetType, method.getName(),
											method.getParameterTypes());
									if (jdtMethod != null) {
										matchingMethod.add(jdtMethod);
									}
								}
							}
							catch (Throwable e) {
								if (e instanceof IllegalArgumentException) {
									throw (IllegalArgumentException) e;
								}
								else if (e instanceof IllegalAccessException) {
									throw (IllegalAccessException) e;
								}
								else {
									// get the original exception out
									throw new RuntimeException(e);
								}
							}
						}
					}
				});

		return matchingMethod;
	}

	private Object initAspectJExpressionPointcut(IAspectDefinition info)
			throws Throwable {

		Class<?> expressionPointcutClass = ClassUtils
				.loadClass(AspectJExpressionPointcut.class);
		Object pc = expressionPointcutClass.newInstance();
		for (Method m : expressionPointcutClass.getMethods()) {
			if (m.getName().equals("setExpression")) {
				m.invoke(pc, info.getPointcutExpression());
			}
		}
		ClassUtils.invokeMethod(pc, "setPointcutDeclarationScope", ClassUtils
				.loadClass(info.getAspectClassName()));
		return pc;
	}
}
