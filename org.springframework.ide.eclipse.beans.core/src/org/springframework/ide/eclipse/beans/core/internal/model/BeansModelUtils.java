/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class BeansModelUtils {

	/**
	 * Returns config for given name from specified project.
	 */
	public static IBeansConfig getConfig(String configName,
										 IBeansProject project) {
		// For external project get the corresponding project from beans model 
		if (configName.charAt(0) == '/') {
			String projectName = configName.substring(0,
													configName.indexOf('/', 1));
			project = BeansCorePlugin.getModel().getProject(projectName);
		}
		return (project != null ? project.getConfig(configName) : null);
	}

	/**
	 * Given a bean's property or constructor argument and it's value, adds any
	 * beans referenced by it's value. This value could be:
	 * <li>A RuntimeBeanReference, which bean will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 */
	public static void addReferencedBeanNamesForValue(
					 IBeansModelElement element, Object value, List beanNames) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			if (!beanNames.contains(beanName)) {
				beanNames.add(beanName);
			}
			IBeansModelElement parent =
								  element.getElementParent().getElementParent();
			IBean bean = getBean(parent, beanName);
			if (bean != null) {
				addReferencedBeanNamesForBean(bean, beanNames);
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencedBeanNamesForValue(element, list.get(i), beanNames);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addReferencedBeanNamesForValue(element, iter.next(), beanNames);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addReferencedBeanNamesForValue(element, map.get(iter.next()),
											   beanNames);
			}
		}
	}

	/**
	 * Adds the names of all beans which are referenced by the specified bean to
	 * the given list.
	 */
	public static void addReferencedBeanNamesForBean(IBean bean, List beanNames) {
		Iterator iter = bean.getReferencedBeans().iterator();
		while (iter.hasNext()) {
			bean = (IBean) iter.next();
			if (!beanNames.contains(bean.getElementName())) {
				beanNames.add(bean.getElementName());
			}
		}
	}

	/**
	 * Returns the IBean for a given bean name from specified Config or
	 * ConfigSet element.
	 * @return IBean or null if bean not defined
	 */
	public static final IBean getBean(IBeansModelElement element,
									  String beanName) {
		IBean bean = null;
		if (element instanceof IBeansConfig) {
			bean = ((IBeansConfig) element).getBean(beanName);
		} else if (element instanceof IBeansConfigSet) {
			bean = ((IBeansConfigSet) element).getBean(beanName);
		}
		return bean;
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project  the JDT project the class belongs to
	 * @param className  the full qualified class name of the requested Java
	 * 					type
	 * @return the requested Java type or null if the class is not defined or
	 * 		   the project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		if (className != null && project.isAccessible()) {

			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0 && pos < (className.length() - 1)) {
				className = className.substring(0, pos) + '.' +
							className.substring(pos + 1);
			}
			try {
				// Find type in this project
				if (project.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = (IJavaProject)
										  project.getNature(JavaCore.NATURE_ID);
					IType type = javaProject.findType(className);
					if (type != null) {
						return type;
					}
				}
	
				// Find type in referenced Java projects
				IProject[] projects = project.getReferencedProjects();
				for (int i = 0; i < projects.length; i++) {
					IProject refProject = projects[i];
					if (refProject.isAccessible() &&
									 refProject.hasNature(JavaCore.NATURE_ID)) {
						IJavaProject javaProject = (IJavaProject)
									   refProject.getNature(JavaCore.NATURE_ID);
						IType type = javaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
	 			}
			} catch (CoreException e) {
				BeansCorePlugin.log("Error getting Java type '" + className +
									"'", e); 
			}
		}
		return null;
	}

	public static final void createProblemMarker(IBeansModelElement element,
						String message, int severity, int line, int errorCode) {
		createProblemMarker(element, message, severity, line, errorCode, null,
							null);
	}

	public static final void createProblemMarker(IBeansModelElement element,
						  String message, int severity, int line, int errorCode,
						  String beanID, String errorData) {
		IFile file;
		if (element instanceof IBeansConfig) {
			file = ((IBeansConfig) element).getConfigFile();
		} else if (element instanceof IBean) {
			file = ((IBean) element).getConfig().getConfigFile();
		} else if (element instanceof IBeanProperty) {
			IBean bean = (IBean) ((IBeanProperty) element).getElementParent();
			file = bean.getConfig().getConfigFile();
		} else if (element instanceof IBeanConstructorArgument) {
			IBean bean = (IBean)
						((IBeanConstructorArgument) element).getElementParent();
			file = bean.getConfig().getConfigFile();
		} else {
			file = null;
		}
		if (file != null) {
			BeansCoreUtils.createProblemMarker(file, message, severity, line,
											   errorCode, beanID, errorData);
		}
	}

	public static final void registerBeanDefinitions(IBeansConfig config,
											 BeanDefinitionRegistry registry) {
		Iterator beans = config.getBeans().iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();
			try {
				BeanDefinitionReaderUtils.registerBeanDefinition(
									 bean.getBeanDefinitionHolder(), registry);
			} catch (BeansException e) {
				// ignore - continue with next bean
			}
		}
	}
}
