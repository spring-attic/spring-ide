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
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.HashMap;
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
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.util.Assert;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating all relevant information
 * used during validation.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansValidationContext extends AbstractValidationContext implements
		IBeansValidationContext {

	private static final char KEY_SEPARATOR_CHAR = '/';

	private BeanDefinitionRegistry incompleteRegistry;

	private BeanDefinitionRegistry completeRegistry;

	private ClassReaderFactory classReaderFactory;

	private Map<String, Set<BeanDefinition>> beanLookupCache;

	public BeansValidationContext(IBeansConfig config, IResourceModelElement contextElement) {
		super(config, contextElement);

		incompleteRegistry = createRegistry(config, contextElement, false);
		completeRegistry = createRegistry(config, contextElement, true);

		beanLookupCache = new HashMap<String, Set<BeanDefinition>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getIncompleteRegistry()
	 */
	public BeanDefinitionRegistry getIncompleteRegistry() {
		return incompleteRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getCompleteRegistry()
	 */
	public BeanDefinitionRegistry getCompleteRegistry() {
		return completeRegistry;
	}

	private BeanDefinitionRegistry createRegistry(IBeansConfig config,
			IResourceModelElement contextElement, boolean fillCompletely) {
		DefaultBeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
		if (contextElement instanceof BeansConfigSet) {
			IBeansConfigSet configSet = (IBeansConfigSet) contextElement;
			if (fillCompletely) {
				registry.setAllowAliasOverriding(true);
				registry.setAllowBeanDefinitionOverriding(true);
			}
			else {
				registry.setAllowAliasOverriding(configSet.isAllowAliasOverriding());
				registry.setAllowBeanDefinitionOverriding(configSet
						.isAllowBeanDefinitionOverriding());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getClassReaderFactory()
	 */
	public synchronized ClassReaderFactory getClassReaderFactory() {
		if (this.classReaderFactory == null) {
			this.classReaderFactory = new CachingClassReaderFactory(JdtUtils.getClassLoader(
					getRootElement().getElementResource().getProject(), false));
		}
		return this.classReaderFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getRootElementProject()
	 */
	public IProject getRootElementProject() {
		return (getRootElement().getElementResource() != null ? getRootElement()
				.getElementResource().getProject() : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getRootElementResource()
	 */
	public IResource getRootElementResource() {
		return getRootElement().getElementResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #getRegisteredBeanDefinition(java.lang.String, java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ide.eclipse.beans.core.internal.model.validation.IBeansValidationContext
	 * #isBeanRegistered(java.lang.String, java.lang.String)
	 */
	public boolean isBeanRegistered(String beanName, String beanClass) {
		Set<BeanDefinition> bds = getRegisteredBeanDefinition(beanName, beanClass);
		return bds != null && bds.size() > 0;
	}

	@Override
	protected Set<ValidationProblem> createProblems(IResourceModelElement element,
			String problemId, int severity, String message,
			ValidationProblemAttribute... attributes) {

		Set<ValidationProblem> problems = super.createProblems(element, problemId, severity,
				message, attributes);
		IResource resource = element.getElementResource();

		// Check if error or warning on imported resource exists
		if (!resource.equals(getRootElementResource())) {
			IBeansImport beansImport = BeansModelUtils
					.getParentOfClass(element, IBeansImport.class);

			while (beansImport != null) {
				if (severity == IValidationProblemMarker.SEVERITY_ERROR) {
					problems.add(createProblem(beansImport, "",
							IValidationProblemMarker.SEVERITY_ERROR,
							"Validation error occured in imported configuration file '"
									+ element.getElementResource().getProjectRelativePath()
											.toString() + "'"));
				}
				else if (severity == IValidationProblemMarker.SEVERITY_WARNING) {
					problems.add(createProblem(beansImport, "",
							IValidationProblemMarker.SEVERITY_WARNING,
							"Validation warning occured in imported configuration file '"
									+ element.getElementResource().getProjectRelativePath()
											.toString() + "'"));
				}
				beansImport = BeansModelUtils.getParentOfClass(beansImport, IBeansImport.class);
			}
		}

		return problems;
	}
}