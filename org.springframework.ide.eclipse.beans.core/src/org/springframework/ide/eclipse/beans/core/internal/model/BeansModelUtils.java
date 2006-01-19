/*
 * Copyright 2002-2006 the original author or authors.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
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
		IModelElement parent;
		if (element instanceof IBean || element instanceof IBeanAlias) {
			parent = element.getElementParent();
		} else if (element instanceof IBeanConstructorArgument ||
											element instanceof IBeanProperty) {
			parent = element.getElementParent().getElementParent();
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
		return (parent instanceof IBeansConfig ? (IBeansConfig) parent :
															getConfig(parent));
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
		} else if (element instanceof IBean || element instanceof IBeanAlias) {
			return getProject(element.getElementParent());
		} else if (element instanceof IBeanConstructorArgument ||
											element instanceof IBeanProperty) {
			return getProject(element.getElementParent().getElementParent());
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
	}

	/**
	 * Returns a list of all <code>BeanReference</code>s which have a target
	 * bean with given ID. The references are looked-up within a certain
	 * context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>).
	 * @param beanID  the ID of the bean which is referenced  
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referencing beans are looked-up
	 * @throws IllegalArgumentException if unsupported context specified 
	 */
	public static final List getBeanReferences(String beanID,
											   IModelElement context) {
		List references = new ArrayList();

		if (context instanceof IBeansConfig) {
			List configSets = getConfigSets((IBeansConfig) context);
			if (configSets.isEmpty()) {
				Collection beans = ((IBeansConfig) context).getBeans();
				addBeanReferences(beans, beanID, context, references);
			} else {
				Iterator iter = configSets.iterator();
				while (iter.hasNext()) {
					IBeansConfigSet configSet = (IBeansConfigSet) iter.next();
					Collection beans = ((IBeansConfig) context).getBeans();
					addBeanReferences(beans, beanID, configSet, references);
				}
			}
		} else if (context instanceof IBeansConfigSet) {
			Collection beans = ((IBeansConfigSet) context).getBeans();
			addBeanReferences(beans, beanID, context, references);
		} else {
			throw new IllegalArgumentException("Unsupported context " +
											   context);
		}
		return references;
	}

	/**
	 * Check given beans for a reference to a bean with given ID and add
	 * these references to the specified list.
	 */
	private static final void addBeanReferences(Collection beans,
					   String beanID, IModelElement context, List references) {
		Iterator iter = beans.iterator();
		while (iter.hasNext()) {
			IBean bean = (IBean) iter.next();
			Iterator refs = getBeanReferences(bean, context,
											  false).iterator();
			while (refs.hasNext()) {
				BeanReference reference = (BeanReference) refs.next();
				IModelElement target = reference.getTarget();
				if (target instanceof IBean &&
									  target.getElementName().equals(beanID)) {
					if (!references.contains(reference)) {
						references.add(reference);
					}
				}
			}
		}
	}

	/**
	 * Returns a list of <code>IBeansConfig</code>s a given config belongs to.
	 */
	public static final List getConfigSets(IBeansConfig config) {
		List configSets = new ArrayList();
		Iterator allConfigSets = ((IBeansProject)
						 config.getElementParent()).getConfigSets().iterator();
		while (allConfigSets.hasNext()) {
			IBeansConfigSet configSet = (IBeansConfigSet) allConfigSets.next();
			if (configSet.hasConfig(config.getElementName())) {
				configSets.add(configSet);
			}
		}
		return configSets;
	}

	/**
	 * Returns a collection of <code>BeanReference</code>s holding all
	 * <code>IBean</code>s which are referenced from given model element.
	 * For a bean it's parent bean (for child beans only), constructor argument
	 * values and property values are checked.
	 * <code>IBean</code> look-up is done from the specified
	 * <code>IBeanConfig</code> or <code>IBeanConfigSet</code>.
	 * @param element  the element (<code>IBean</code>,
	 *	   <code>IBeanConstructorArgument</code> or <code>IBeanProperty</code>)
	 *     to get all referenced beans from
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 * @param recursive  if set to <code>true</code> then the dependeny graph is
	 *			traversed recursively
	 * @throws IllegalArgumentException if unsupported model element specified
	 * @see BeanReference
	 */
	public static final List getBeanReferences(IModelElement element,
									IModelElement context, boolean recursive) {
		List references = new ArrayList();
		List referencedBeans = new ArrayList(); // used to break from cycles
		if (element instanceof Bean) {

			// Add referenced beans from bean element
			Bean bean = (Bean) element;

			// For a child bean add the parent bean
			if (!bean.isRootBean()) {
				IBean parentBean = getBean(bean.getParentName(), context);
				addBeanReference(BeanReference.PARENT_BEAN_TYPE, bean,
							 parentBean, context, references, referencedBeans);
				if (recursive) {
					// Now add all parent beans and all beans which are
					// referenced by the parent beans
					Set beanNames = new HashSet();  // used to detect a cycle
					beanNames.add(bean.getElementName());
					beanNames.add(parentBean.getElementName());
					while (parentBean != null && !parentBean.isRootBean()) {
						String parentName = parentBean.getParentName();
						if (beanNames.contains(parentName)) {
							// break from cycle
							break;
						}
						beanNames.add(parentName);
						parentBean = getBean(parentName, context);
						if (addBeanReference(BeanReference.PARENT_BEAN_TYPE,
										 bean, parentBean, context, references,
										 referencedBeans) && recursive) {
							addBeanReferencesForElement(parentBean, context,
									   references, referencedBeans, recursive);
						}
					}
				}
			}

			// Get bean's merged or standard bean definition
			AbstractBeanDefinition bd;
			if (recursive) {
				bd = (AbstractBeanDefinition) getMergedBeanDefinition(bean,
																	  context);
			} else {
				bd = (AbstractBeanDefinition) getBeanDefinition(bean);
			}

			// Add bean's factoy bean
			if (bd.getFactoryBeanName() != null) {
				IBean factoryBean = getBean(bd.getFactoryBeanName(), context);
				if (addBeanReference(BeanReference.FACTORY_BEAN_TYPE, bean,
						  factoryBean, context, references, referencedBeans) &&
								   recursive) {
					addBeanReferencesForElement(factoryBean, context,
									   references, referencedBeans, recursive);
				}
			}

			// Add bean's depends-on beans
			if (bd.getDependsOn() != null) {
				String[] dependsOnBeans = bd.getDependsOn();
				for (int i = 0; i < dependsOnBeans.length; i++) {
					IBean dependsOnBean = getBean(dependsOnBeans[i], context);
					if (addBeanReference(BeanReference.DEPENDS_ON_BEAN_TYPE,
									  bean, dependsOnBean, context, references,
									  referencedBeans) && recursive) {
						addBeanReferencesForElement(dependsOnBean, context,
									   references, referencedBeans, recursive);
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
						if (addBeanReference(
								 BeanReference.METHOD_OVERRIDE_BEAN_TYPE, bean,
								 overrideBean, context, references,
								 referencedBeans) && recursive) {
							addBeanReferencesForElement(overrideBean, context,
									   references, referencedBeans, recursive);
						}
					} else if (methodOverride instanceof ReplaceOverride) {
						String beanName = ((ReplaceOverride)
								 methodOverride).getMethodReplacerBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (addBeanReference(
								 BeanReference.METHOD_OVERRIDE_BEAN_TYPE, bean,
								 overrideBean, context, references,
								 referencedBeans) && recursive) {
							addBeanReferencesForElement(overrideBean, context,
									   references, referencedBeans, recursive);
						}
					}
				}
			}

			// Add beans referenced from bean's constructor arguments
			Iterator cargs = bean.getConstructorArguments().iterator();
			while (cargs.hasNext()) {
				IBeanConstructorArgument carg = (IBeanConstructorArgument)
																  cargs.next();
				addBeanReferencesForValue(bean, carg.getValue(), context,
									   references, referencedBeans, recursive);
			}

			// Add referenced beans from bean's properties
			Iterator properties = bean.getProperties().iterator();
			while (properties.hasNext()) {
				IBeanProperty property = (IBeanProperty) properties.next();
				addBeanReferencesForValue(property, property.getValue(), context,
									   references, referencedBeans, recursive);
			}
		} else if (element instanceof IBeanConstructorArgument) {

			// Add referenced beans from constructor arguments element
			IBeanConstructorArgument carg = (IBeanConstructorArgument) element;
			addBeanReferencesForValue(carg, carg.getValue(), context,
									   references, referencedBeans, recursive);
		} else if (element instanceof IBeanProperty) {

			// Add referenced beans from property element
			IBeanProperty property = (IBeanProperty) element;
			addBeanReferencesForValue(property, property.getValue(), context,
									   references, referencedBeans, recursive);
			
		} else {
			throw new IllegalArgumentException("Unsupported model element " +
											   element);
		}
		return references;
	}

	private static boolean addBeanReference(int type, IModelElement source,
									   IBean target, IModelElement context,
									   List references, List referencedBeans) {
		if (target != null && target  != source) {
			BeanReference ref = new BeanReference(type, source, target,
												  context); 
			if (!references.contains(ref)) {
				references.add(ref);
				if (!referencedBeans.contains(target)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds the all <code>IBean</code>s which are referenced by the specified
	 * model element to the given list as an instance of
	 * <code>BeanReference</code>.
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 * @param referencedBeans  used to break from cycles
	 */
	private static final void addBeanReferencesForElement(IModelElement element,
				  IModelElement context, List references, List referencedBeans, 
				  boolean recursive) {
		if (!referencedBeans.contains(element)) {
			Iterator refs = getBeanReferences(element, context,
											  recursive).iterator();
			while (refs.hasNext()) {
				BeanReference ref = (BeanReference) refs.next();
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
			referencedBeans.add(element);
		}
	}

	/**
	 * Given a bean property's or constructor argument's value, adds any
	 * beans referenced by this value. This value could be:
	 * <li>A RuntimeBeanReference, which bean will be added.
	 * <li>A BeanDefinitionHolder. This is an inner bean that may contain
	 * RuntimeBeanReferences which will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 */
	private static final void addBeanReferencesForValue(IModelElement element,
						  Object value, IModelElement context, List references,
						  List referencedBeans, boolean recursive) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			IBean bean = getBean(beanName, context);
			if (addBeanReference(BeanReference.STANDARD_BEAN_TYPE, element,
								 bean, context, references, referencedBeans) &&
								 recursive) {
				addBeanReferencesForElement(bean, context, references,
											referencedBeans, recursive);
			}
		} else if (value instanceof BeanDefinitionHolder) {
			String beanName = ((BeanDefinitionHolder) value).getBeanName();
			IBean bean = getInnerBean(beanName, context);
			addBeanReference(BeanReference.INNER_BEAN_TYPE,
							bean.getElementParent(), bean, context, references,
							referencedBeans);
			addBeanReferencesForElement(bean, context, references,
										referencedBeans, recursive);
		} else if (value instanceof List) {

			// Add bean property's interceptors
			if (element instanceof IBeanProperty &&
						 element.getElementName().equals("interceptorNames")) {
				IType type = getBeanType((IBean) element.getElementParent(),
										 context);
				if (type != null) {
					if (type.getFullyQualifiedName().equals(
						"org.springframework.aop.framework.ProxyFactoryBean")) {
						Iterator names = ((List) value).iterator();
						while (names.hasNext()) {
							Object name = (Object) names.next();
							if (name instanceof String) {
								IBean interceptor = getBean((String) name,
															context);
								if (addBeanReference(
										   BeanReference.INTERCEPTOR_BEAN_TYPE,
										   element, interceptor, context,
										   references, referencedBeans) &&
										   recursive) {
									addBeanReferencesForElement(interceptor,
												   context, references,
											   	   referencedBeans, recursive);
								}
							}
						}
					}
				}
			} else {
				List list = (List) value;
				for (int i = 0; i < list.size(); i++) {
					addBeanReferencesForValue(element, list.get(i), context,
									   references, referencedBeans, recursive);
				}
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addBeanReferencesForValue(element, iter.next(), context,
									   references, referencedBeans, recursive);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addBeanReferencesForValue(element, map.get(iter.next()), context,
									   references, referencedBeans, recursive);
			}
		}
	}

	/**
	 * Returns the bean definition for a given bean.
	 * @param bean  the bean the bean definition is requested for
	 * @return given bean's bean definition 
	 */
	public static final BeanDefinition getBeanDefinition(IBean bean) {
		return ((Bean) bean).getBeanDefinitionHolder().getBeanDefinition();
	}

	/**
	 * Returns the merged bean definition for a given bean from specified
	 * context (<code>IBeansConfig</code> or <code>IBeansConfigSet</code>).
	 * Any cyclic-references are ignored.
	 * @param bean  the bean the merged bean definition is requested for
	 * @param context  the context (<code>IBeanConfig</code> or
	 * 		  <code>IBeanConfigSet</code>) the beans are looked-up
	 * @return given bean's merged bean definition 
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static final BeanDefinition getMergedBeanDefinition(IBean bean,
													   IModelElement context) {
		BeanDefinition bd = getBeanDefinition(bean);
		if (!bean.isRootBean()) {

			// Fill a set with all bean definitions belonging to the
			// hierarchy of the requested bean definition 
			List beanDefinitions = new ArrayList();  // used to detect a cycle
			beanDefinitions.add(bd);
			addBeanDefinition(bean, context, beanDefinitions);

			// Merge the bean definition hierarchy to a single bean
			// definition
			RootBeanDefinition rbd = null;
			int bdCount = beanDefinitions.size();
			for (int i = bdCount - 1; i >= 0; i--) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition)
														beanDefinitions.get(i);
				if (rbd != null) {
					rbd.overrideFrom(abd);
				} else {
					if (abd instanceof RootBeanDefinition) {
						rbd = new RootBeanDefinition((RootBeanDefinition) abd);
					} else {

						// root of hierarchy is not a root bean definition
						break;
					}
				}
			}
			if (rbd != null) {
				return rbd;
			}
		}
		return bd;
	}

	private static final void addBeanDefinition(IBean bean,
			   					 IModelElement context, List beanDefinitions) {
		String parentName = bean.getParentName();
		Bean parentBean = (Bean) getBean(parentName, context);
		if (parentBean != null) {
			BeanDefinition parentBd =
					  parentBean.getBeanDefinitionHolder().getBeanDefinition();
			// Break cyclic references
			if (!parentName.equals(bean.getElementName()) &&
										 !beanDefinitions.contains(parentBd)) {
				beanDefinitions.add(parentBd);
				if (!parentBean.isRootBean()) {
					addBeanDefinition(parentBean, context, beanDefinitions);
				}
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
				String configName = (String) configs.next();
				IBeansConfig config = BeansModelUtils.getConfig(configName,
																context);
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
	 * @return the Java type of givern bean's class or <code>null</code> if
	 *			no bean class defined or type not found
	 */
	public static final IType getBeanType(IBean bean, IModelElement context) {
		Assert.notNull(bean);
		String className = getBeanClass(bean, context);
		if (className != null) {
			return getJavaType(getProject(bean).getProject(), className);
		}
		return null;
	}

	/**
	 * Returns the first constructor argument defined for given bean.
	 * @param bean  the bean to lookup the first constructor argument
	 * @return the first constructor argument or <code>null</code> if no
	 * 			constructor argument is defined
	 */
	public static final IBeanConstructorArgument getFirstConstructorArgument(
																  IBean bean) {
		IBeanConstructorArgument firstCarg = null;
		int firstCargStartLine = Integer.MAX_VALUE;
		Iterator cargs = bean.getConstructorArguments().iterator();
		while (cargs.hasNext()) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument) cargs.next();
			if (carg.getElementStartLine() < firstCargStartLine) {
				firstCarg = carg;
				firstCargStartLine = carg.getElementStartLine();
			}
		}
		return firstCarg;
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

	public static final void removeProblemMarkers(IModelElement element) {
		if (element instanceof IBeansConfig) {
			IFile file = ((IBeansConfig) element).getConfigFile();
			BeansCoreUtils.deleteProblemMarkers(file);
		} else if (element instanceof IBeansProject) {
			Iterator configs = ((IBeansProject)
											  element).getConfigs().iterator();
			while (configs.hasNext()) {
				IBeansConfig config = (IBeansConfig) configs.next();
				removeProblemMarkers(config);
			}
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
