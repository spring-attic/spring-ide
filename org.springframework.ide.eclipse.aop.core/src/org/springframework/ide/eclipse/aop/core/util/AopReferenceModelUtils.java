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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Some helper methods.
 * @author Christian Dupuis
 */
public class AopReferenceModelUtils {

	private static final String JAVA_FILE_EXTENSION = ".java";

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

	public static Set<IFile> getAffectedFilesFromBeansProject(IProject file) {
		Set<IFile> affectedFiles = new HashSet<IFile>();
		IBeansProject bp = BeansCorePlugin.getModel().getProject(
				file.getProject());
		if (bp != null && bp.getConfigs() != null && bp.getConfigs().size() > 0) {
			for (IBeansConfig config : bp.getConfigs()) {
				affectedFiles.add((IFile) config.getElementResource());
			}
		}
		return affectedFiles;
	}

	public static Set<IResource> getAffectedFiles(int kind, IResource resource) {
		Set<IResource> files = new HashSet<IResource>();
		
		// since we moved to the new AbstractProjectBuilder we don't need the
		// following check.
		//if ((kind == IncrementalProjectBuilder.AUTO_BUILD
		//		|| kind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
		if (resource instanceof IFile
				&& resource.getName().endsWith(JAVA_FILE_EXTENSION)) {
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
			IBeansConfig beansConfig = (IBeansConfig) 
				BeansModelUtils.getResourceModelElement(resource);
			files.add((IFile) resource);
			
			// add confis from config set
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					resource.getProject());
			if (project != null) {
				Set<IBeansConfigSet> configSets = project.getConfigSets();
				for (IBeansConfigSet configSet : configSets) {
					if (configSet.getConfigs().contains(beansConfig)) {
						Set<IBeansConfig> bcs = configSet.getConfigs();
						for (IBeansConfig bc : bcs) {
							files.add(bc.getElementResource());
						}
					}
				}
			}
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
				IMethod jdtMethod = JdtUtils.getMethod(
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
					IMethod jdtMethod = JdtUtils.getMethod(
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
