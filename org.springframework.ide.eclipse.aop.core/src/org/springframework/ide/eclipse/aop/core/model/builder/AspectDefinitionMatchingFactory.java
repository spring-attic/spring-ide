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
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.support.AopUtils;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;

/**
 * Helper class for {@link AopReferenceModelBuilder} and
 * {@link AspectDefinitionBuilder}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AspectDefinitionMatchingFactory {

	private Map<IAspectDefinition, Object> aspectDefinitionToPoincutCache = 
		new ConcurrentHashMap<IAspectDefinition, Object>();;

	private boolean checkMethod(Class targetClass, Method targetMethod,
			boolean allMethods) {
		Assert.notNull(targetMethod);
		Assert.notNull(targetClass);

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

	private Object createAspectJPointcutExpression(IAspectDefinition info)
			throws Throwable {
		try {
			Object pc = initAspectJExpressionPointcut(info);
			Class<?> aspectJAdviceClass = getAspectJAdviceClass(info);
			Constructor<?> ctor = aspectJAdviceClass.getConstructors()[0];
			Object aspectJAdvice = ctor.newInstance(new Object[] {
					info.getAdviceMethod(), pc, null });
			if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
				if (info.getReturning() != null) {
					Method setReturningNameMethod = aspectJAdviceClass
							.getMethod("setReturningName", String.class);
					setReturningNameMethod.invoke(aspectJAdvice, info
							.getReturning());
				}
			}
			else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
				if (info.getThrowing() != null) {
					Method setThrowingNameMethod = aspectJAdviceClass
							.getMethod("setThrowingName", String.class);
					setThrowingNameMethod.invoke(aspectJAdvice, info
							.getThrowing());
				}
			}

			if (info.getArgNames() != null && info.getArgNames().length > 0) {
				Method setArgumentNamesFromStringArrayMethod = aspectJAdviceClass
						.getMethod("setArgumentNamesFromStringArray",
								String[].class);
				setArgumentNamesFromStringArrayMethod.invoke(aspectJAdvice,
						new Object[] { info.getArgNames() });
			}

			Method getPointuctMethod = aspectJAdviceClass.getMethod(
					"getPointcut", (Class[]) null);
			return getPointuctMethod.invoke(aspectJAdvice, (Object[]) null);
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private Class<?> getAspectJAdviceClass(IAspectDefinition info)
			throws ClassNotFoundException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> aspectJAdviceClass = null;
		if (info.getType() == ADVICE_TYPES.AROUND) {
			aspectJAdviceClass = loader.loadClass(AspectJAroundAdvice.class
					.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER) {
			aspectJAdviceClass = loader.loadClass(AspectJAfterAdvice.class
					.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
			aspectJAdviceClass = loader
					.loadClass(AspectJAfterReturningAdvice.class.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
			aspectJAdviceClass = loader
					.loadClass(AspectJAfterThrowingAdvice.class.getName());
		}
		else if (info.getType() == ADVICE_TYPES.BEFORE) {
			aspectJAdviceClass = loader
					.loadClass(AspectJMethodBeforeAdvice.class.getName());
		}
		return aspectJAdviceClass;
	}

	public List<IMethod> getMatchesForBeanAspectDefinition(Class<?> targetClass,
			IAspectDefinition info, IProject project)
			throws Throwable {

		Object aspectJExpressionPointcut = null;
		if (aspectDefinitionToPoincutCache.containsKey(info)) {
			aspectJExpressionPointcut = aspectDefinitionToPoincutCache.get(info);
		}
		else {
			aspectJExpressionPointcut = createAspectJPointcutExpression(info);
			aspectDefinitionToPoincutCache.put(info, aspectJExpressionPointcut);
		}

		IType jdtTargetType = BeansModelUtils.getJavaType(project, targetClass
				.getName());

		Method matchesMethod = aspectJExpressionPointcut.getClass().getMethod(
				"matches", Method.class, Class.class);
		List<IMethod> matchingMethod = new ArrayList<IMethod>();

		for (Method m : targetClass.getDeclaredMethods()) {
			if (checkMethod(targetClass, m, info.isProxyTargetClass())) {
				boolean matches = (Boolean) matchesMethod.invoke(
						aspectJExpressionPointcut, m, targetClass);
				if (matches) {
					IMethod jdtMethod = JdtUtils.getMethod(jdtTargetType, m
							.getName(), m.getParameterTypes());
					if (jdtMethod != null) {
						matchingMethod.add(jdtMethod);
					}
				}
			}
		}
		return matchingMethod;
	}

	public List<IMethod> getMatchesForAnnotationAspectDefinition(
			Class<?> targetClass, IAspectDefinition info, IProject project)
			throws Throwable {

		Object aspectJExpressionPointcut = null;
		if (aspectDefinitionToPoincutCache.containsKey(info)) {
			aspectJExpressionPointcut = aspectDefinitionToPoincutCache.get(info);
		}
		else {
			aspectJExpressionPointcut = initAspectJExpressionPointcut(info);
			aspectDefinitionToPoincutCache.put(info, aspectJExpressionPointcut);
		}

		Method getMethodMatcherMethod = aspectJExpressionPointcut.getClass()
				.getMethod("getMethodMatcher", (Class[]) null);
		Object methodMatcher = getMethodMatcherMethod.invoke(
				aspectJExpressionPointcut, (Object[]) null);
		Method matchesMethod = methodMatcher.getClass().getMethod("matches",
				Method.class, Class.class);

		IType jdtTargetClass = BeansModelUtils.getJavaType(project, targetClass
				.getName());
		Method[] methods = targetClass.getDeclaredMethods();
		List<IMethod> matchingMethod = new ArrayList<IMethod>();
		for (Method method : methods) {
			if (checkMethod(targetClass, method, info.isProxyTargetClass())
					&& (Boolean) matchesMethod.invoke(methodMatcher, method,
							targetClass)) {
				IMethod jdtMethod = JdtUtils.getMethod(jdtTargetClass, method
						.getName(), method.getParameterTypes());
				if (jdtMethod != null) {
					matchingMethod.add(jdtMethod);
				}
			}
		}
		return matchingMethod;
	}

	private Object initAspectJExpressionPointcut(IAspectDefinition info)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			NoSuchMethodException {
		Class<?> expressionPointcutClass = ClassUtils
				.loadClass(AspectJExpressionPointcut.class.getName());
		Object pc = expressionPointcutClass.newInstance();
		for (Method m : expressionPointcutClass.getMethods()) {
			if (m.getName().equals("setExpression")) {
				m.invoke(pc, info.getPointcutExpression());
			}
		}
		Method setDeclarationScopeMethod = expressionPointcutClass.getMethod(
				"setPointcutDeclarationScope", Class.class);
		setDeclarationScopeMethod.invoke(pc, ClassUtils.loadClass(info
				.getAspectClassName()));
		return pc;
	}
}
