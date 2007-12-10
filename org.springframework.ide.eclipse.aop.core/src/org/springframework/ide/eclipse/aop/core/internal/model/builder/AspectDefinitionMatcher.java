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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.autoproxy.ProxyCreationContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class that tries to locate matches of Spring AOP configurations given
 * by {@link IAspectDefinition}.
 * <p>
 * Uses Spring AOP's {@link AspectJExpressionPointcut} infrastructure to
 * determine matches.
 * <p>
 * With Spring 2.1 this class supports the bean pointcut primitive as well.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AspectDefinitionMatcher {
	
	private Map<IAspectDefinition, Object> pointcutExpressionCache = 
		new HashMap<IAspectDefinition, Object>();
	
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
			// if target class does not implement any interface allow match
			if (targetInterfaces == null || targetInterfaces.length == 0) {
				return true;
			}
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
			
			if (pointcutExpressionCache.containsKey(info)) {
				return pointcutExpressionCache.get(info);
			}
			
			Object pc = initAspectJExpressionPointcut(info);
			pointcutExpressionCache.put(info, pc);
			
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
		// don't set the declaration scope if no aspect class is yet given
		if (info.getAspectClassName() != null) { 
			ClassUtils.invokeMethod(pc, "setPointcutDeclarationScope", ClassUtils
					.loadClass(info.getAspectClassName()));
		}
		return pc;
	}

	private Set<IMethod> internalMatches(final Class<?> targetClass,
			final IBean targetBean, final IAspectDefinition info,
			final IProject project) throws Throwable {
		final Set<IMethod> matchingMethods = new HashSet<IMethod>();

		// check if bean is an infrastructure class
		if (isInfrastructureClass(targetClass)) {
			return matchingMethods;
		}

		final Object aspectJExpressionPointcut = createAspectJPointcutExpression(info);

		if (!((Boolean) ClassUtils.invokeMethod(aspectJExpressionPointcut, 
				"matches", targetClass))) {
			return matchingMethods;
		}
		
		final IType jdtTargetType = JdtUtils.getJavaType(project, targetClass
				.getName());

		ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
					public void doWith(Method method)
							throws IllegalArgumentException,
							IllegalAccessException {

						if (checkMethod(targetClass, method, info
								.isProxyTargetClass())
								&& !matchingMethods.contains(method)) {
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
										matchingMethods.add(jdtMethod);
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
		return matchingMethods;
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
			
	public Set<IMethod> matches(final Class<?> targetClass,
			final IBean targetBean, final IAspectDefinition info,
			final IProject project) throws Throwable {
		
		// check aspect definition
		if (SpringCoreUtils.hasPlaceHolder(info.getPointcutExpression())) {
			return Collections.emptySet();
		}
		
		// expose bean name on thread local
		Class<?> proxyCreationContextClass = ClassUtils
			.loadClass(ProxyCreationContext.class);
		ClassUtils.invokeMethod(proxyCreationContextClass,
				"setCurrentProxiedBeanName", new Object[] { targetBean
				.getElementName() }, new Class[] { String.class });
		try {
			return internalMatches(targetClass, targetBean, info, project);
		}
		finally {
			// reset bean name on thread local
			ClassUtils.invokeMethod(proxyCreationContextClass,
					"setCurrentProxiedBeanName", new Object[] { null },
					new Class[] { String.class });
		}
	}
}