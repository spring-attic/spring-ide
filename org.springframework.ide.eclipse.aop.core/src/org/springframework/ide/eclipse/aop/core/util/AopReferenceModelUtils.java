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
package org.springframework.ide.eclipse.aop.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.aop.support.AopUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilderUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Public;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Static;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Some helper methods.
 * @author Christian Dupuis
 */
public class AopReferenceModelUtils {

	public static String getJavaElementLinkName(IJavaElement je) {
		if (je == null) {
			return "";
		}
		// use element name instead, qualified with parent
		if (je instanceof IMethod) {
			// return je.getParent().getElementName() + '.' +
			return readableName((IMethod) je);
		}
		else if (je instanceof IType) {
			return je.getElementName();
		}
		else if (je instanceof IField) {
			return je.getElementName() + " - "
					+ ((IType) je.getParent()).getFullyQualifiedName();
		}
		else if (je.getParent() != null) {
			return je.getParent().getElementName() + '.' + je.getElementName();
		}
		return je.getElementName();
	}

	public static String getPackageLinkName(IJavaElement je) {
		if (je instanceof IMethod) {
			return ((IMethod) je).getDeclaringType().getPackageFragment()
					.getElementName();
		}
		else if (je instanceof IType) {
			return ((IType) je).getPackageFragment().getElementName();
		}
		return je.getElementName();
	}

	public static String readableName(IMethod method) {

		StringBuffer buffer = new StringBuffer(method.getElementName());
		buffer.append('(');
		String[] parameterTypes = method.getParameterTypes();
		int length;
		if (parameterTypes != null && (length = parameterTypes.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameterTypes[i]));
				if (i < length - 1) {
					buffer.append(", ");
				}
			}
		}
		buffer.append(')');
		return buffer.toString();
	}

	public static String getElementDescription(IAopReference reference) {
		StringBuffer buf = new StringBuffer(": <");
		buf.append(reference.getDefinition().getAspectName());
		buf.append("> [");
		buf.append(reference.getDefinition().getResource()
				.getProjectRelativePath().toString());
		buf.append("]");
		return buf.toString();
	}

	public static IMethod getMethod(IType type, String methodName,
			Class[] parameterTypes) {
		String[] parameterTypesAsString = getParameterTypesAsStringArray(parameterTypes);
		return getMethod(type, methodName, parameterTypesAsString);
	}

	private static String[] getParameterTypesAsStringArray(
			Class[] parameterTypes) {
		String[] parameterTypesAsString = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesAsString[i] = parameterTypes[i].getName();
		}
		return parameterTypesAsString;
	}

	private static String[] getParameterTypesAsStringArray(IMethod method) {
		String[] parameterTypesAsString = new String[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameterTypesAsString[i] = resolveClassName(method
					.getParameterTypes()[i], method.getDeclaringType());
		}
		return parameterTypesAsString;
	}

	public static final String resolveClassName(String className, IType type) {
		try {
			className = Signature.toString(className).replace('$', '.');
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		}
		catch (JavaModelException e) {
		}

		return className;
	}

	public static IMethod getMethod(IType type, String methodName,
			String[] parameterTypes) {
		int index = methodName.indexOf('(');
		if (index >= 0) {
			methodName = methodName.substring(0, index);
		}
		try {
			Set<IMethod> methods = Introspector.getAllMethods(type);
			for (IMethod method : methods) {
				if (method.getElementName().equals(methodName)
						&& method.getParameterTypes().length == parameterTypes.length) {
					String[] methodParameterTypes = getParameterTypesAsStringArray(method);
					if (Arrays.deepEquals(parameterTypes, methodParameterTypes)) {
						return method;
					}
				}
			}

			return Introspector.findMethod(type, methodName,
					parameterTypes.length, Public.YES, Static.DONT_CARE);
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static Set<IFile> getFilesToBuildFromBeansProject(IProject file) {
		Set<IFile> resourcesToBuild = new HashSet<IFile>();
		IBeansProject bp = BeansCorePlugin.getModel().getProject(
				file.getProject());
		if (bp != null && bp.getConfigs() != null && bp.getConfigs().size() > 0) {
			for (IBeansConfig config : bp.getConfigs()) {
				resourcesToBuild.add((IFile) config.getElementResource());
			}
		}
		return resourcesToBuild;
	}

	public static Set<IFile> getFilesToBuild(int kind, IResource resource) {
		Set<IFile> files = new HashSet<IFile>();
		if ((kind == IncrementalProjectBuilder.AUTO_BUILD
				|| kind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
				&& resource instanceof IFile
				&& resource.getName().endsWith(".java")) {
			Set<IBeansProject> projects = BeansCorePlugin.getModel()
					.getProjects();
			if (projects != null) {
				for (IBeansProject project : projects) {
					if (project != null) {
						Set<IBeansConfig> configs = project.getConfigs();
						IJavaElement element = JavaCore.create(resource);
						if (element instanceof ICompilationUnit) {
							try {
								IType[] types = ((ICompilationUnit) element)
										.getAllTypes();
								List<String> typeNames = new ArrayList<String>();
								for (IType type : types) {
									typeNames.add(type.getFullyQualifiedName());
								}
								for (IBeansConfig config : configs) {
									Set<String> allBeanClasses = config
											.getBeanClasses();
									for (String className : allBeanClasses) {
										if (typeNames.contains(className)) {
											files.add((IFile) config
													.getElementResource());
										}
									}
								}
							}
							catch (JavaModelException e) {
							}
						}
					}
				}
			}
		}
		else if (BeansCoreUtils.isBeansConfig(resource)) {
			files.add((IFile) resource);
		}
		return files;
	}

	public static IJavaProject getJavaProject(IBeansConfig config) {
		if (config != null) {
			IJavaProject project = JavaCore.create(config.getElementResource()
					.getProject());
			return project;
		}
		else {
			return null;
		}
	}

	public static IJavaProject getJavaProject(IResource config) {
		IJavaProject project = JavaCore.create(config.getProject());
		return project;
	}

	public static IJavaProject getJavaProject(IProject project) {
		IJavaProject jp = JavaCore.create(project);
		return jp;
	}

	public static int getLineNumber(IJavaElement element) {

		if (element != null && element instanceof IMethod) {
			try {
				IMethod method = (IMethod) element;
				int lines = 0;
				String targetsource;
				if (method.getDeclaringType() != null
						&& method.getDeclaringType().getCompilationUnit() != null) {
					targetsource = method.getDeclaringType()
							.getCompilationUnit().getSource();
					String sourceuptomethod = targetsource.substring(0, method
							.getNameRange().getOffset());

					char[] chars = new char[sourceuptomethod.length()];
					sourceuptomethod.getChars(0, sourceuptomethod.length(),
							chars, 0);
					for (char element0 : chars) {
						if (element0 == '\n') {
							lines++;
						}
					}
					return new Integer(lines + 1);
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IType) {
			try {
				IType type = (IType) element;
				int lines = 0;
				String targetsource;
				targetsource = type.getCompilationUnit().getSource();
				String sourceuptomethod = targetsource.substring(0, type
						.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars,
						0);
				for (char element0 : chars) {
					if (element0 == '\n') {
						lines++;
					}
				}
				return new Integer(lines + 1);
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IField) {
			try {
				IField type = (IField) element;
				int lines = 0;
				String targetsource;
				targetsource = type.getCompilationUnit().getSource();
				String sourceuptomethod = targetsource.substring(0, type
						.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars,
						0);
				for (char element0 : chars) {
					if (element0 == '\n') {
						lines++;
					}
				}
				return new Integer(lines + 1);
			}
			catch (JavaModelException e) {
			}
		}
		return new Integer(-1);
	}

	public static List<IMethod> getMatchesForAnnotationAspectDefinition(
			Class<?> targetClass, IAspectDefinition info, IProject project,
			Map<IAspectDefinition, Object> aspectDefinitionCache)
			throws Throwable {

		Object aspectJExpressionPointcut = null;
		if (aspectDefinitionCache.containsKey(info)) {
			aspectJExpressionPointcut = aspectDefinitionCache.get(info);
		}
		else {
			aspectJExpressionPointcut = AopReferenceModelBuilderUtils
					.initAspectJExpressionPointcut(info);
			aspectDefinitionCache.put(info, aspectJExpressionPointcut);
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
				IMethod jdtMethod = AopReferenceModelUtils.getMethod(
						jdtTargetClass, method.getName(), method
								.getParameterTypes());
				if (jdtMethod != null) {
					matchingMethod.add(jdtMethod);
				}
			}
		}
		return matchingMethod;
	}

	public static List<IMethod> getMatchesBeanAspectDefinition(
			Class<?> targetClass, IAspectDefinition info, IProject project,
			Map<IAspectDefinition, Object> aspectDefinitionCache)
			throws Throwable {

		Object aspectJExpressionPointcut = null;
		if (aspectDefinitionCache.containsKey(info)) {
			aspectJExpressionPointcut = aspectDefinitionCache.get(info);
		}
		else {
			aspectJExpressionPointcut = AopReferenceModelBuilderUtils
					.createAspectJPointcutExpression(info);
			aspectDefinitionCache.put(info, aspectJExpressionPointcut);
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
					IMethod jdtMethod = AopReferenceModelUtils.getMethod(
							jdtTargetType, m.getName(), m.getParameterTypes());
					if (jdtMethod != null) {
						matchingMethod.add(jdtMethod);
					}
				}
			}
		}
		return matchingMethod;
	}

	private static boolean checkMethod(Class targetClass, Method targetMethod,
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
			Class[] targetInterfaces = ClassUtils
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

	public static IBean getBeanFromElementId(String elementId) {
		IBeansModel model = BeansCorePlugin.getModel();
		return (IBean) model.getElement(elementId);
	}

}
