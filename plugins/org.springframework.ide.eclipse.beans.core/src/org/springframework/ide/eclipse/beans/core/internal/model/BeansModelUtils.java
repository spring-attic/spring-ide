/*******************************************************************************
 * Copyright (c) 2005, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
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
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.SuperTypeHierarchyCache;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyEngine;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper methods for working with the BeansCoreModel.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public abstract class BeansModelUtils {

	/**
	 * Returns the <code>IBean</code> for a given bean name from specified context ( <code>IBeansConfig</code> or
	 * <code>IBeansConfigSet</code>). If the corresponding bean is not found then the context's list of
	 * <code>IBeanAlias</code>es is checked too.
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the beans are looked-up
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
			if (bean == null) {
				for (IBeansComponent component : config.getComponents()) {
					bean = getBean(name, component);
					if (bean != null) {
						return bean;
					}
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
			if (bean == null) {
				for (IBeansComponent component : configSet.getComponents()) {
					bean = getBean(name, component);
					if (bean != null) {
						return bean;
					}
				}
			}
			return bean;
		}
		else {
			throw new IllegalArgumentException("Unsupported context " + context);
		}
	}

	/**
	 * Return's the {@link IBean} for the given name by recursively looking into the {@link IBeansComponent}.
	 */
	private static IBean getBean(String name, IBeansComponent component) {
		for (IBean componentBean : component.getBeans()) {
			if (componentBean.getElementName().equals(name)) {
				return componentBean;
			}
		}
		for (IBeansComponent nestedComponent : component.getComponents()) {
			IBean bean = getBean(name, nestedComponent);
			if (bean != null) {
				return bean;
			}
		}
		return null;
	}

	/**
	 * Returns the given bean's class name.
	 * @param bean the bean to lookup the bean class name for
	 * @param context the context ({@link IBeanConfig} or {@link IBeanConfigSet}) the beans are looked-up; if
	 * <code>null</code> then the bean's config will be first used; if the bean class name cannot be resolved in the
	 * bean's configs, the algorithm will look in all bean config sets that contain the bean's config
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
	 * @param context the context ({@link IBeanConfig} or {@link IBeanConfigSet}) the beans are looked-up
	 * @throws IllegalArgumentException if context of bean is <code>null</code>
	 */
	public static String getBeanClassFromContext(IBean bean, IModelElement context) {
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
	 * Returns a collection of {@link BeansConnection}s holding all {@link IBean<}s which are referenced from given
	 * model element. For a bean it's parent bean (for child beans only), constructor argument values and property
	 * values are checked. {@link IBean} look-up is done from the specified {@link IBeanConfig} or
	 * {@link IBeanConfigSet}.
	 * @param element the element ({@link IBean}, {@link IBeanConstructorArgument} or {@link IBeanProperty}) to get all
	 * referenced beans from
	 * @param context the context ({@link IBeanConfig} or {@link IBeanConfigSet}) the referenced beans are looked-up
	 * @param recursive set to <code>true</code> if the dependency graph is traversed recursively
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<BeansConnection> getBeanReferences(IModelElement element, IModelElement context, boolean recursive) {
		Set<BeansConnection> references = new LinkedHashSet<BeansConnection>();
		Set<IBean> referencedBeans = new HashSet<IBean>(); // used to break
		// from cycles
		return getBeanReferences(element, context, recursive, references, referencedBeans);
	}

	/**
	 * Returns a list of all beans which belong to the given model element.
	 * @param element the model element which contains beans
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<IBean> getBeans(IModelElement element) {
		return getBeans(element, null);
	}

	/**
	 * Returns a list of all beans which belong to the given model element.
	 * @param element the model element which contains beans
	 * @param monitor the progress monitor to indicate progress; mark the monitor done after completing the work
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<IBean> getBeans(IModelElement element, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		final Set<IBean> beans = new LinkedHashSet<IBean>();
		if (element instanceof IBeansModel) {
			Set<IBeansProject> projects = ((IBeansModel) element).getProjects();
			monitor.beginTask("Locating bean definitions", projects.size());
			try {
				for (IBeansProject project : projects) {
					monitor.subTask("Locating bean definitions in project '" + project.getElementName() + "'");
					for (IBeansConfig config : project.getConfigs()) {
						monitor.subTask("Locating bean defintions from file '" + config.getElementName() + "'");
						
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}

						config.accept(new IModelElementVisitor() {
							
							public boolean visit(IModelElement element, IProgressMonitor monitor) {
								if (element instanceof IBean) {
									beans.add((IBean) element);
								}
								return !monitor.isCanceled();
							}
						}, new NullProgressMonitor());
						
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					monitor.worked(1);
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
			monitor.beginTask("Locating bean definitions", configs.size());
			try {
				for (IBeansConfig config : configs) {
					monitor.subTask("Loading bean defintion from file '" + config.getElementName() + "'");
					beans.addAll(config.getBeans());
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					for (IBeansComponent component : config.getComponents()) {
						beans.addAll(component.getBeans());
					}
					monitor.worked(1);
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
			for (IBeansComponent component : ((IBeansConfig) element).getComponents()) {
				beans.addAll(getBeans(component, monitor));
			}
		}
		else if (element instanceof IBeansConfigSet) {
			beans.addAll(((IBeansConfigSet) element).getBeans());
			for (IBeansComponent component : ((IBeansConfigSet) element).getComponents()) {
				beans.addAll(getBeans(component, monitor));
			}
		}
		else if (element instanceof IBeansComponent) {
			for (IBeansComponent component : ((IBeansComponent) element).getComponents()) {
				beans.addAll(getBeans(component, monitor));
			}
			beans.addAll(((IBeansComponent) element).getBeans());
		}
		else if (element instanceof IBean) {
			beans.add((IBean) element);
		}
		else {
			throw new IllegalArgumentException("Unsupported model element " + element);
		}
		return beans;
	}

	/**
	 * Returns the corresponding Java type for given bean's class.
	 * @param bean the bean to lookup the bean class' Java type
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the beans are looked-up; if
	 * <code>null</code> then the bean's config is used
	 * @return the Java type of given bean's class or <code>null</code> if no bean class defined or type not found
	 */
	public static IType getBeanType(IBean bean, IModelElement context) {
		Assert.notNull(bean);
		String className = getBeanClass(bean, context);
		if (className != null) {
			return JdtUtils.getJavaType(getProject(bean).getProject(), className);
		}
		return null;
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
	 * Returns the child of given parent element's subtree the specified element belongs to. If the given element does
	 * not belong to the subtree of the specified parent element <code>null</code> is returned.
	 */
	public static IModelElement getChildForElement(IModelElement parent, IModelElement element) {
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
	 * Returns the {@link IBeanConfig} the given {@link IModelElement} belongs to.
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
		throw new IllegalArgumentException("Unsupported model element " + element);
	}

	/**
	 * Returns config for given name from specified context (<code>IBeansProject</code> or <code>IBeansConfigSet</code>
	 * ). Externally referenced configs (config name starts with '/') are recognized too.
	 * @param configName the name of the config to look for
	 * @param context the context used for config look-up
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static IBeansConfig getConfig(String configName, IModelElement context) {
		// For external project get the corresponding project from beans model
		if (configName.charAt(0) == IBeansConfigSet.EXTERNAL_CONFIG_NAME_PREFIX) {
			// Extract project and config name from full qualified config name
			int pos = configName.indexOf('/', 1);
			String projectName = configName.substring(1, pos);
			configName = configName.substring(pos + 1);
			IBeansProject project = BeansCorePlugin.getModel().getProject(projectName);
			if (project != null) {
				return project.getConfig(configName);
			}
		}
		else if (context instanceof IBeansProject) {
			return ((IBeansProject) context).getConfig(configName);
		}
		else if (context instanceof IBeansConfigSet) {
			return ((IBeansProject) context.getElementParent()).getConfig(configName);
		}
		return null;
	}

	/**
	 * Returns the beans config for a given ZIP file entry.
	 */
	public static IBeansConfig getConfig(ZipEntryStorage storage) {
		IResourceModelElement parent = (IResourceModelElement) storage.getAdapter(IResourceModelElement.class);
		if (parent instanceof IBeansConfig) {
			return (IBeansConfig) parent;
		}

		IBeansProject project = BeansCorePlugin.getModel().getProject(storage.getFile().getProject());
		if (project != null) {
			return project.getConfig(storage.getFullName());
		}
		return null;
	}

	/**
	 * Returns all {@link IBeansConfigSet} the given {@link IModelElement} belongs to.
	 * @param element the model element to get the beans config for
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static Set<IBeansConfigSet> getConfigSets(IModelElement element) {
		Set<IBeansConfigSet> configSets = new LinkedHashSet<IBeansConfigSet>();
		if (element instanceof IBeansConfigSet) {
			configSets.add((IBeansConfigSet) element);
		}
		else if (element instanceof IBeansConfig) {
			for (IBeansProject beansProject : BeansCorePlugin.getModel().getProjects()) {
				Set<IBeansConfigSet> css = beansProject.getConfigSets();
				for (IBeansConfigSet cs : css) {
					if (cs.getConfigs().contains(element)) {
						configSets.add(cs);
					}
				}
			}
		}
		else if (element instanceof ISourceModelElement) {
			IBeansConfig bc = getConfig(element);
			for (IBeansProject beansProject : BeansCorePlugin.getModel().getProjects()) {
				Set<IBeansConfigSet> css = beansProject.getConfigSets();
				for (IBeansConfigSet cs : css) {
					if (cs.getConfigs().contains(bc)) {
						configSets.add(cs);
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported model element " + element);
		}
		return configSets;
	}

	/**
	 * Returns the {@link IFile} for a given {@link IModelElement}.
	 */
	public static IFile getFile(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element).getElementResource();
			if (resource instanceof IFile) {
				return (IFile) resource;
			}
		}
		return null;
	}

	/**
	 * Returns the first constructor argument defined for given bean.
	 * @param bean the bean to lookup the first constructor argument
	 * @return the first constructor argument or <code>null</code> if no constructor argument is defined
	 */
	public static IBeanConstructorArgument getFirstConstructorArgument(IBean bean) {
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

	/**
	 * Returns the importing {@link IBeansConfig} from a {@link IImportedBeansConfig}.
	 * <p>
	 * <code>null</code> can be returned if the given {@link IBeansConfig} is not imported.
	 * @since 2.0.4
	 */
	public static IBeansConfig getImportingBeansConfig(IBeansConfig beansConfig) {
		if (beansConfig instanceof IImportedBeansConfig) {
			// navigate up the model tree
			return (IBeansConfig) beansConfig.getElementParent().getElementParent();
		}
		return null;
	}

	/**
	 * Returns the inner {@link IBean}s of a given {@link IModelElement}.
	 * @since 2.1.0
	 */
	public static Set<IBean> getInnerBeans(final IModelElement element, final boolean recursive) {
		final Set<IBean> innerBeans = new HashSet<IBean>();
		IModelElementVisitor visitor = new IModelElementVisitor() {
			public boolean visit(IModelElement visitedElement, IProgressMonitor monitor) {
				if (!element.equals(visitedElement) && visitedElement instanceof IBean
						&& ((IBean) visitedElement).isInnerBean()) {
					innerBeans.add((IBean) visitedElement);
					return recursive;
				}
				return true;
			}
		};
		element.accept(visitor, new NullProgressMonitor());
		return innerBeans;
	}

	/**
	 * Returns the inner {@link IBean}s of a given {@link IModelElement}.
	 */
	public static Set<IBean> getInnerBeans(final IModelElement element) {
		return getInnerBeans(element, true);
	}

	/**
	 * Returns the merged bean definition for a given bean from specified context ( {@link IBeansConfig} or
	 * {@link IBeansConfigSet}). Any cyclic-references are ignored.
	 * @param bean the bean the merged bean definition is requested for
	 * @param context the context ({@link IBeanConfig} or {@link IBeanConfigSet}) the beans are looked-up
	 * @return given bean's merged bean definition
	 * @throws IllegalArgumentException if unsupported context specified
	 */
	public static BeanDefinition getMergedBeanDefinition(IBean bean, IModelElement context) {
		BeanDefinition bd = ((Bean) bean).getBeanDefinition();
		if (bean.isChildBean()) {

			// If no context specified the use the bean's config instead
			if (context == null) {
				context = BeansModelUtils.getConfig(bean);
			}

			// Fill a set with all bean definitions belonging to the
			// hierarchy of the requested bean definition
			List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
			// used to detect a cycle
			beanDefinitions.add(bd);
			addBeanDefinition(bean, context, beanDefinitions);

			// Merge the bean definition hierarchy to a single bean
			// definition
			AbstractBeanDefinition rbd = null;
			int bdCount = beanDefinitions.size();
			for (int i = bdCount - 1; i >= 0; i--) {
				BeanDefinition abd = beanDefinitions.get(i);
				if (rbd != null) {
					rbd.overrideFrom(abd);
				}
				else {
					if (abd instanceof RootBeanDefinition) {
						rbd = new RootBeanDefinition((RootBeanDefinition) abd);
					}
					else if (abd instanceof GenericBeanDefinition) {
						rbd = new GenericBeanDefinition(abd);
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

	public static IModelElement getModelElement(Element element, IModelElement context) {
		Node parent = element.getParentNode();
		if (BeansTags.isTag(element, Tag.BEAN) && BeansTags.isTag(parent, Tag.BEANS)) {
			String beanName = getBeanName(element);
			if (beanName != null) {
				return BeansModelUtils.getBean(beanName, context);
			}
		}
		else if (BeansTags.isTag(element, Tag.PROPERTY) && BeansTags.isTag(parent, Tag.BEAN)
				&& BeansTags.isTag(parent.getParentNode(), Tag.BEANS)) {
			String beanName = getBeanName((Element) parent);
			if (beanName != null) {
				IBean bean = BeansModelUtils.getBean(beanName, context);
				if (bean != null) {
					Node nameAttribute = element.getAttributeNode("name");
					if (nameAttribute != null && nameAttribute.getNodeValue() != null) {
						return bean.getProperty(nameAttribute.getNodeValue());
					}
					return bean;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the most specific {@link IModelElement} that corresponds to the given <code>startLine</code> and
	 * <code>endLine</code> line numbers.
	 * <p>
	 * Client should be aware of possible <code>null</code> returns in case on {@link IModelElement} can be found at the
	 * given location.
	 * @since 2.0.1
	 */
	public static IModelElement getMostSpecificModelElement(int startLine, int endLine, IFile resource,
			IProgressMonitor monitor) {
		if (BeansCoreUtils.isBeansConfig(resource, true)) {

			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(resource, true);
			ModelElementDetermingModelVisitor v = new ModelElementDetermingModelVisitor(startLine, endLine, resource);
			beansConfig.accept(v, monitor);
			return v.getElement();
		}
		return null;
	}

	/**
	 * Iterates up the model tree to find the first parent element that is of the given <code>parentType</code>.
	 * @since 2.0.4
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getParentOfClass(IModelElement child, Class<T> parentType) {
		if (child != null) {
			IModelElement parent = child.getElementParent();
			while (parent != null) {
				if (parentType.isAssignableFrom(parent.getClass())) {
					return (T) parent;
				}
				parent = parent.getElementParent();
			}
		}
		return null;
	}

	/**
	 * Returns the <code>IBeansProject</code> the given model element belongs to.
	 * @param element the model element to get the beans project for
	 * @throws IllegalArgumentException if unsupported model element specified
	 */
	public static IBeansProject getProject(IModelElement element) {
		IBeansProject project = getParentOfClass(element, IBeansProject.class);
		if (project != null) {
			return project;
		}
		
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element).getElementResource();
			if (resource != null) {
				project = BeansCorePlugin.getModel().getProject(resource.getProject());
				if (project != null) {
					return project;
				}
			}
		}

		throw new IllegalArgumentException("Unsupported model element " + element);
	}

	/**
	 * Returns the corresponding Java set method for given bean property.
	 * @param property the property to lookup the Java method for
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the beans are looked-up; if
	 * <code>null</code> then the bean's config is used
	 * @return the Java method of given bean property or <code>null</code> if no bean class defined or set method not
	 * found
	 */
	public static IMethod getPropertyMethod(IBeanProperty property, IModelElement context) {
		Assert.notNull(property);
		IType type = getBeanType((IBean) property.getElementParent(), context);
		if (type != null) {
			try {
				return Introspector.getWritableProperty(type, property.getElementName());
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
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
			IResource resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
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
				name.append(((BeanDefinition) value).getParentName());
				name.append('>');
			}
		}
		else if (value instanceof RuntimeBeanReference) {
			name.append("reference ");
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			name.append('<').append(beanName).append(">");
		}
		else if (value != null) {
			String valueName = null;
			if (value.getClass().isArray()) {
				valueName = "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]";
			}
			else {
				valueName = value.toString();
			}
			if (valueName.length() > 30) {
				name.append(valueName.substring(0, 12)).append(" .. ")
						.append(valueName.substring(valueName.length() - 13));
			}
			else {
				name.append(valueName);
			}
		}
		else {
			name.append("<null>");
		}
		return name.toString();
	}

	/**
	 * Checks if a given <code>className</code> is used as a bean class. The check iterates the complete
	 * {@link IBeansModel} and not "only" the current {@link IBeansProject}.
	 * @param className
	 */
	public static boolean isBeanClass(String className) {
		Set<IBeansConfig> beans = BeansCorePlugin.getModel().getConfigs(className);
		return beans != null && beans.size() > 0;
	}

	/**
	 * Checks if a given <code>type</code> is used as a bean class. The check iterates the complete {@link IBeansModel}
	 * and not "only" the current {@link IBeansProject}.
	 * <p>
	 * The implementation checks if the given <code>type</code> is on the project's classpath.
	 * @param type
	 * @since 2.2.1
	 */
	public static boolean isBeanClass(IType type) {
		for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
			if (javaProject != null && javaProject.isOnClasspath(type)) {
				for (IBeansConfig config : project.getConfigs()) {
					if (config.isBeanClass(type.getFullyQualifiedName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isInnerBean(IBean bean) {
		return !(bean.getElementParent() instanceof IBeansConfig);
	}

	/**
	 * Registers all bean definitions and aliases from given {@link IBeansConfig} in specified
	 * {@link BeanDefinitionRegistry}. All {@link BeansException}s thrown by the {@link BeanDefinitionRegistry} are
	 * ignored.
	 */
	public static void register(IBeansConfigSet configSet, IBeansConfig config, BeanDefinitionRegistry registry) {

		// Register bean aliases
		for (IBeanAlias alias : config.getAliases()) {
			try {
				registry.registerAlias(alias.getBeanName(), alias.getElementName());
			}
			catch (BeansException e) {
				// ignore - continue with next alias
			}
		}

		// Register root bean definitions
		for (IBean bean : config.getBeans()) {
			String beanName = bean.getElementName();

			// Register bean definition under primary name
			try {
				registry.registerBeanDefinition(beanName, ((Bean) bean).getBeanDefinition());
			}
			catch (BeansException e) {
				// ignore - continue with next bean
			}

			// Register aliases for bean name, if any
			String[] aliases = bean.getAliases();
			if (aliases != null) {
				for (String alias : aliases) {
					try {
						registry.registerAlias(beanName, alias);
					}
					catch (BeansException e) {
						// ignore - continue with next bean
					}
				}
			}
		}

		// Register bean definitions from components
		registerComponents(configSet, config.getComponents(), registry);
	}

	public static Object resolveValueIfNecessary(ISourceModelElement parent, Object value) {
		if (value instanceof IModelElement) {
			return value;
		}
		else if (value instanceof BeanDefinitionHolder) {
			return new Bean(parent, (BeanDefinitionHolder) value);
		}
		else if (value instanceof BeanDefinition) {
			return new Bean(parent, "(inner bean)", null, (BeanDefinition) value);
		}
		else if (value instanceof RuntimeBeanNameReference) {
			return new BeanReference(parent, (RuntimeBeanNameReference) value);
		}
		else if (value instanceof RuntimeBeanReference) {
			return new BeanReference(parent, (RuntimeBeanReference) value);
		}
		else if (value instanceof ManagedList) {
			return new BeansList(parent, (ManagedList<?>) value);
		}
		else if (value instanceof ManagedSet) {
			return new BeansSet(parent, (ManagedSet<?>) value);
		}
		else if (value instanceof ManagedMap) {
			return new BeansMap(parent, (ManagedMap<?, ?>) value);
		}
		else if (value instanceof ManagedProperties) {
			return new BeansProperties(parent, (ManagedProperties) value);
		}
		else if (value instanceof TypedStringValue) {
			return new BeansTypedString(parent, (TypedStringValue) value);
		}
		else if (value != null && value.getClass().isArray()) {
			return new BeansTypedString(parent, "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]");
		}
		return new BeansTypedString(parent, (value != null ? value.toString() : "null"));
	}

	private static void addBeanDefinition(IBean bean, IModelElement context, List<BeanDefinition> beanDefinitions) {
		String parentName = bean.getParentName();
		Bean parentBean = (Bean) getBean(parentName, context);
		if (parentBean != null) {
			BeanDefinition parentBd = parentBean.getBeanDefinition();

			// Break cyclic references
			if (!parentName.equals(bean.getElementName()) && !beanDefinitions.contains(parentBd)) {
				beanDefinitions.add(parentBd);
				if (parentBean.isChildBean()) {
					addBeanDefinition(parentBean, context, beanDefinitions);
				}
			}
		}
	}

	/**
	 * If given target is not equal to source then a {@link BeansConnection} is created. This bean reference is added to
	 * the given list of bean references (if not already). If given target is not already checked for bean references
	 * then <code>true</code> is returned else <code>false</code>.
	 */
	private static boolean addBeanReference(BeanType type, IModelElement source, IBean target, IModelElement context,
			Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (target != null && target != source) {
			BeansConnection ref = new BeansConnection(type, source, target, context);
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
	 * Adds the all beans which are referenced by the specified bean to the given list as an instance of
	 * {@link BeansConnection}.
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the referenced beans are
	 * looked-up
	 * @param referencedBeans used to break from cycles
	 */
	private static void addBeanReferencesForBean(IBean element, IModelElement context, boolean recursive,
			Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (!referencedBeans.contains(element)) {

			// must add this element first to break from cycles
			referencedBeans.add(element);
			for (BeansConnection ref : getBeanReferences(element, context, recursive, references, referencedBeans)) {
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}
	}

	/**
	 * Adds the all beans which are referenced by the specified {@link IBeansComponent} to the given list as an instance
	 * of {@link BeansConnection}.
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the referenced beans are
	 * looked-up
	 * @param referencedBeans used to break from cycles
	 */
	private static void addBeanReferencesForBeansComponent(IBeansComponent component, IModelElement context,
			boolean recursive, Set<BeansConnection> references, Set<IBean> referencedBeans) {
		for (IBean bean : component.getBeans()) {
			addBeanReferencesForBean(bean, context, recursive, references, referencedBeans);
		}
		for (IBeansComponent innerComponent : component.getComponents()) {
			addBeanReferencesForBeansComponent(innerComponent, context, recursive, references, referencedBeans);
		}
	}

	/**
	 * Given a bean property's or constructor argument's value, adds any beans referenced by this value. This value
	 * could be: <li>An {@link IBeanReference}, which bean will be added. <li>An inner {@link IBean}. This is an inner
	 * {@link IBean} that may contain {@link IBeanReference}s which will be added too. <li>An {@link IBeansList}. This
	 * is a collection that may contain {@link IBeanReference}s which will be added. <li>An {@link IBeansSet}. May also
	 * contain {@link IBeanReference}s that will be added. <li>An {@link IBeansMap}. In this case the value may be a
	 * {@link IBeanReference} that will be added. <li>An ordinary object or null, in which case it's ignored.
	 * @param context the context (<code>IBeanConfig</code> or <code>IBeanConfigSet</code>) the referenced beans are
	 * looked-up
	 */
	private static void addBeanReferencesForValue(IModelElement element, Object value, IModelElement context,
			Set<BeansConnection> references, Set<IBean> referencedBeans, boolean recursive) {
		if (value instanceof IBeanReference) {
			String beanName = ((IBeanReference) value).getBeanName();
			IBean bean = getBean(beanName, context);
			if (addBeanReference(BeanType.STANDARD, element, bean, context, references, referencedBeans) && recursive) {
				addBeanReferencesForBean(bean, context, recursive, references, referencedBeans);
			}
		}
		else if (value instanceof IBeansList) {

			// Add bean property's interceptors
			if (element instanceof IBeanProperty && element.getElementName().equals("interceptorNames")) {
				IType type = getBeanType((IBean) element.getElementParent(), context);
				if (type != null) {
					if (type.getFullyQualifiedName().equals("org.springframework.aop.framework.ProxyFactoryBean")) {
						for (IModelElement child : ((IBeansList) value).getElementChildren()) {
							if (child instanceof IBeansTypedString) {
								IBean interceptor = getBean(((IBeansTypedString) child).getString(), context);
								if (addBeanReference(BeanType.INTERCEPTOR, element, interceptor, context, references,
										referencedBeans) && recursive) {
									addBeanReferencesForBean(interceptor, context, recursive, references,
											referencedBeans);
								}
							}
						}
					}
				}
			}
			else {
				for (IModelElement child : ((IBeansList) value).getElementChildren()) {
					addBeanReferencesForValue(element, child, context, references, referencedBeans, recursive);
				}
			}
		}
		else if (value instanceof IBeansSet) {
			for (IModelElement child : ((IBeansSet) value).getElementChildren()) {
				addBeanReferencesForValue(element, child, context, references, referencedBeans, recursive);
			}
		}
		else if (value instanceof IBeansMap) {
			for (IModelElement child : ((IBeansMap) value).getElementChildren()) {
				if (child instanceof IBeansMapEntry) {
					addBeanReferencesForValue(element, ((IBeansMapEntry) child).getKey(), context, references,
							referencedBeans, recursive);
					addBeanReferencesForValue(element, ((IBeansMapEntry) child).getValue(), context, references,
							referencedBeans, recursive);
				}
			}
		}
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

	private static Set<BeansConnection> getBeanReferences(IModelElement element, IModelElement context,
			boolean recursive, Set<BeansConnection> references, Set<IBean> referencedBeans) {
		if (element instanceof IBeansComponent) {
			addBeanReferencesForBeansComponent((IBeansComponent) element, context, recursive, references,
					referencedBeans);
		}
		else if (element instanceof Bean) {

			// Add referenced beans from bean element
			Bean bean = (Bean) element;

			// For a child bean add the parent bean
			if (bean.isChildBean()) {
				IBean parentBean = getBean(bean.getParentName(), context);
				if (addBeanReference(BeanType.PARENT, bean, parentBean, context, references, referencedBeans)
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
						if (addBeanReference(BeanType.PARENT, bean, parentBean, context, references, referencedBeans)
								&& recursive) {
							addBeanReferencesForBean(parentBean, context, recursive, references, referencedBeans);
						}
					}
				}
			}

			// Get bean's merged or standard bean definition
			AbstractBeanDefinition bd;
			if (recursive) {
				bd = (AbstractBeanDefinition) getMergedBeanDefinition(bean, context);
			}
			else {
				bd = (AbstractBeanDefinition) (bean).getBeanDefinition();
			}

			// Add bean's factoy bean
			if (bd.getFactoryBeanName() != null) {
				IBean factoryBean = getBean(bd.getFactoryBeanName(), context);
				if (addBeanReference(BeanType.FACTORY, bean, factoryBean, context, references, referencedBeans)
						&& recursive) {
					addBeanReferencesForBean(factoryBean, context, recursive, references, referencedBeans);
				}
			}

			// Add bean's depends-on beans
			if (bd.getDependsOn() != null) {
				for (String dependsOnBeanId : bd.getDependsOn()) {
					IBean dependsOnBean = getBean(dependsOnBeanId, context);
					if (addBeanReference(BeanType.DEPENDS_ON, bean, dependsOnBean, context, references, referencedBeans)
							&& recursive) {
						addBeanReferencesForBean(dependsOnBean, context, recursive, references, referencedBeans);
					}
				}
			}

			// Add beans from bean's MethodOverrides
			if (!bd.getMethodOverrides().isEmpty()) {
				for (Object methodOverride : bd.getMethodOverrides().getOverrides()) {
					if (methodOverride instanceof LookupOverride) {
						String beanName = ((LookupOverride) methodOverride).getBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (addBeanReference(BeanType.METHOD_OVERRIDE, bean, overrideBean, context, references,
								referencedBeans) && recursive) {
							addBeanReferencesForBean(overrideBean, context, recursive, references, referencedBeans);
						}
					}
					else if (methodOverride instanceof ReplaceOverride) {
						String beanName = ((ReplaceOverride) methodOverride).getMethodReplacerBeanName();
						IBean overrideBean = getBean(beanName, context);
						if (addBeanReference(BeanType.METHOD_OVERRIDE, bean, overrideBean, context, references,
								referencedBeans) && recursive) {
							addBeanReferencesForBean(overrideBean, context, recursive, references, referencedBeans);
						}
					}
				}
			}

			// Add beans referenced from bean's constructor arguments
			for (IBeanConstructorArgument carg : bean.getConstructorArguments()) {
				addBeanReferencesForValue(carg, carg.getValue(), context, references, referencedBeans, recursive);
			}

			// Add referenced beans from bean's properties
			for (IBeanProperty property : bean.getProperties()) {
				addBeanReferencesForValue(property, property.getValue(), context, references, referencedBeans,
						recursive);
			}

			// Add referenced beans from bean annotations contributed into the bean meta data model
			// for (IBeanProperty property : BeansCorePlugin.getMetadataModel().getBeanProperties(bean)) {
			// addBeanReferencesForValue(property, property.getValue(), context, references, referencedBeans,
			// recursive);
			// }

			// Add references from inner beans
			for (IBean nestedBean : getInnerBeans(bean, false)) {
				Set<BeansConnection> nestedConnections = getBeanReferences(nestedBean, context, false);
				for (BeansConnection nestedConnection : nestedConnections) {
					references.add(new BeansConnection(nestedConnection.getType(), bean, nestedConnection.getTarget(),
							true));
				}
			}
		}
		else if (element instanceof IBeanConstructorArgument) {

			// Add referenced beans from constructor arguments element
			IBeanConstructorArgument carg = (IBeanConstructorArgument) element;
			addBeanReferencesForValue(carg, carg.getValue(), context, references, referencedBeans, recursive);
		}
		else if (element instanceof IBeanProperty) {

			// Add referenced beans from property element
			IBeanProperty property = (IBeanProperty) element;
			addBeanReferencesForValue(property, property.getValue(), context, references, referencedBeans, recursive);

		}
		else {
			throw new IllegalArgumentException("Unsupported model element " + element);
		}
		return references;
	}

	/**
	 * Registers all {@link IBean}s and {@link IBeansComponent}s that are nested within the given
	 * <code>components</code>.
	 * 
	 * @param configSet A beans config set that, is not null, limits the registered beans to those that are included in the beans config set
	 */
	private static void registerComponents(IBeansConfigSet configSet, Set<IBeansComponent> components,
			BeanDefinitionRegistry registry) {

		for (IBeansComponent component : components) {

			if (isProfileDisabled(configSet, component)) {
				continue;
			}

			for (IBean bean : component.getBeans()) {
				try {
					String beanName = bean.getElementName();

					// Register bean definition under primary name.
					registry.registerBeanDefinition(beanName, ((Bean) bean).getBeanDefinition());

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

			// Register bean definitions from components
			registerComponents(configSet, component.getComponents(), registry);
		}
	}
	
	public static boolean isProfileDisabled(IResourceModelElement contextElement, IModelElement beansComponent) {
		if (contextElement != null && contextElement instanceof IBeansConfigSet && beansComponent != null && beansComponent instanceof IProfileAwareBeansComponent) {
			IProfileAwareBeansComponent profileAwareBeansComponent = (IProfileAwareBeansComponent) beansComponent;
			IBeansConfigSet configSet = (IBeansConfigSet) contextElement;

			if (profileAwareBeansComponent.getProfiles().size() != 0
					&& !CollectionUtils.containsAny(profileAwareBeansComponent.getProfiles(), configSet.getProfiles())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns a list of all configs which contain a bean that uses a bean class that is part of the java structure
	 * represented by the given <code>resource</code>.
	 * <p>
	 * This implementation considers <b>all</b> inner classes as potential bean classes as well.
	 * @since 2.0.5
	 */
	public static Set<IBeansConfig> getConfigsByContainingTypes(IResource resource, TypeHierarchyEngine typeEngine, IProgressMonitor monitor) {
		if (System.getProperty(TypeHierarchyEngine.ENABLE_PROPERTY, "true").equals("true")) {
			return getConfigsByContainingTypesUsingTypeHierarchyEngine(resource, typeEngine, monitor);
		}
		return getConfigsByContainingTypesJDT(resource, monitor);
	}
	
	protected static Set<IBeansConfig> getConfigsByContainingTypesUsingTypeHierarchyEngine(IResource resource, TypeHierarchyEngine typeEngine, IProgressMonitor monitor) {
		Set<IBeansConfig> files = new LinkedHashSet<IBeansConfig>();

		if (resource != null && resource.isAccessible() && resource.isSynchronized(IResource.DEPTH_ZERO)
				&& resource.getName().endsWith(".java")) {
			Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
			if (projects != null) {

				IJavaElement element = JavaCore.create(resource);
				if (element instanceof ICompilationUnit && element.getJavaProject().isOnClasspath(element)) {
					
					try {
						IType[] types = ((ICompilationUnit) element).getAllTypes();
						String[] changedTypeNames = new String[types.length];
						boolean[] changedTypeIsInterface = new boolean[types.length];
						for (int i = 0; i < types.length; i++) {
							changedTypeNames[i] = types[i].getFullyQualifiedName();
							changedTypeIsInterface[i] = types[i].isInterface();
						}
						
						for (IBeansProject project : projects) {
							if (project != null) {

								// don't look at projects that do not have the java element on their classpath
								if (JdtUtils.isJavaProject(project.getProject()) && !JdtUtils.getJavaProject(project.getProject()).isOnClasspath(element)) {
									continue;
								}
								
								Set<IBeansConfig> configs = project.getConfigs();
								for (IBeansConfig config : configs) {
									boolean configAdded = false;
									Set<String> allBeanClasses = config.getBeanClasses();
									for (int i = 0; i < changedTypeNames.length; i++) {
										for (String className : allBeanClasses) {
											if (changedTypeIsInterface[i] && typeEngine.doesImplement(className, changedTypeNames[i], project.getProject())) {
												files.add(config);
												configAdded = true;
												break;
											}
											else if (!changedTypeIsInterface[i] && typeEngine.doesExtend(className, changedTypeNames[i], project.getProject())) {
												files.add(config);
												configAdded = true;
												break;
											}
										}
										if (configAdded) break;
									}
								}
								
//								typeHierarchyEngine.cleanup(project.getProject());
							}
						}
					}
					catch (JavaModelException e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}
		return files;
	}

	protected static Set<IBeansConfig> getConfigsByContainingTypesJDT(IResource resource, IProgressMonitor monitor) {
		Set<IBeansConfig> files = new LinkedHashSet<IBeansConfig>();

		if (resource != null && resource.isAccessible() && resource.isSynchronized(IResource.DEPTH_ZERO)
				&& resource.getName().endsWith(".java")) {
			Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
			if (projects != null) {

				IJavaElement element = JavaCore.create(resource);
				if (element instanceof ICompilationUnit && element.getJavaProject().isOnClasspath(element)) {

					try {
						IType[] types = ((ICompilationUnit) element).getAllTypes();
						Set<List<IType>> hierachies = new HashSet<List<IType>>();
						List<IType> relevantTypes = Arrays.asList(types);
						for (IType type : types) {
							IType[] subTypes = SuperTypeHierarchyCache.getTypeHierarchy(type, monitor).getAllSubtypes(
									type);
							if (subTypes != null && subTypes.length > 0) {
								hierachies.add(Arrays.asList(subTypes));
							}
						}

						for (IBeansProject project : projects) {
							if (project != null) {
								Set<IBeansConfig> configs = project.getConfigs();
								for (IBeansConfig config : configs) {

									Set<String> allBeanClasses = config.getBeanClasses();
									for (String className : allBeanClasses) {
										IType type = JdtUtils.getJavaType(project.getProject(), className);
										if (type != null) {
											// 1. check if the bean class is clear match
											if (relevantTypes.contains(type)) {
												files.add(config);
											}
											else {
												for (List<IType> subTypes : hierachies) {
													if (subTypes.contains(type)) {
														files.add(config);
														break;
													}
												}
											}
											// 3. break the for loop if file is already in
											if (files.contains(config)) {
												break;
											}
										}
									}
								}
							}
						}
					}
					catch (JavaModelException e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}

		return files;
	}
	
	/**
	 * Returns a list of all beans which use a bean class that is part of the java structure represented by the given
	 * <code>resource</code>.
	 * <p>
	 * This implementation considers <b>all</b> inner classes as potential bean classes as well.
	 * @since 2.0.5
	 */
	public static Set<IBean> getBeansByContainingTypes(IResource resource, TypeHierarchyEngine typeEngine, IProgressMonitor monitor) {
		if (System.getProperty(TypeHierarchyEngine.ENABLE_PROPERTY, "true").equals("true")) {
			return getBeansByContainingTypesUsingTypeHierarchyEngine(resource, typeEngine, monitor);
		}
		return getBeansByContainingTypesJDT(resource, monitor);
	}
	
	protected static Set<IBean> getBeansByContainingTypesUsingTypeHierarchyEngine(IResource resource, TypeHierarchyEngine typeEngine, IProgressMonitor monitor) {
		Set<IBean> files = new LinkedHashSet<IBean>();

		if (resource != null && resource.isAccessible() && resource.isSynchronized(IResource.DEPTH_ZERO)
				&& resource.getName().endsWith(".java")) {
			Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
			if (projects != null) {

				IJavaElement element = JavaCore.create(resource);
				if (element instanceof ICompilationUnit && element.getJavaProject().isOnClasspath(element)) {

					try {
						IType[] types = ((ICompilationUnit) element).getAllTypes();
						String[] changedTypeNames = new String[types.length];
						boolean[] changedTypeIsInterface = new boolean[types.length];
						for (int i = 0; i < types.length; i++) {
							changedTypeNames[i] = types[i].getFullyQualifiedName();
							changedTypeIsInterface[i] = types[i].isInterface();
						}
						
						for (IBeansProject project : projects) {
							if (project != null) {
								
								// don't look at projects that do not have the java element on their classpath
								if (JdtUtils.isJavaProject(project.getProject()) && !JdtUtils.getJavaProject(project.getProject()).isOnClasspath(element)) {
									continue;
								}
								
								Set<IBeansConfig> configs = project.getConfigs();
								for (IBeansConfig config : configs) {
									Set<IBean> allBeans = getBeans(config);

									for (IBean bean : allBeans) {
										String className = resolveBeanTypeAsString(bean);
										
										if (className != null) {
											for (int i = 0; i < changedTypeNames.length; i++) {
												if (changedTypeIsInterface[i] && typeEngine.doesImplement(className, changedTypeNames[i], project.getProject())) {
													files.add(bean);
													break;
												}
												else if (!changedTypeIsInterface[i] && typeEngine.doesExtend(className, changedTypeNames[i], project.getProject())) {
													files.add(bean);
													break;
												}
											}
										}
										else {
											// We can't determine the beans type so don't be cleverer as we can and let
											// it be processed again
											// One last check before adding too much that is not even on the resource's
											// classpath
											if (project != null
													&& JdtUtils.isJavaProject(project.getProject())
													&& JdtUtils.getJavaProject(project.getProject()).isOnClasspath(
															resource)) {
												files.add(bean);
											}
										}
									}
								}
								
//								typeHierarchyEngine.cleanup(project.getProject());
							}
						}
					}
					catch (JavaModelException e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}
		return files;
	}

	protected static Set<IBean> getBeansByContainingTypesJDT(IResource resource, IProgressMonitor monitor) {
		Set<IBean> files = new LinkedHashSet<IBean>();

		if (resource != null && resource.isAccessible() && resource.isSynchronized(IResource.DEPTH_ZERO)
				&& resource.getName().endsWith(".java")) {
			Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
			if (projects != null) {

				IJavaElement element = JavaCore.create(resource);
				if (element instanceof ICompilationUnit && element.getJavaProject().isOnClasspath(element)) {

					try {
						IType[] types = ((ICompilationUnit) element).getAllTypes();
						Set<List<IType>> hierachies = new HashSet<List<IType>>();
						List<IType> relevantTypes = Arrays.asList(types);
						for (IType type : types) {
							IType[] subTypes = SuperTypeHierarchyCache.getTypeHierarchy(type, monitor).getAllSubtypes(
									type);
							if (subTypes != null && subTypes.length > 0) {
								hierachies.add(Arrays.asList(subTypes));
							}
						}

						for (IBeansProject project : projects) {
							if (project != null) {
								Set<IBeansConfig> configs = project.getConfigs();
								for (IBeansConfig config : configs) {
									Set<IBean> allBeans = getBeans(config);

									for (IBean bean : allBeans) {
										IType type = resolveBeanType(bean);
										if (type != null) {
											if (relevantTypes.contains(type)) {
												files.add(bean);
											}
											else {
												for (List<IType> subTypes : hierachies) {
													if (subTypes.contains(type)) {
														files.add(bean);
														break;
													}
												}
											}
										}
										else {
											// We can't determine the beans type so don't be cleverer as we can and let
											// it be processed again
											// One last check before adding too much that is not even on the resource's
											// classpath
											if (project != null
													&& JdtUtils.isJavaProject(project.getProject())
													&& JdtUtils.getJavaProject(project.getProject()).isOnClasspath(
															resource)) {
												files.add(bean);
											}
										}
									}
								}
							}
						}
					}
					catch (JavaModelException e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}
		return files;
	}

	/**
	 * Resolves the {@link IBean} bean class by looking at parent, factory-bean and factory-method.
	 */
	public static IType resolveBeanType(IBean bean) {
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(bean, null);
		String mergedClassName = mergedBd.getBeanClassName();
		return extractBeanClass(mergedBd, bean, mergedClassName, getParentOfClass(bean, IBeansConfig.class));
	}

	/**
	 * Extracts the {@link IType} of a bean definition.
	 * <p>
	 * Honors <code>factory-method</code>s and <code>factory-bean</code>.
	 */
	private static IType extractBeanClass(BeanDefinition bd, IBean bean, String mergedClassName,
			IBeansConfig beansConfig) {
		IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), mergedClassName);
		// 1. factory-method on bean
		if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() == null) {
			type = extractTypeFromFactoryMethod(bd, type);
		}
		// 2. factory-method on factory-bean
		else if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() != null) {
			try {
				IBean factoryB = getBeanWithConfigSets(bd.getFactoryBeanName(), beansConfig);
				if (factoryB != null) {
					BeanDefinition factoryBd = BeansModelUtils.getMergedBeanDefinition(factoryB, null);
					IType factoryBeanType = extractBeanClass(factoryBd, bean, factoryBd.getBeanClassName(), beansConfig);
					if (factoryBeanType != null) {
						type = extractTypeFromFactoryMethod(bd, factoryBeanType);
					}
				}
			}
			catch (NoSuchBeanDefinitionException e) {
			}
		}
		return type;
	}

	/**
	 * Resolves the {@link IBean} bean class by looking at parent, factory-bean and factory-method.
	 */
	public static String resolveBeanTypeAsString(IBean bean) {
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(bean, null);
		String mergedClassName = mergedBd.getBeanClassName();
		return extractBeanClassAsString(mergedBd, bean, mergedClassName, getParentOfClass(bean, IBeansConfig.class));
	}

	/**
	 * Extracts the {@link IType} of a bean definition.
	 * <p>
	 * Honors <code>factory-method</code>s and <code>factory-bean</code>.
	 */
	private static String extractBeanClassAsString(BeanDefinition bd, IBean bean, String mergedClassName,
			IBeansConfig beansConfig) {
		String result = mergedClassName;
		// 1. factory-method on bean
		if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() == null) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), mergedClassName);
			result = extractTypeFromFactoryMethodAsString(bd, type);
			if (result == null) {
				result = mergedClassName;
			}
		}
		// 2. factory-method on factory-bean
		else if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() != null) {
			try {
				IBean factoryB = getBeanWithConfigSets(bd.getFactoryBeanName(), beansConfig);
				if (factoryB != null) {
					BeanDefinition factoryBd = BeansModelUtils.getMergedBeanDefinition(factoryB, null);
					String factoryBeanTypeName = extractBeanClassAsString(factoryBd, bean, factoryBd.getBeanClassName(), beansConfig);
					if (factoryBeanTypeName != null) {
						IType factoryBeanType = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), factoryBeanTypeName);
						result = extractTypeFromFactoryMethodAsString(bd, factoryBeanType);
						if (result == null) {
							result = factoryBeanTypeName;
						}
					}
				}
			}
			catch (NoSuchBeanDefinitionException e) {
			}
		}
		return result;
	}

	/**
	 * Extracts the {@link IType} of a {@link BeanDefinition} by only looking at the <code>
	 * factory-method</code> . The passed in {@link IType} <b>must</b> be the bean class or the resolved type of the
	 * factory bean in use.
	 */
	private static String extractTypeFromFactoryMethodAsString(BeanDefinition bd, IType type) {
		String factoryMethod = bd.getFactoryMethodName();
		try {
			int argCount = (!bd.isAbstract() ? bd.getConstructorArgumentValues().getArgumentCount() : -1);
			Set<IMethod> methods = Introspector.getAllMethods(type);
			for (IMethod method : methods) {
				if (factoryMethod.equals(method.getElementName()) && method.getParameterNames().length == argCount) {
					return JdtUtils.resolveClassNameBySignature(method.getReturnType(), type);
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	/**
	 * Extracts the {@link IType} of a {@link BeanDefinition} by only looking at the <code>
	 * factory-method</code> . The passed in {@link IType} <b>must</b> be the bean class or the resolved type of the
	 * factory bean in use.
	 */
	private static IType extractTypeFromFactoryMethod(BeanDefinition bd, IType type) {
		String factoryMethod = bd.getFactoryMethodName();
		try {
			int argCount = (!bd.isAbstract() ? bd.getConstructorArgumentValues().getArgumentCount() : -1);
			Set<IMethod> methods = Introspector.getAllMethods(type);
			for (IMethod method : methods) {
				if (factoryMethod.equals(method.getElementName()) && method.getParameterNames().length == argCount) {
					type = JdtUtils.getJavaTypeFromSignatureClassName(method.getReturnType(), type);
					break;
				}
			}
		}
		catch (JavaModelException e) {
		}
		return type;
	}

	/**
	 * A {@link IModelElementVisitor} that tries to determine the closest {@link IModelElement} by looking at
	 * <code>startLine</code> and <code>endLine</code> information.
	 * @author Christian Dupuis
	 */
	private static class ModelElementDetermingModelVisitor implements IModelElementVisitor {

		private int startLine;

		private int endLine;

		private final IFile file;

		private IModelElement element;

		public ModelElementDetermingModelVisitor(final int startLine, final int endLine, final IFile file) {
			if (startLine + 1 == endLine) {
				this.startLine = startLine + 1;
			}
			else {
				this.startLine = startLine;
			}
			this.endLine = endLine;
			this.file = file;
		}

		public IModelElement getElement() {
			return element;
		}

		public boolean visit(IModelElement element, IProgressMonitor monitor) {
			if (element instanceof ISourceModelElement) {
				ISourceModelElement sourceElement = (ISourceModelElement) element;
				if (sourceElement.getElementResource().equals(file)
						&& (sourceElement.getElementStartLine() <= startLine || sourceElement.getElementStartLine() - 1 <= startLine)
						&& endLine <= sourceElement.getElementEndLine()) {
					this.element = element;

					if (sourceElement.getElementStartLine() == startLine
							&& endLine == sourceElement.getElementEndLine()) {
						startLine = -1;
						endLine = -1;
						return false;
					}
					return true;
				}
				return false;
			}
			else if (element instanceof IBeansConfig) {
				return true;
			}
			else {
				return false;
			}
		}
	}

}
