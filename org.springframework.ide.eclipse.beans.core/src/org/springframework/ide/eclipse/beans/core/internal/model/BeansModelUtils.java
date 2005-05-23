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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.util.Assert;

public class BeansModelUtils {

	/**
	 * Returns config for given name from specified context
	 * (<code>IBeansProject</code> or <code>IBeansConfigSet</code>).
	 * Externally referenced configs (config name starts with '/') are
	 * recognized too.
	 * @param configName  the name of the config to look for
	 * @param context  the context used for config look-up
	 * @throws IllegalArgumentException if unsupported context specified 
	 */
	public static final IBeansConfig getConfig(String configName,
											   IModelElement context) {
		// For external project get the corresponding project from beans model 
		if (configName.charAt(0) == '/') {
			int pos = configName.indexOf('/', 1);

			// Extract project and config name from full qualified config name
			String projectName = configName.substring(1, pos);
			configName = configName.substring(pos + 1);
			IBeansProject project = BeansCorePlugin.getModel().getProject(
																  projectName);
			if (project != null) {
				return project.getConfig(configName);
			}
		} else if (context instanceof IBeansProject) {
			return ((IBeansProject) context).getConfig(configName);
		} else if (context instanceof IBeansConfigSet) {
			return ((IBeansProject) context.getElementParent()).getConfig(
																   configName);
		}
		return null;
	}

	/**
	 * Returns the <code>IBeanConfig</code> the given model element
	 * (<code>IBean</code>, <code>IBeanConstructorArgument</code> or
	 * <code>IBeanProperty</code>) belongs to.
	 * @param element  the model element to get the beans config for
	 * @throws IllegalArgumentException if unsupported model element specified 
	 */
	public static final IBeansConfig getConfig(IModelElement element) {
		if (element instanceof IBean) {
			return (IBeansConfig) element.getElementParent();
		} else if (element instanceof IBeanConstructorArgument ||
											element instanceof IBeanProperty) {
			return (IBeansConfig) element.getElementParent().getElementParent();
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
	}

	/**
	 * Returns the <code>IBeansProject</code> the given model element belongs
	 * to.
	 * @param element  the model element to get the beans project for
	 * @throws IllegalArgumentException if unsupported model element specified 
	 */
	public static final IBeansProject getProject(IModelElement element) {
		if (element instanceof IBeansProject) {
			return (IBeansProject) element;
		} else if (element instanceof IBeansConfig ||
										  element instanceof IBeansConfigSet) {
			return (IBeansProject) element.getElementParent();
		} else if (element instanceof IBean) {
			return (IBeansProject)
								 element.getElementParent().getElementParent();
		} else if (element instanceof IBeanConstructorArgument ||
											element instanceof IBeanProperty) {
			return (IBeansProject) element.getElementParent()
										.getElementParent().getElementParent();
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
	}

	/**
	 * Returns a collection with all <code>IBean</code>s which are referenced
	 * from given model element (<code>IBean</code>,
	 * <code>IBeanConstructorArgument</code> or <code>IBeanProperty</code>).
	 * For a bean it's parent bean (for child beans only), constructor argument
	 * values and property values are checked.
	 * <code>IBean</code> look-up is done from the <code>IBeanConfig</code>
	 * the given model element belongs to.
	 * @param element  the element to get all referenced beans from
	 * @throws IllegalArgumentException if unsupported model element specified 
	 */
	public static final Collection getReferencedBeans(IModelElement element,
													  boolean recursive) {
		return getReferencedBeans(element, getConfig(element), recursive);
	}

	/**
	 * Returns a collection with all <code>IBean</code>s which are referenced
	 * from given model element (<code>IBean</code>,
	 * <code>IBeanConstructorArgument</code> or <code>IBeanProperty</code>).
	 * For a bean it's parent bean (for child beans only), constructor argument
	 * values and property values are checked.
	 * <code>IBean</code> look-up is done from the specified
	 * <code>IBeanConfig</code> or <code>IBeanConfigSet</code>.
	 * @param element  the element to get all referenced beans from
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 * @throws IllegalArgumentException if unsupported model element specified 
	 */
	public static final Collection getReferencedBeans(IModelElement element,
									IModelElement context, boolean recursive) {
		List referencedBeans = new ArrayList();
		if (element instanceof Bean) {

			// Add referenced beans from bean element
			Bean bean = (Bean) element;
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
							bean.getBeanDefinitionHolder().getBeanDefinition();
			// For a child bean add all parent beans and all beans which are
			// referenced by the parent beans
			for (IBean parentBean = bean; parentBean != null &&
												  !parentBean.isRootBean(); ) {
				String parentName = parentBean.getParentName();
				if (parentName != null) {
					parentBean = getBean(parentName, context);
					if (parentBean != null &&
									   !referencedBeans.contains(parentBean)) {
						referencedBeans.add(parentBean);
						if (recursive) {
							addReferencedBeansForElement(parentBean, context,
												   referencedBeans, recursive);
						}
					}
				}
			}

			// Add bean's factoy bean
			if (bd.getFactoryBeanName() != null) {
				IBean factoryBean = getBean(bd.getFactoryBeanName(), context);
				if (factoryBean != null &&
									  !referencedBeans.contains(factoryBean)) {
					referencedBeans.add(factoryBean);
					if (recursive) {
						addReferencedBeansForElement(factoryBean, context,
												   referencedBeans, recursive);
					}
				}
			}

			// Add bean's depends-on beans
			if (bd.getDependsOn() != null) {
				String[] dependsOnBeans = bd.getDependsOn();
				for (int i = 0; i < dependsOnBeans.length; i++) {
					String beanName = dependsOnBeans[i];
					IBean dependsOnBean = getBean(beanName, context);
					if (dependsOnBean != null &&
									!referencedBeans.contains(dependsOnBean)) {
						referencedBeans.add(dependsOnBean);
						if (recursive) {
							addReferencedBeansForElement(dependsOnBean,
										  context, referencedBeans, recursive);
						}
					}
				}
			}

			// Add beans from bean's MethodOverrides
			if (!bd.getMethodOverrides().isEmpty()) {
				Iterator methodsOverrides =
							 bd.getMethodOverrides().getOverrides().iterator();
				while (methodsOverrides.hasNext()) {
					MethodOverride methodOverride = (MethodOverride)
													   methodsOverrides.next();
					if (methodOverride instanceof LookupOverride) {
						String beanName = ((LookupOverride)
												 methodOverride).getBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (overrideBean != null &&
									 !referencedBeans.contains(overrideBean)) {
							referencedBeans.add(overrideBean);
							if (recursive) {
								addReferencedBeansForElement(overrideBean,
										  context, referencedBeans, recursive);
							}
						}
					} if (methodOverride instanceof ReplaceOverride) {
						String beanName = ((ReplaceOverride)
								 methodOverride).getMethodReplacerBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (overrideBean != null &&
									 !referencedBeans.contains(overrideBean)) {
							referencedBeans.add(overrideBean);
							if (recursive) {
								addReferencedBeansForElement(overrideBean,
										  context, referencedBeans, recursive);
							}
						}
					}
				}
			}

			// Add beans referenced from bean's constructor arguments
			Iterator cargs = bean.getConstructorArguments().iterator();
			while (cargs.hasNext()) {
				IBeanConstructorArgument carg = (IBeanConstructorArgument)
																   cargs.next();
				addReferencedBeansForValue(carg.getValue(), context,
										   referencedBeans, recursive);
			}

			// Add referenced beans from bean's properties
			Iterator properties = bean.getProperties().iterator();
			while (properties.hasNext()) {
				IBeanProperty property = (IBeanProperty) properties.next();
				Object value = property.getValue();

				// Add bean's interceptors
				if (value instanceof List &&
						"interceptorNames".equals(property.getElementName())) {
					IType type = getBeanType(bean, context);
					if (type != null) {
						if ("org.springframework.aop.framework.ProxyFactoryBean"
									   .equals(type.getFullyQualifiedName())) {
							Iterator names = ((List) value).iterator();
							while (names.hasNext()) {
								Object name = (Object) names.next();
								if (name instanceof String) {
									IBean interceptor = getBean((String) name,
																context);
									if (interceptor != null &&
													 !referencedBeans.contains(
																interceptor)) {
										referencedBeans.add(interceptor);
										if (recursive) {
											addReferencedBeansForElement(
												   interceptor, context,
												   referencedBeans, recursive);
										}
									}
								}
							}
						}
					}
				} else {
					addReferencedBeansForValue(value, context, referencedBeans,
											   recursive);
				}
			}
		} else if (element instanceof IBeanConstructorArgument) {

			// Add referenced beans from constructor arguments element
			IBeanConstructorArgument carg  = (IBeanConstructorArgument) element;
			addReferencedBeansForValue(carg.getValue(), context,
									   referencedBeans, recursive);
		} else if (element instanceof IBeanProperty) {

			// Add referenced beans from property element
			IBeanProperty property  = (IBeanProperty) element;
			addReferencedBeansForValue(property.getValue(), context,
									   referencedBeans, recursive);
			
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
		return referencedBeans;
	}

	/**
	 * Adds the all <code>IBean</code>s which are referenced by the specified
	 * model element to the given list.
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 */
	public static final void addReferencedBeansForElement(IModelElement element,
			  IModelElement context, List referencedBeans, boolean recursive) {
		Iterator beans = getReferencedBeans(element, context,
											recursive).iterator();
		while (beans.hasNext()) {
			IBean bean = (IBean) beans.next();
			if (!referencedBeans.contains(bean)) {
				referencedBeans.add(bean);
			}
		}
	}

	/**
	 * Given a bean property's or constructor argument's value, adds any
	 * beans referenced by this value. This value could be:
	 * <li>A RuntimeBeanReference, which bean will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 */
	public static final void addReferencedBeansForValue(Object value,
			  IModelElement context, List referencedBeans, boolean recursive) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			IBean bean = getBean(beanName, context);
			if (bean != null && !referencedBeans.contains(bean)) {
				referencedBeans.add(bean);
				if (recursive) {
					addReferencedBeansForElement(bean, context,
												 referencedBeans, recursive);
				}
			}
		} else if (value instanceof BeanDefinitionHolder) {
			if (recursive) {
				String beanName = ((BeanDefinitionHolder) value).getBeanName();
				IBean bean = getInnerBean(beanName, context);
				if (bean != null && !referencedBeans.contains(bean)) {
	
					// TODO howto handle inner beans - currently we don't add
					// them referencedBeans.add(bean);
					addReferencedBeansForElement(bean, context,
												 referencedBeans, recursive);
				}
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencedBeansForValue(list.get(i), context,
										   referencedBeans, recursive);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(iter.next(), context,
										   referencedBeans, recursive);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(map.get(iter.next()), context,
										   referencedBeans, recursive);
			}
		}
	}

	/**
	 * Returns the IBean for a given bean name from specified context
	 * (<code>IBeansConfig</code> or <code>IBeansConfigSet</code>).
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 * @return IBean or null if bean not defined in the context
	 * @throws IllegalArgumentException if unsupported context specified 
	 */
	public static final IBean getBean(String beanName, IModelElement context) {
		if (context instanceof IBeansConfig) {
			return ((IBeansConfig) context).getBean(beanName);
		} else if (context instanceof IBeansConfigSet) {
			return ((IBeansConfigSet) context).getBean(beanName);
		} else {
			throw new IllegalArgumentException("Unsupported context " +
											   context);
		}
	}

	/**
	 * Returns the inner IBean for a given bean name from specified context
	 * (<code>IBeansConfig</code> or <code>IBeansConfigSet</code>).
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 * @return IBean or null if bean not defined
	 * @throws IllegalArgumentException if unsupported context specified 
	 */
	public static final IBean getInnerBean(String beanName,
										   IModelElement context) {
		if (context instanceof IBeansConfig) {
			Iterator beans = ((IBeansConfig)
										   context).getInnerBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				if (beanName.equals(bean.getElementName())) {
					return bean;
				}
			}
			return null;
		} else if (context instanceof IBeansConfigSet) {
			Iterator configs = ((IBeansConfigSet)
					   						  context).getConfigs().iterator();
			while (configs.hasNext()) {
				IBeansConfig config = (IBeansConfig) configs.next();
				Iterator beans = config.getInnerBeans().iterator();
				while (beans.hasNext()) {
					IBean bean = (IBean) beans.next();
					if (beanName.equals(bean.getElementName())) {
						return bean;
					}
				}
			}
			return ((IBeansConfigSet) context).getBean(beanName);
		} else {
			throw new IllegalArgumentException("Unsupported context " +
											   context);
		}
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project  the JDT project the class belongs to
	 * @param className  the full qualified class name of the requested Java
	 * 					type
	 * @return the requested Java type or null if the class is not defined or
	 * 		   the project is not accessible
	 */
	public static final IType getJavaType(IProject project, String className) {
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

	/**
	 * Returns the given bean's class name.
	 * @param bean  the bean to lookup the bean class name for
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 */
	public static final String getBeanClass(IBean bean, IModelElement context) {
		Assert.notNull(bean);
		if (bean.isRootBean()) {
			return bean.getClassName();
		} else {
			if (context == null) {
				context = bean.getElementParent();
			}
			do {
				String parentName = bean.getParentName();
				if (parentName != null) {
					bean = getBean(parentName, context);
					if (bean != null && bean.isRootBean()) {
						return bean.getClassName();
					}
				}
			} while (bean != null);
		}
		return null;
	}

	/**
	 * Returns the corresponding Java type for given bean's class.
	 * @param bean  the bean to lookup the bean class' Java type
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 */
	public static final IType getBeanType(IBean bean, IModelElement context) {
		Assert.notNull(bean);
		String className = getBeanClass(bean, context);
		if (className != null) {
			return getJavaType(getProject(bean).getProject(), className);
		}
		return null;
	}

	public static final void createProblemMarker(IModelElement element,
						String message, int severity, int line, int errorCode) {
		createProblemMarker(element, message, severity, line, errorCode, null,
							null);
	}

	public static final void createProblemMarker(IModelElement element,
						  String message, int severity, int line, int errorCode,
						  String beanID, String errorData) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement)
												 element).getElementResource();
			BeansCoreUtils.createProblemMarker(resource, message, severity,
										   line, errorCode, beanID, errorData);
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
