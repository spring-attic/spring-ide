/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.ValidationRuleUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.project.DefaultProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating all relevant information used during
 * validation.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansValidationContext extends AbstractValidationContext implements IBeansValidationContext, IProjectContributorState {

	private static final char KEY_SEPARATOR_CHAR = '/';

	private BeanDefinitionRegistry incompleteRegistry;

	private BeanDefinitionRegistry completeRegistry;

	private ClassReaderFactory classReaderFactory;

	private IProjectClassLoaderSupport projectClassLoaderSupport;

	private Map<String, Set<BeanDefinition>> beanLookupCache;

	private final Map<AttributeDescriptor, List<ToolAnnotationData>> toolAnnotationLookupCache;

	private final IProjectContributorState buildState;

	public BeansValidationContext(IBeansConfig config, IResourceModelElement contextElement) {
		this(config, contextElement, new DefaultProjectContributorState());
	}
	
	public BeansValidationContext(IBeansConfig config, IResourceModelElement contextElement, IProjectContributorState context) {
		super(config, contextElement);
		
		this.buildState = context;
		
		this.incompleteRegistry = createRegistry(config, contextElement, false);
		this.completeRegistry = createRegistry(config, contextElement, true);

		this.beanLookupCache = new HashMap<String, Set<BeanDefinition>>();
		this.toolAnnotationLookupCache = new HashMap<AttributeDescriptor, List<ToolAnnotationData>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public BeanDefinitionRegistry getIncompleteRegistry() {
		return incompleteRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	public BeanDefinitionRegistry getCompleteRegistry() {
		return completeRegistry;
	}

	private BeanDefinitionRegistry createRegistry(IBeansConfig config, IResourceModelElement contextElement,
			boolean fillCompletely) {
		DefaultBeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
		if (contextElement instanceof BeansConfigSet) {
			IBeansConfigSet configSet = (IBeansConfigSet) contextElement;
			if (fillCompletely) {
				registry.setAllowAliasOverriding(true);
				registry.setAllowBeanDefinitionOverriding(true);
			}
			else {
				registry.setAllowAliasOverriding(configSet.isAllowAliasOverriding());
				registry.setAllowBeanDefinitionOverriding(configSet.isAllowBeanDefinitionOverriding());
			}
			for (IBeansConfig csConfig : configSet.getConfigs()) {
				if (!fillCompletely && config.equals(csConfig)) {
					break;
				}
				BeansModelUtils.register(csConfig, registry);
			}
		}
		else {
			registry.setAllowAliasOverriding(false);
			registry.setAllowBeanDefinitionOverriding(false);
			if (fillCompletely) {
				BeansModelUtils.register(config, registry);
			}
		}
		return registry;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized ClassReaderFactory getClassReaderFactory() {
		if (this.classReaderFactory == null) {
			this.classReaderFactory = new CachingClassReaderFactory(JdtUtils.getClassLoader(getRootElement()
					.getElementResource().getProject(), false));
		}
		return this.classReaderFactory;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public synchronized IProjectClassLoaderSupport getProjectClassLoaderSupport() {
		if (this.projectClassLoaderSupport == null) {
			this.projectClassLoaderSupport = JdtUtils.getProjectClassLoaderSupport(getRootElementProject(), true);
		}
		return this.projectClassLoaderSupport;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProject getRootElementProject() {
		return (getRootElement().getElementResource() != null ? getRootElement().getElementResource().getProject()
				: null);
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getRootElementResource() {
		return getRootElement().getElementResource();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<BeanDefinition> getRegisteredBeanDefinition(String beanName, String beanClass) {
		Assert.notNull(beanName);
		Assert.notNull(beanClass);

		String key = beanClass + KEY_SEPARATOR_CHAR + beanName;
		if (beanLookupCache.containsKey(key)) {
			return beanLookupCache.get(key);
		}
		Set<BeanDefinition> bds = ValidationRuleUtils.getBeanDefinitions(beanName, beanClass, this);
		// as we don't use a Hashtable we can insert null values
		beanLookupCache.put(key, bds);
		return bds;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isBeanRegistered(String beanName, String beanClass) {
		Set<BeanDefinition> bds = getRegisteredBeanDefinition(beanName, beanClass);
		return bds != null && bds.size() > 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public <T> T get(Class<T> clazz) {
		return buildState.get(clazz);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hold(Object obj) {
		return buildState.hold(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<ValidationProblem> createProblems(IResourceModelElement element, String problemId, int severity,
			String message, ValidationProblemAttribute... attributes) {

		Set<ValidationProblem> problems = super.createProblems(element, problemId, severity, message, attributes);
		IResource resource = element.getElementResource();

		// Check if error or warning on imported resource exists
		if (!resource.equals(getRootElementResource())) {
			IBeansImport beansImport = BeansModelUtils.getParentOfClass(element, IBeansImport.class);

			while (beansImport != null) {
				if (severity == IValidationProblemMarker.SEVERITY_ERROR) {
					problems.add(createProblem(beansImport, "", IValidationProblemMarker.SEVERITY_ERROR,
							"Validation error occured in imported configuration file '"
									+ element.getElementResource().getProjectRelativePath().toString() + "'"));
				}
				else if (severity == IValidationProblemMarker.SEVERITY_WARNING) {
					problems.add(createProblem(beansImport, "", IValidationProblemMarker.SEVERITY_WARNING,
							"Validation warning occured in imported configuration file '"
									+ element.getElementResource().getProjectRelativePath().toString() + "'"));
				}
				beansImport = BeansModelUtils.getParentOfClass(beansImport, IBeansImport.class);
			}
		}

		return problems;
	}

	public synchronized List<ToolAnnotationData> getToolAnnotation(Node n, String attributeName) {
		AttributeDescriptor descriptor = AttributeDescriptor.create(n, attributeName);
		if (toolAnnotationLookupCache.containsKey(descriptor)) {
			return toolAnnotationLookupCache.get(descriptor);
		}

		// Search for tool annotations
		List<ToolAnnotationData> annotationDatas = new ArrayList<ToolAnnotationData>();
		List<Element> appInfoElements = ToolAnnotationUtils.getApplicationInformationElements(n, attributeName);
		for (Element elem : appInfoElements) {
			NodeList children = elem.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node annotation = children.item(j);
				if (annotation.getNodeType() == Node.ELEMENT_NODE
						&& ToolAnnotationUtils.ANNOTATION_ELEMENT.equals(annotation.getLocalName())
						&& ToolAnnotationUtils.TOOL_NAMESPACE_URI.equals(annotation.getNamespaceURI())) {
					ToolAnnotationData annotationData = ToolAnnotationUtils.getToolAnnotationData(annotation);
					if (annotationData != null) {
						annotationDatas.add(annotationData);
					}
				}
			}
		}

		// Add to internal cache
		toolAnnotationLookupCache.put(descriptor, annotationDatas);

		// / Return found annoatations
		return annotationDatas;
	}
	
	static class AttributeDescriptor {

		private final String namespaceUri;

		private final String localName;

		private final String attributeName;

		private AttributeDescriptor(String namespaceUri, String localName, String attributeName) {
			this.namespaceUri = namespaceUri;
			this.localName = localName;
			this.attributeName = attributeName;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AttributeDescriptor)) {
				return false;
			}
			AttributeDescriptor other = (AttributeDescriptor) obj;
			if (!ObjectUtils.nullSafeEquals(namespaceUri, other.namespaceUri)) {
				return false;
			}
			if (!ObjectUtils.nullSafeEquals(localName, other.localName)) {
				return false;
			}
			return ObjectUtils.nullSafeEquals(attributeName, attributeName);
		}

		@Override
		public int hashCode() {
			int hashCode = 7;
			hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(namespaceUri);
			hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(localName);
			hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(attributeName);
			return hashCode;
		}
		
		public static AttributeDescriptor create(Node n, String attributeName) {
			return new AttributeDescriptor(n.getNamespaceURI(), n.getLocalName(), attributeName);
		}

	}
}