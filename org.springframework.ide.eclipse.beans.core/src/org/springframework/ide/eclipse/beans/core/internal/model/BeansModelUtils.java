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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.BeansTags;
import org.springframework.ide.eclipse.beans.core.BeansTags.Tag;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker.ErrorCode;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper methods for working with the BeansCoreModel.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class BeansModelUtils {

	/**
	 * only allow use of static helper methods
	 */
	private BeansModelUtils() {
	}

	/**
	 * Returns config for given name from specified context (<code>IBeansProject</code>
	 * or <code>IBeansConfigSet</code>). Externally referenced configs
	 * (config name starts with '/') are recognized too.
	 * @param configName the name of the config to look for
	 * @param context the context used for config look-up
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static IBeansConfig getConfig(String configName,
			IModelElement context) {
		// For external project get the corresponding project from beans model
		if (configName.charAt(0) == IBeansConfigSet.EXTERNAL_CONFIG_NAME_PREFIX) {
			// Extract project and config name from full qualified config name
			int pos = configName.indexOf('/', 1);
			String projectName = configName.substring(1, pos);
			configName = configName.substring(pos + 1);
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					projectName);
			if (project != null) {
				return project.getConfig(configName);
			}
		}
		else if (context instanceof IBeansProject) {
			return ((IBeansProject) context).getConfig(configName);
		}
		else if (context instanceof IBeansConfigSet) {
			return ((IBeansProject) context.getElementParent())
					.getConfig(configName);
		}
		return null;
	}

	/**
	 * Returns the {@link IBeanConfig} the given {@link IModelElement} belongs
	 * to.
	 * @param element the model element to get the beans config for
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static IBeansConfig getConfig(IModelElement element) {
		if (element instanceof IBeansConfig) {
			return (IBeansConfig) element;
		}
		else if (element instanceof ISourceModelElement) {
			do {
				element = element.getElementParent();
			} while (!(element instanceof IBeansConfig));
			return (IBeansConfig) element;
		}
		throw new IllegalArgumentException("Unsupported model element "
				+ element);
	}

	/**
	 * Returns all {@link IBeansConfigSet} the given {@link IModelElement}
	 * belongs to.
	 * @param element the model element to get the beans config for
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<IBeansConfigSet> getConfigSets(IModelElement element) {
		Set<IBeansConfigSet> configSets = new HashSet<IBeansConfigSet>();
		if (element instanceof IBeansConfigSet) {
			configSets.add((IBeansConfigSet) element);
		}
		else if (element instanceof IBeansConfig) {
			IBeansProject beansProject = getProject(element);
			Set<IBeansConfigSet> css = beansProject.getConfigSets();
			for (IBeansConfigSet cs : css) {
				if (cs.getConfigs().contains(element)) {
					configSets.add(cs);
				}
			}
		}
		else if (element instanceof ISourceModelElement) {
			IBeansConfig bc = getConfig(element);
			IBeansProject beansProject = getProject(bc);
			Set<IBeansConfigSet> css = beansProject.getConfigSets();
			for (IBeansConfigSet cs : css) {
				if (cs.getConfigs().contains(bc)) {
					configSets.add(cs);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported model element "
					+ element);
		}
		return configSets;
	}

	/**
	 * Returns the <code>IBeansProject</code> the given model element belongs
	 * to.
	 * @param element the model element to get the beans project for
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static IBeansProject getProject(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			if (resource != null) {
				IBeansProject project = BeansCorePlugin.getModel().getProject(
						resource.getProject());
				if (project != null) {
					return project;
				}
			}
		}
		throw new IllegalArgumentException("Unsupported model element "
				+ element);
	}

	/**
	 * Returns a list of all beans which belong to the given model element.
	 * @param element the model element which contains beans
	 * @param monitor the progress monitor to indicate progess; mark the monitor
	 * done after completing the work
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<IBean> getBeans(IModelElement element,
			IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Set<IBean> beans = new LinkedHashSet<IBean>();
		if (element instanceof IBeansModel) {
			Set<IBeansProject> projects = ((IBeansModel) element).getProjects();
			int worked = 0;
			monitor.beginTask("Locating Spring Bean definitions", projects
					.size());
			try {
				for (IBeansProject project : projects) {
					monitor
							.subTask("Locating Spring Bean definitions in project '"
									+ project.getElementName() + "'");
					for (IBeansConfig config : project.getConfigs()) {
						monitor
								.subTask("Loading Spring Bean defintion from file '"
										+ config.getElementName() + "'");
						beans.addAll(config.getBeans());
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						Set<IBeansComponent> components = config
								.getComponents();
						for (IBeansComponent componet : components) {
							beans.addAll(componet.getBeans());
						}
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					monitor.worked(worked++);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}
			finally {
				monitor.done();
			}
		}
		else if (element instanceof IBeansProject) {
			Set<IBeansConfig> configs = ((IBeansProject) element).getConfigs();
			int worked = 0;
			monitor.beginTask("Locating Spring Bean definitions", configs
					.size());
			try {
				for (IBeansConfig config : configs) {
					monitor.subTask("Loading Spring Bean defintion from file '"
							+ config.getElementName() + "'");
					beans.addAll(config.getBeans());
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					Set<IBeansComponent> components = config.getComponents();
					for (IBeansComponent componet : components) {
						beans.addAll(componet.getBeans());
					}
					monitor.worked(worked++);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}
			finally {
				monitor.done();
			}
		}
		else if (element instanceof IBeansConfig) {
			beans.addAll(((IBeansConfig) element).getBeans());
		}
		else if (element instanceof IBeansConfigSet) {
			beans.addAll(((IBeansConfigSet) element).getBeans());
		}
		else if (element instanceof IBeansComponent) {
			beans.addAll(((IBeansComponent) element).getBeans());
		}
		else if (element instanceof IBean) {
			beans.add((IBean) element);
		}
		else {
			throw new IllegalArgumentException("Unsupported model element "
					+ element);
		}
		return beans;
	}

	/**
	 * Returns a collection of {@link BeansConnection}s holding all
	 * {@link IBean<}s which are referenced from given model element. For a
	 * bean it's parent bean (for child beans only), constructor argument values
	 * and property values are checked. {@link IBean} look-up is done from the
	 * specified {@link IBeanConfig} or {@link IBeanConfigSet}.
	 * @param element the element ({@link IBean},
	 * {@link IBeanConstructorArgument} or {@link IBeanProperty}) to get all
	 * referenced beans from
	 * @param context the context ({@link IBeanConfig} or
	 * {@link IBeanConfigSet}) the referenced beans are looked-up
	 * @param recursive set to <code>true</code> if the dependeny graph is
	 * traversed recursively
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<BeansConnection> getBeanReferences(IModelElement element,
			IModelElement context, boolean recursive) {
		Set<BeansConnection> references = new LinkedHashSet<BeansConnection>();
		Set<IBean> referencedBeans = new HashSet<IBean>(); // used to break
		// from cycles
		return getBeanReferences(element, context, recursive, references,
				referencedBeans);
	}

	private static Set<BeansConnection> getBeanReferences(
			IModelElement element, IModelElement context, boolean recursive,
			Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (element instanceof IBeansComponent) {
			addBeanReferencesForBeansComponent((IBeansComponent) element,
					context, recursive, references, referencedBeans);
		}
		else if (element instanceof Bean) {

			// Add referenced beans from bean element
			Bean bean = (Bean) element;

			// For a child bean add the parent bean
			if (bean.isChildBean()) {
				IBean parentBean = getBean(bean.getParentName(), context);
				if (addBeanReference(BeanType.PARENT, bean, parentBean,
						context, references, referencedBeans)
						&& recursive) {
					// Now add all parent beans and all beans which are
					// referenced by the parent beans
					// The HashSet is used to detect a cycle
					Set<String> beanNames = new HashSet<String>();
					beanNames.add(bean.getElementName());
					beanNames.add(parentBean.getElementName());
					while (parentBean != null && parentBean.isChildBean()) {
						String parentName = parentBean.getParentName();
						if (beanNames.contains(parentName)) {
							// break from cycle
							break;
						}
						beanNames.add(parentName);
						parentBean = getBean(parentName, context);
						if (addBeanReference(BeanType.PARENT, bean, parentBean,
								context, references, referencedBeans)
								&& recursive) {
							addBeanReferencesForBean(parentBean, context,
									recursive, references, referencedBeans);
						}
					}
				}
			}

			// Get bean's merged or standard bean definition
			AbstractBeanDefinition bd;
			if (recursive) {
				bd = (AbstractBeanDefinition) getMergedBeanDefinition(bean,
						context);
			}
			else {
				bd = (AbstractBeanDefinition) (bean).getBeanDefinition();
			}

			// Add bean's factoy bean
			if (bd.getFactoryBeanName() != null) {
				IBean factoryBean = getBean(bd.getFactoryBeanName(), context);
				if (addBeanReference(BeanType.FACTORY, bean, factoryBean,
						context, references, referencedBeans)
						&& recursive) {
					addBeanReferencesForBean(factoryBean, context, recursive,
							references, referencedBeans);
				}
			}

			// Add bean's depends-on beans
			if (bd.getDependsOn() != null) {
				for (String dependsOnBeanId : bd.getDependsOn()) {
					IBean dependsOnBean = getBean(dependsOnBeanId, context);
					if (addBeanReference(BeanType.DEPENDS_ON, bean,
							dependsOnBean, context, references, referencedBeans)
							&& recursive) {
						addBeanReferencesForBean(dependsOnBean, context,
								recursive, references, referencedBeans);
					}
				}
			}

			// Add beans from bean's MethodOverrides
			if (!bd.getMethodOverrides().isEmpty()) {
				for (Object methodOverride : bd.getMethodOverrides()
						.getOverrides()) {
					if (methodOverride instanceof LookupOverride) {
						String beanName = ((LookupOverride) methodOverride)
								.getBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (addBeanReference(BeanType.METHOD_OVERRIDE, bean,
								overrideBean, context, references,
								referencedBeans)
								&& recursive) {
							addBeanReferencesForBean(overrideBean, context,
									recursive, references, referencedBeans);
						}
					}
					else if (methodOverride instanceof ReplaceOverride) {
						String beanName = ((ReplaceOverride) methodOverride)
								.getMethodReplacerBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (addBeanReference(BeanType.METHOD_OVERRIDE, bean,
								overrideBean, context, references,
								referencedBeans)
								&& recursive) {
							addBeanReferencesForBean(overrideBean, context,
									recursive, references, referencedBeans);
						}
					}
				}
			}

			// Add beans referenced from bean's constructor arguments
			for (IBeanConstructorArgument carg : bean.getConstructorArguments()) {
				addBeanReferencesForValue(carg, carg.getValue(), context,
						references, referencedBeans, recursive);
			}

			// Add referenced beans from bean's properties
			for (IBeanProperty property : bean.getProperties()) {
				addBeanReferencesForValue(property, property.getValue(),
						context, references, referencedBeans, recursive);
			}
		}
		else if (element instanceof IBeanConstructorArgument) {

			// Add referenced beans from constructor arguments element
			IBeanConstructorArgument carg = (IBeanConstructorArgument) element;
			addBeanReferencesForValue(carg, carg.getValue(), context,
					references, referencedBeans, recursive);
		}
		else if (element instanceof IBeanProperty) {

			// Add referenced beans from property element
			IBeanProperty property = (IBeanProperty) element;
			addBeanReferencesForValue(property, property.getValue(), context,
					references, referencedBeans, recursive);

		}
		else {
			throw new IllegalArgumentException("Unsupported model element "
					+ element);
		}
		return references;
	}

	/**
	 * If given target is not equal to source then a {@link BeansConnection} is
	 * created. This bean reference is added to the given list of bean
	 * references (if not already). If given target is not already checked for
	 * bean references then <code>true</code> is returned else
	 * <code>false</code>.
	 */
	private static boolean addBeanReference(BeanType type,
			IModelElement source, IBean target, IModelElement context,
			Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (target != null && target != source) {
			BeansConnection ref = new BeansConnection(type, source, target,
					context);
			if (!references.contains(ref)) {
				references.add(ref);

				// If given target not checked for references then check it too
				if (!referencedBeans.contains(target)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds the all beans which are referenced by the specified
	 * {@link IBeansComponent} to the given list as an instance of
	 * {@link BeansConnection}.
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 * @param referencedBeans used to break from cycles
	 */
	private static void addBeanReferencesForBeansComponent(
			IBeansComponent component, IModelElement context,
			boolean recursive, Set<BeansConnection> references,
			Set<IBean> referencedBeans) {
		for (IBean bean : component.getBeans()) {
			addBeanReferencesForBean(bean, context, recursive, references,
					referencedBeans);
		}
		for (IBeansComponent innerComponent : component.getComponents()) {
			addBeanReferencesForBeansComponent(innerComponent, context,
					recursive, references, referencedBeans);
		}
	}

	/**
	 * Adds the all beans which are referenced by the specified bean to the
	 * given list as an instance of {@link BeansConnection}.
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 * @param referencedBeans used to break from cycles
	 */
	private static void addBeanReferencesForBean(IBean element,
			IModelElement context, boolean recursive,
			Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (!referencedBeans.contains(element)) {

			// must add this element first to break from cycles
			referencedBeans.add(element);
			for (BeansConnection ref : getBeanReferences(element, context,
					recursive, references, referencedBeans)) {
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}
	}

	/**
	 * Given a bean property's or constructor argument's value, adds any beans
	 * referenced by this value. This value could be:
	 * <li>An {@link IBeanReference}, which bean will be added.
	 * <li>An inner {@link IBean}. This is an inner {@link IBean} that may
	 * contain {@link IBeanReference}s which will be added too.
	 * <li>An {@link IBeansList}. This is a collection that may contain
	 * {@link IBeanReference}s which will be added.
	 * <li>An {@link IBeansSet}. May also contain {@link IBeanReference}s
	 * that will be added.
	 * <li>An {@link IBeansMap}. In this case the value may be a
	 * {@link IBeanReference} that will be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the referenced beans are looked-up
	 */
	private static void addBeanReferencesForValue(IModelElement element,
			Object value, IModelElement context,
			Set<BeansConnection> references, Set<IBean> referencedBeans,
			boolean recursive) {
		if (value instanceof IBeanReference) {
			String beanName = ((IBeanReference) value).getBeanName();
			IBean bean = getBean(beanName, context);
			if (addBeanReference(BeanType.STANDARD, element, bean, context,
					references, referencedBeans)
					&& recursive) {
				addBeanReferencesForBean(bean, context, recursive, references,
						referencedBeans);
			}
		}
		else if (value instanceof IBeansList) {

			// Add bean property's interceptors
			if (element instanceof IBeanProperty
					&& element.getElementName().equals("interceptorNames")) {
				IType type = getBeanType((IBean) element.getElementParent(),
						context);
				if (type != null) {
					if (type
							.getFullyQualifiedName()
							.equals(
									"org.springframework.aop.framework.ProxyFactoryBean")) {
						for (IModelElement child : ((IBeansList) value)
								.getElementChildren()) {
							if (child instanceof IBeansTypedString) {
								IBean interceptor = getBean(
										((IBeansTypedString) child).getString(),
										context);
								if (addBeanReference(BeanType.INTERCEPTOR,
										element, interceptor, context,
										references, referencedBeans)
										&& recursive) {
									addBeanReferencesForBean(interceptor,
											context, recursive, references,
											referencedBeans);
								}
							}
						}
					}
				}
			}
			else {
				for (IModelElement child : ((IBeansList) value)
						.getElementChildren()) {
					addBeanReferencesForValue(element, child, context,
							references, referencedBeans, recursive);
				}
			}
		}
		else if (value instanceof IBeansSet) {
			for (IModelElement child : ((IBeansSet) value).getElementChildren()) {
				addBeanReferencesForValue(element, child, context, references,
						referencedBeans, recursive);
			}
		}
		else if (value instanceof IBeansMap) {
			for (IModelElement child : ((IBeansMap) value).getElementChildren()) {
				if (child instanceof IBeansMapEntry) {
					addBeanReferencesForValue(element, ((IBeansMapEntry) child)
							.getKey(), context, references, referencedBeans,
							recursive);
					addBeanReferencesForValue(element, ((IBeansMapEntry) child)
							.getValue(), context, references, referencedBeans,
							recursive);
				}
			}
		}
	}

	/**
	 * Returns the merged bean definition for a given bean from specified
	 * context (<code>IBeansConfig</code> or <code>IBeansConfigSet</code>).
	 * Any cyclic-references are ignored.
	 * @param bean the bean the merged bean definition is requested for
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the beans are looked-up
	 * @return given bean's merged bean definition
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static BeanDefinition getMergedBeanDefinition(IBean bean,
			IModelElement context) {
		BeanDefinition bd = ((Bean) bean).getBeanDefinition();
		if (bean.isChildBean()) {

			// Fill a set with all bean definitions belonging to the
			// hierarchy of the requested bean definition
			List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>(); // used
			// to
			// detect
			// a
			// cycle
			beanDefinitions.add(bd);
			addBeanDefinition(bean, context, beanDefinitions);

			// Merge the bean definition hierarchy to a single bean
			// definition
			RootBeanDefinition rbd = null;
			int bdCount = beanDefinitions.size();
			for (int i = bdCount - 1; i >= 0; i--) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) beanDefinitions
						.get(i);
				if (rbd != null) {
					rbd.overrideFrom(abd);
				}
				else {
					if (abd instanceof RootBeanDefinition) {
						rbd = new RootBeanDefinition((RootBeanDefinition) abd);
					}
					else {

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

	private static void addBeanDefinition(IBean bean, IModelElement context,
			List<BeanDefinition> beanDefinitions) {
		String parentName = bean.getParentName();
		Bean parentBean = (Bean) getBean(parentName, context);
		if (parentBean != null) {
			BeanDefinition parentBd = parentBean.getBeanDefinition();

			// Break cyclic references
			if (!parentName.equals(bean.getElementName())
					&& !beanDefinitions.contains(parentBd)) {
				beanDefinitions.add(parentBd);
				if (parentBean.isChildBean()) {
					addBeanDefinition(parentBean, context, beanDefinitions);
				}
			}
		}
	}

	/**
	 * Returns the <code>IBean</code> for a given bean name from specified
	 * context (<code>IBeansConfig</code> or <code>IBeansConfigSet</code>).
	 * If the corresponding bean is not found then the context's list of
	 * <code>IBeanAlias</code>es is checked too.
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the beans are looked-up
	 * @return <code>IBean</code> or <code>null</code> if bean not found
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static IBean getBean(String name, IModelElement context) {
		if (context instanceof IBeansConfig) {
			IBeansConfig config = (IBeansConfig) context;
			IBean bean = config.getBean(name);
			if (bean == null) {
				IBeanAlias alias = config.getAlias(name);
				if (alias != null) {
					bean = config.getBean(alias.getBeanName());
				}
			}
			return bean;
		}
		else if (context instanceof IBeansConfigSet) {
			IBeansConfigSet configSet = (IBeansConfigSet) context;
			IBean bean = configSet.getBean(name);
			if (bean == null) {
				IBeanAlias alias = configSet.getAlias(name);
				if (alias != null) {
					bean = configSet.getBean(alias.getBeanName());
				}
			}
			return bean;
		}
		else {
			throw new IllegalArgumentException("Unsupported context " + context);
		}
	}

	public static IBean getBeanWithConfigSets(String name, IBeansConfig config) {
		IBean bean = getBean(name, config);
		if (bean == null) {
			IBeansProject project = (IBeansProject) config.getElementParent();
			for (IBeansConfigSet configSet : project.getConfigSets()) {
				if (configSet.hasConfig(config.getElementName())) {
					bean = getBean(name, configSet);
					if (bean != null) {
						break;
					}
				}
			}
		}
		return bean;
	}

	/**
	 * Returns the inner {@link IBean}s of a given {@link IModelElement}.
	 */
	public static Set<IBean> getInnerBeans(final IModelElement element) {
		final Set<IBean> innerBeans = new HashSet<IBean>();
		IModelElementVisitor visitor = new IModelElementVisitor() {
			public boolean visit(IModelElement visitedElement,
					IProgressMonitor monitor) {
				if (!element.equals(visitedElement)
						&& visitedElement instanceof IBean
						&& ((IBean) visitedElement).isInnerBean()) {
					innerBeans.add((IBean) visitedElement);
				}
				return true;
			}
		};
		element.accept(visitor, new NullProgressMonitor());
		return innerBeans;
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project the JDT project the class belongs to
	 * @param className the full qualified class name of the requested Java type
	 * @return the requested Java type or null if the class is not defined or
	 * the project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null && className != null) {

			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0 && pos < (className.length() - 1)) {
				className = className.substring(0, pos) + '.'
						+ className.substring(pos + 1);
			}
			try {
				// First look for the type in the Java project
				IType type = javaProject.findType(className);
				if (type != null) {
					return type;
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					IJavaProject refJavaProject = JdtUtils
							.getJavaProject(refProject);
					if (refJavaProject != null) {
						type = refJavaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
				}

				// fall back and try to locate the class using AJDT
				return JdtUtils.getJavaType(project, className);
			}
			catch (CoreException e) {
				BeansCorePlugin.log("Error getting Java type '" + className
						+ "'", e);
			}
		}

		return null;
	}

	/**
	 * Returns the given bean's class name.
	 * @param bean the bean to lookup the bean class name for
	 * @param context the context ({@link IBeanConfig} or
	 * {@link IBeanConfigSet}) the beans are looked-up; if <code>null</code>
	 * then the bean's config will be first used; if the bean class name cannot
	 * be resolved in the bean's configs, the algorithm will look in all bean
	 * config sets that contain the bean's config
	 */
	public static String getBeanClass(IBean bean, IModelElement context) {
		Assert.notNull(bean);

		if (context == null) {
			// first use config
			context = getConfig(bean);
			String className = getBeanClassFromContext(bean, context);
			if (className != null) {
				return className;
			}
			// second use possibly config sets
			Set<IBeansConfigSet> configSets = getConfigSets(bean);
			for (IBeansConfigSet configSet : configSets) {
				className = getBeanClassFromContext(bean, configSet);
				if (className != null) {
					return className;
				}
			}
		}
		else {
			return getBeanClassFromContext(bean, context);
		}

		return null;
	}

	/**
	 * Returns the given bean's class name.
	 * @param bean the bean to lookup the bean class name for
	 * @param context the context ({@link IBeanConfig} or
	 * {@link IBeanConfigSet}) the beans are looked-up
	 * @throws IllegalArgumentException if context of bean is <code>null</code>
	 */
	public static String getBeanClassFromContext(IBean bean,
			IModelElement context) {
		Assert.notNull(bean);
		Assert.notNull(context);

		// TODO add factory-bean and factory-method to this check
		if (bean.getClassName() != null) {
			return bean.getClassName();
		}

		Set<String> beanNames = new HashSet<String>();
		do {
			beanNames.add(bean.getElementName());
			String parentName = bean.getParentName();
			if (parentName != null) {
				if (beanNames.contains(parentName)) {
					// Break cyclic references
					break;
				}
				bean = getBean(parentName, context);
				// TODO add factory-bean and factory-method to this check
				if (bean != null && bean.getClassName() != null) {
					return bean.getClassName();
				}
			}
			else {
				bean = null;
			}
		} while (bean != null);
		return null;
	}

	/**
	 * Returns the corresponding Java type for given bean's class.
	 * @param bean the bean to lookup the bean class' Java type
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the beans are looked-up; if
	 * <code>null</code> then the bean's config is used
	 * @return the Java type of given bean's class or <code>null</code> if no
	 * bean class defined or type not found
	 */
	public static IType getBeanType(IBean bean, IModelElement context) {
		Assert.notNull(bean);
		String className = getBeanClass(bean, context);
		if (className != null) {
			return getJavaType(getProject(bean).getProject(), className);
		}
		return null;
	}

	/**
	 * Returns the corresponding Java set method for given bean property.
	 * @param property the property to lookup the Java method for
	 * @param context the context (<code>IBeanConfig</code> or
	 * <code>IBeanConfigSet</code>) the beans are looked-up; if
	 * <code>null</code> then the bean's config is used
	 * @return the Java method of given bean property or <code>null</code> if
	 * no bean class defined or set method not found
	 */
	public static IMethod getPropertyMethod(IBeanProperty property,
			IModelElement context) {
		Assert.notNull(property);
		IType type = getBeanType((IBean) property.getElementParent(), context);
		if (type != null) {
			try {
				return Introspector.getWritableProperty(type, property
						.getElementName());
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Returns the first constructor argument defined for given bean.
	 * @param bean the bean to lookup the first constructor argument
	 * @return the first constructor argument or <code>null</code> if no
	 * constructor argument is defined
	 */
	public static IBeanConstructorArgument getFirstConstructorArgument(
			IBean bean) {
		IBeanConstructorArgument firstCarg = null;
		int firstCargStartLine = Integer.MAX_VALUE;
		for (IBeanConstructorArgument carg : bean.getConstructorArguments()) {
			if (carg.getElementStartLine() < firstCargStartLine) {
				firstCarg = carg;
				firstCargStartLine = carg.getElementStartLine();
			}
		}
		return firstCarg;
	}

	public static void createProblemMarker(IModelElement element,
			String message, int severity, Problem problem, ErrorCode errorCode) {
		int line;
		Object source = problem.getLocation().getSource();
		if (source instanceof XmlSourceLocation) {
			line = ((XmlSourceLocation) source).getStartLine();
		}
		else if (source instanceof Node) {
			line = LineNumberPreservingDOMParser
					.getStartLineNumber((Node) source);
		}
		else {
			line = -1;
		}
		createProblemMarker(element, message, severity, line, errorCode, null,
				null);
	}

	public static void createProblemMarker(IModelElement element,
			String message, int severity) {
		int line = (element instanceof ISourceModelElement
				? ((ISourceModelElement) element).getElementStartLine() : -1);
		createProblemMarker(element, message, severity, line, ErrorCode.NONE,
				null, null);
	}

	public static void createProblemMarker(IModelElement element,
			String message, int severity, int line, ErrorCode errorCode) {
		createProblemMarker(element, message, severity, line, errorCode, null,
				null);
	}

	public static void createProblemMarker(IModelElement element,
			String message, int severity, int line, ErrorCode errorCode,
			String beanID, String errorData) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			BeansCoreUtils.createProblemMarker(resource, message, severity,
					line, errorCode, beanID, errorData);
		}
	}

	public static void deleteProblemMarkers(IModelElement element) {
		if (element instanceof IBeansProject) {
			for (IBeansConfig config : ((IBeansProject) element).getConfigs()) {
				ModelUtils.deleteProblemMarkers(config);
			}
		}
		else {
			ModelUtils.deleteProblemMarkers(element);
		}
	}

	/**
	 * Registers all bean definitions and aliases from given
	 * {@link IBeansConfig} in specified {@link BeanDefinitionRegistry}. All
	 * {@link BeansException}s thrown by the {@link BeanDefinitionRegistry} are
	 * ignored.
	 */
	public static void registerBeanConfig(IBeansConfig config,
			BeanDefinitionRegistry registry) {
		// Register bean definitions
		for (IBean bean : config.getBeans()) {
			try {
				String beanName = bean.getElementName();

				// Register bean definition under primary name.
				registry.registerBeanDefinition(beanName, ((Bean) bean)
						.getBeanDefinition());

				// Register aliases for bean name, if any.
				String[] aliases = bean.getAliases();
				if (aliases != null) {
					for (String alias : aliases) {
						registry.registerAlias(beanName, alias);
					}
				}
			}
			catch (BeansException e) {
				// ignore - continue with next bean
			}
		}

		// Register bean aliases
		for (IBeanAlias alias : config.getAliases()) {
			try {
				registry.registerAlias(alias.getBeanName(), alias
						.getElementName());
			}
			catch (BeansException e) {
				// ignore - continue with next alias
			}
		}
	}

	/**
	 * Returns the child of given parent element's subtree the specified element
	 * belongs to. If the given element does not belong to the subtree of the
	 * specified parent element <code>null</code> is returned.
	 */
	public static IModelElement getChildForElement(IModelElement parent,
			IModelElement element) {
		while (element != null) {
			IModelElement elementParent = element.getElementParent();
			if (parent.equals(elementParent)) {
				return element;
			}
			element = elementParent;
		}
		return null;
	}

	/**
	 * Returns the beans config for a given ZIP file entry.
	 */
	public static IBeansConfig getConfig(ZipEntryStorage storage) {
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				storage.getFile().getProject());
		if (project != null) {
			return project.getConfig(storage.getFullName());
		}
		return null;
	}

	/**
	 * Returns the {@link IResourceModelElement} for a given object.
	 */
	public static IResourceModelElement getResourceModelElement(Object obj) {
		if (obj instanceof IFile) {
			return BeansCorePlugin.getModel().getConfig((IFile) obj);
		}
		else if (obj instanceof IProject) {
			return BeansCorePlugin.getModel().getProject((IProject) obj);
		}
		else if (obj instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) obj)
					.getAdapter(IResource.class);
			if (resource instanceof IFile) {
				return BeansCorePlugin.getModel().getConfig((IFile) resource);
			}
			else if (resource instanceof IProject) {
				return BeansCorePlugin.getModel().getConfig((IFile) obj);
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IFile} for a given {@link IModelElement}.
	 */
	public static IFile getFile(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			if (resource instanceof IFile) {
				return (IFile) resource;
			}
		}
		return null;
	}

	public static IModelElement getModelElement(Element element,
			IModelElement context) {
		Node parent = element.getParentNode();
		if (BeansTags.isTag(element, Tag.BEAN)
				&& BeansTags.isTag(parent, Tag.BEANS)) {
			String beanName = getBeanName(element);
			if (beanName != null) {
				return BeansModelUtils.getBean(beanName, context);
			}
		}
		else if (BeansTags.isTag(element, Tag.PROPERTY)
				&& BeansTags.isTag(parent, Tag.BEAN)
				&& BeansTags.isTag(parent.getParentNode(), Tag.BEANS)) {
			String beanName = getBeanName((Element) parent);
			if (beanName != null) {
				IBean bean = BeansModelUtils.getBean(beanName, context);
				if (bean != null) {
					Node nameAttribute = element.getAttributeNode("name");
					if (nameAttribute != null
							&& nameAttribute.getNodeValue() != null) {
						return bean.getProperty(nameAttribute.getNodeValue());
					}
					return bean;
				}
			}
		}
		return null;
	}

	private static String getBeanName(Element element) {
		Node idAttribute = element.getAttributeNode("id");
		if (idAttribute != null && idAttribute.getNodeValue() != null) {
			return idAttribute.getNodeValue();
		}
		Node nameAttribute = element.getAttributeNode("name");
		if (nameAttribute != null && nameAttribute.getNodeValue() != null) {
			return nameAttribute.getNodeValue();
		}
		return null;
	}

	/**
	 * Returns a string representation of the given value object.
	 */
	public static String getValueName(Object value) {

		// Ignore bean name of inner beans
		if (value instanceof BeanDefinitionHolder) {
			value = ((BeanDefinitionHolder) value).getBeanDefinition();
		}

		StringBuffer name = new StringBuffer();
		if (value instanceof String) {
			name.append('"').append(value).append('"');
		}
		else if (value instanceof BeanDefinition) {
			name.append("bean ");
			if (value instanceof RootBeanDefinition) {
				name.append('[');
				name.append(((RootBeanDefinition) value).getBeanClassName());
				name.append(']');
			}
			else {
				name.append('<');
				name.append(((ChildBeanDefinition) value).getParentName());
				name.append('>');
			}
		}
		else if (value instanceof RuntimeBeanReference) {
			name.append("reference ");
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			name.append('<').append(beanName).append(">");
		}
		else {
			String valueName;
			if (value.getClass().isArray()) {
				valueName = "["
						+ StringUtils.arrayToDelimitedString((Object[]) value,
								", ") + "]";
			}
			else {
				valueName = value.toString();
			}
			if (valueName.length() > 30) {
				name.append(valueName.substring(0, 12)).append(" .. ").append(
						valueName.substring(valueName.length() - 13));
			}
			else {
				name.append(valueName);
			}
		}
		return name.toString();
	}

	public static Object resolveValueIfNecessary(ISourceModelElement parent,
			Object value) {
		if (value instanceof IModelElement) {
			return value;
		}
		else if (value instanceof BeanDefinitionHolder) {
			return new Bean(parent, (BeanDefinitionHolder) value);
		}
		else if (value instanceof BeanDefinition) {
			return new Bean(parent, "(inner bean)", null,
					(BeanDefinition) value);
		}
		else if (value instanceof RuntimeBeanNameReference) {
			return new BeanReference(parent, (RuntimeBeanNameReference) value);
		}
		else if (value instanceof RuntimeBeanReference) {
			return new BeanReference(parent, (RuntimeBeanReference) value);
		}
		else if (value instanceof ManagedList) {
			return new BeansList(parent, (ManagedList) value);
		}
		else if (value instanceof ManagedSet) {
			return new BeansSet(parent, (ManagedSet) value);
		}
		else if (value instanceof ManagedMap) {
			return new BeansMap(parent, (ManagedMap) value);
		}
		else if (value instanceof ManagedProperties) {
			return new BeansProperties(parent, (ManagedProperties) value);
		}
		else if (value instanceof TypedStringValue) {
			return new BeansTypedString(parent, (TypedStringValue) value);
		}
		else if (value.getClass().isArray()) {
			return new BeansTypedString(parent, "["
					+ StringUtils
							.arrayToDelimitedString((Object[]) value, ", ")
					+ "]");
		}
		return new BeansTypedString(parent, value.toString());
	}
	
	/**
	 * Checks if a given <code>className</code> is used as a bean class. The
	 * check iterates the complete {@link IBeansModel} and not "only" the current
	 * {@link IBeansProject}.
	 * @param className
	 * @return
	 */
	public static boolean isBeanClass(String className) {
		Set<IBeansConfig> beans = BeansCorePlugin.getModel().getConfigs(
				className);
		return beans != null && beans.size() > 0;
	}

	public static boolean isInnerBean(IBean bean) {
		return !(bean.getElementParent() instanceof IBeansConfig);
	}

}
