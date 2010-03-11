/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class that tries to locate matches of Spring AOP configurations given by {@link IAspectDefinition}.
 * <p>
 * Uses Spring AOP's {@link AspectJExpressionPointcut} infrastructure to determine matches.
 * <p>
 * With Spring 2.5 this class supports the bean pointcut primitive as well.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AspectDefinitionMatcher {

	/** Internal cache to used with {@link AspectJExpressionPointcut} */
	private Map<IAspectDefinition, Object> pointcutExpressionCache = new HashMap<IAspectDefinition, Object>();

	/**
	 * Returns all matches on {@link Method} in form of the corresponding {@link IMethod}.
	 * @param targetClass the target class to check for a match
	 * @param targetBean the target bean to check for a match
	 * @param info the {@link IAspectDefinition}
	 * @param project the current {@link IProject}
	 * @return the set of {@link IMethod} that match the given {@link IAspectDefinition}
	 * @throws Throwable any exception occurred during reflective invocation
	 */
	public Set<IMethod> matches(final Class<?> targetClass, final IBean targetBean, final IAspectDefinition info,
			final IProject project) throws Throwable {
		Set<IMethod> matches = new LinkedHashSet<IMethod>();

		// check aspect definition
		if (SpringCoreUtils.hasPlaceHolder(info.getPointcutExpression())) {
			return Collections.emptySet();
		}

		// expose bean name on thread local
		Class<?> proxyCreationContextClass = ClassUtils
				.loadClass("org.springframework.ide.eclipse.springframework.aop.framework.autoproxy.ProxyCreationContext");
		List<String> beanNames = new ArrayList<String>();
		beanNames.add(targetBean.getElementName());
		if (targetBean.getAliases() != null && targetBean.getAliases().length > 0) {
			beanNames.addAll(Arrays.asList(targetBean.getAliases()));
		}
		for (String beanName : beanNames) {
			ClassUtils.invokeMethod(proxyCreationContextClass, "setCurrentProxiedBeanName", new Object[] { beanName },
					new Class[] { String.class });
			try {
				matches.addAll(internalMatches(targetClass, targetBean, info, project));
			}
			finally {
				// reset bean name on thread local
				ClassUtils.invokeMethod(proxyCreationContextClass, "setCurrentProxiedBeanName", new Object[] { null },
						new Class[] { String.class });
			}
		}
		return matches;
	}

	public void close() {
		for (Object pce : pointcutExpressionCache.values()) {
			Field field = ReflectionUtils.findField(pce.getClass(), "shadowMatchCache");
			field.setAccessible(true);
			Map<?, ?> shadowMatchCache = (Map<?, ?>) ReflectionUtils.getField(field, pce);

			try {
				Class<?> resolvedTypeClass = pce.getClass().getClassLoader().loadClass(
						"org.aspectj.weaver.ResolvedType");
				Method resetPrimitivesMethod = resolvedTypeClass.getMethod("resetPrimitives");
				resetPrimitivesMethod.invoke(resolvedTypeClass);
			}
			catch (Exception e) {
			}
			shadowMatchCache.clear();
		}
		pointcutExpressionCache.clear();
	}

	/**
	 * Checks if the given matching candidate method is a legal match for Spring AOP.
	 * <p>
	 * Legal matches need to be public and either defined on the class and/or interface depending on the
	 * <code>isProxyTargetClass</code>.
	 */
	private boolean checkMethod(Class targetClass, Method targetMethod, boolean isProxyTargetClass) {
		Assert.notNull(targetClass);
		Assert.notNull(targetMethod);

		if (!Modifier.isPublic(targetMethod.getModifiers())) {
			return false;
		}
		else if (isProxyTargetClass) {
			return true;
		}
		else {
			Class[] targetInterfaces = org.springframework.util.ClassUtils.getAllInterfacesForClass(targetClass);
			// if target class does not implement any interface allow match
			if (targetInterfaces == null || targetInterfaces.length == 0) {
				return true;
			}
			for (Class targetInterface : targetInterfaces) {
				Method[] targetInterfaceMethods = targetInterface.getMethods();
				for (Method targetInterfaceMethod : targetInterfaceMethods) {
					Method targetMethodGuess = AopUtils.getMostSpecificMethod(targetInterfaceMethod, targetClass);
					if (targetMethod.equals(targetMethodGuess)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Checks if the given <code>targetClass</code> can be proxied in Spring AOP.
	 */
	private boolean checkClass(Class targetClass, boolean isProxyTargetClass) throws ClassNotFoundException {

		// check if bean is an infrastructure class
		if (isInfrastructureClass(targetClass)) {
			return false;
		}

		// Check if proxy-target-class=true is being used and CGLIB can't subclass the class
		if (Modifier.isFinal(targetClass.getModifiers()) && isProxyTargetClass) {
			return false;
		}

		// all fine; proceed with the given class
		return true;
	}

	/**
	 * Creates {@link AspectJExpressionPointcut} instances based on {@link IAspectDefinition}.
	 */
	private Object createAspectJPointcutExpression(IAspectDefinition info) throws Throwable {
		try {

			if (pointcutExpressionCache.containsKey(info)) {
				return pointcutExpressionCache.get(info);
			}

			Object pc = initAspectJExpressionPointcut(info);
			pointcutExpressionCache.put(info, pc);

			Class<?> aspectJAdviceClass = AspectJAdviceClassFactory.getAspectJAdviceClass(info);
			Class<?> aspectInstanceFactoryClass = ClassUtils
					.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.SimpleAspectInstanceFactory");
			if (aspectJAdviceClass != null && aspectInstanceFactoryClass != null) {
				Constructor<?> ctor = aspectInstanceFactoryClass.getConstructors()[0];
				Object aspectInstanceFactory = ctor.newInstance(aspectJAdviceClass);

				ctor = aspectJAdviceClass.getConstructors()[0];
				Object aspectJAdvice = ctor.newInstance(new Object[] { info.getAdviceMethod(), pc,
						aspectInstanceFactory });

				if (info.getType() == ADVICE_TYPE.AFTER_RETURNING) {
					if (info.getReturning() != null) {
						ClassUtils.invokeMethod(aspectJAdvice, "setReturningName", info.getReturning());
					}
				}
				else if (info.getType() == ADVICE_TYPE.AFTER_THROWING) {
					if (info.getThrowing() != null) {
						ClassUtils.invokeMethod(aspectJAdvice, "setThrowingName", info.getThrowing());
					}
				}
				if (info.getArgNames() != null && info.getArgNames().length > 0) {
					ClassUtils.invokeMethod(aspectJAdvice, "setArgumentNamesFromStringArray", new Object[] { info
							.getArgNames() });
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

	private Object initAspectJExpressionPointcut(IAspectDefinition info) throws Throwable {

		Class<?> expressionPointcutClass = ClassUtils
				.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJExpressionPointcut");
		Object pc = expressionPointcutClass.newInstance();
		for (Method m : expressionPointcutClass.getMethods()) {
			if (m.getName().equals("setExpression")) {
				m.invoke(pc, info.getPointcutExpression());
			}
		}
		// don't set the declaration scope if no aspect class is yet given
		if (info.getAspectClassName() != null) {
			ClassUtils.invokeMethod(pc, "setPointcutDeclarationScope", ClassUtils.loadClass(info.getAspectClassName()));
		}
		return pc;
	}

	private Set<IMethod> internalMatches(final Class<?> targetClass, final IBean targetBean,
			final IAspectDefinition info, final IProject project) throws Throwable {

		// check if bean class can be processed
		if (!checkClass(targetClass, info.isProxyTargetClass())) {
			return Collections.emptySet();
		}

		// check if bean is synthetic as this would mean that the BeanPostProcessor would not load
		BeanDefinition beanDefinition = BeansModelUtils.getMergedBeanDefinition(targetBean, null);
		if (beanDefinition instanceof RootBeanDefinition && ((RootBeanDefinition) beanDefinition).isSynthetic()) {
			return Collections.emptySet();
		}

		// check if pointcut expression has been set
		if (info.getPointcutExpression() == null) {
			return Collections.emptySet();
		}

		final Set<IMethod> matchingMethods = new HashSet<IMethod>();
		final Object aspectJExpressionPointcut = createAspectJPointcutExpression(info);

		if (!((Boolean) ClassUtils.invokeMethod(aspectJExpressionPointcut, "matches", targetClass))) {
			return matchingMethods;
		}

		final IType jdtTargetType = JdtUtils.getJavaType(project, targetClass.getName());

		// TODO CD here is room for speed improvements by collecting all valid methods in one go and then ask for
		// matches
		ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

				if (checkMethod(targetClass, method, info.isProxyTargetClass()) && !matchingMethods.contains(method)) {
					try {
						boolean matches = (Boolean) ClassUtils.invokeMethod(aspectJExpressionPointcut, "matches",
								method, targetClass);
						if (matches) {
							addMatchingJdtMethod(matchingMethods, jdtTargetType, method);
						}
						// If in proxy interface mode we can match on methods from the interface rather then the actual
						// class
						else if (!info.isProxyTargetClass()) {
							Class[] targetInterfaces = org.springframework.util.ClassUtils
									.getAllInterfacesForClass(targetClass);

							if (targetInterfaces != null) {
								for (Class targetInterface : targetInterfaces) {
									Method[] targetInterfaceMethods = targetInterface.getMethods();
									for (Method targetInterfaceMethod : targetInterfaceMethods) {
										Method targetMethodGuess = AopUtils.getMostSpecificMethod(
												targetInterfaceMethod, targetClass);
										if (method.equals(targetMethodGuess)) {
											matches = (Boolean) ClassUtils.invokeMethod(aspectJExpressionPointcut,
													"matches", targetInterfaceMethod, targetInterface);
											if (matches) {
												addMatchingJdtMethod(matchingMethods, jdtTargetType, method);
											}
										}
									}
								}
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

			private void addMatchingJdtMethod(final Set<IMethod> matchingMethods, final IType jdtTargetType,
					Method method) {
				IMethod jdtMethod = JdtUtils.getMethod(jdtTargetType, method.getName(), method.getParameterTypes());
				if (jdtMethod != null) {
					matchingMethods.add(jdtMethod);
				}
			}
		});
		return matchingMethods;
	}

	private boolean isInfrastructureClass(Class<?> beanClass) throws ClassNotFoundException {
		Class<?> advisorClass = ClassUtils.loadClass(Advisor.class);
		Class<?> adviceClass = ClassUtils.loadClass(Advice.class);
		Class<?> aopInfrastructureBeanClass = ClassUtils.loadClass(AopInfrastructureBean.class);
		return advisorClass.isAssignableFrom(beanClass) || adviceClass.isAssignableFrom(beanClass)
				|| aopInfrastructureBeanClass.isAssignableFrom(beanClass);
	}

}