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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansValueHolder;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractNonInfrastructureBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBean}'s or {@link IBeansValueHolder}'s bean reference(s).
 * <p>
 * NOTE: This {@link IValidationRule} is the only rule that works on {@link IBean} instances or its children that does
 * not extend {@link AbstractNonInfrastructureBeanValidationRule}. This is on purpose as we only want to validate bean
 * references for infrastructure beans.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanReferenceRule implements IValidationRule<IBeansModelElement, IBeansValidationContext> {

	/** Magic beans names that should not be reported as invalid bean reference */
	private static final List<String> BEANS_TO_IGNORE = Arrays.asList(new String[] { "bundleContext" });

	/**
	 * Returns <code>true</code> if this rule is able to validate the given {@link IModelElement} with the specified
	 * {@link IValidationContext}.
	 * <p>
	 * Skip IBeansMap because it's entries (IBeansMapEntry -> IBansValueHolder) are validated instead.
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		IBean parentBean = BeansModelUtils.getParentOfClass(element, IBean.class);
		return (element instanceof Bean || element instanceof IBeansValueHolder || element instanceof IBeansList || element instanceof IBeansSet)
				&& ((element instanceof IBean && !((IBean) element).isInfrastructure()) || (parentBean != null && !parentBean
						.isInfrastructure()));
	}

	public void validate(IBeansModelElement element, IBeansValidationContext context, IProgressMonitor monitor) {
		if (element instanceof Bean) {
			validateBean((Bean) element, context);
		}
		else if (element instanceof IBeansValueHolder) {
			IBeansValueHolder holder = (IBeansValueHolder) element;
			validateValue(holder, holder.getValue(), context);
		}
		else if (element instanceof IBeansList) {
			IBeansList list = (IBeansList) element;
			for (Object entry : list.getList()) {
				validateValue(list, entry, context);
			}
		}
		else if (element instanceof IBeansSet) {
			IBeansSet set = (IBeansSet) element;
			for (Object entry : set.getSet()) {
				validateValue(set, entry, context);
			}
		}
	}

	private void validateBean(Bean bean, IBeansValidationContext context) {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) bean.getBeanDefinition();

		// Validate parent bean
		if (bean.isChildBean()) {
			String parentName = bean.getParentName();
			if (parentName != null && !SpringCoreUtils.hasPlaceHolder(parentName)
					&& !BEANS_TO_IGNORE.contains(parentName)) {
				try {
					context.getCompleteRegistry().getBeanDefinition(parentName);
				}
				catch (NoSuchBeanDefinitionException e) {
					context.warning(bean, "UNDEFINED_PARENT_BEAN", "Parent bean '" + parentName + "' not found");
				}
				catch (BeanDefinitionStoreException e) {
					context.warning(bean, "UNDEFINED_PARENT_BEAN", "Parent bean '" + parentName + "' not found");
				}
			}
		}

		// Validate depends-on beans
		if (bd.getDependsOn() != null) {
			for (String beanName : bd.getDependsOn()) {
				validateDependsOnBean(bean, beanName, context);
			}
		}
	}

	private void validateDependsOnBean(IBean bean, String beanName, IBeansValidationContext context) {
		if (beanName != null && !SpringCoreUtils.hasPlaceHolder(beanName) && !BEANS_TO_IGNORE.contains(beanName)) {
			try {
				BeanDefinition dependsBd = context.getCompleteRegistry().getBeanDefinition(beanName);
				if (dependsBd.isAbstract()
						|| (dependsBd.getBeanClassName() == null && dependsBd.getFactoryBeanName() == null)) {
					context.error(bean, "INVALID_DEPENDS_ON_BEAN", "Referenced depends-on bean '" + beanName
							+ "' is invalid (abstract or no bean class and no " + "factory bean)");
				}
			}
			catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					context.warning(bean, "UNDEFINED_DEPENDS_ON_BEAN", "Depends-on bean '" + beanName + "' not found");
				}
			}
		}
	}

	private void validateValue(IResourceModelElement element, Object value, IBeansValidationContext context) {
		String beanName = null;
		if (value instanceof RuntimeBeanReference) {
			beanName = ((RuntimeBeanReference) value).getBeanName();
		}
		else if (value instanceof BeanReference) {
			beanName = ((BeanReference) value).getBeanName();
		}
		if (beanName != null && !SpringCoreUtils.hasPlaceHolder(beanName) && !BEANS_TO_IGNORE.contains(beanName)) {
			try {
				BeanDefinition refBd = context.getCompleteRegistry().getBeanDefinition(beanName);
				if (refBd.isAbstract() || (refBd.getBeanClassName() == null && refBd.getFactoryBeanName() == null)) {
					context.error(element, "INVALID_REFERENCED_BEAN", "Referenced bean '" + beanName + "' is invalid "
							+ "(abstract or no bean class and " + "no factory bean)");
				}
			}
			catch (NoSuchBeanDefinitionException e) {

				// Handle factory bean references
				if (ValidationRuleUtils.isFactoryBeanReference(beanName)) {
					String tempBeanName = beanName.replaceFirst(ValidationRuleUtils.FACTORY_BEAN_REFERENCE_REGEXP, "");
					try {
						BeanDefinition def = context.getCompleteRegistry().getBeanDefinition(tempBeanName);
						String beanClassName = def.getBeanClassName();
						if (beanClassName != null) {
							IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(element).getProject(),
									beanClassName);
							if (type != null) {
								if (!JdtUtils.doesImplement(context.getRootElementResource(), type, FactoryBean.class.getName())) {
									context.error(element, "INVALID_FACTORY_BEAN", "Referenced factory bean '"
											+ tempBeanName + "' does not implement the " + "interface 'FactoryBean'");
								}
							}
							else {
								context.warning(element, "INVALID_REFERENCED_BEAN", "Referenced factory bean '"
										+ tempBeanName + "' implementation class not found");
							}
						}
					}
					catch (NoSuchBeanDefinitionException be) {
						context.warning(element, "UNDEFINED_FACTORY_BEAN", "Referenced factory bean '" + tempBeanName
								+ "' not found");
					}
					catch (BeanDefinitionStoreException be) {
						// ignore unresolvable parent bean exceptions
					}
				}
				else {
					context.warning(element, "UNDEFINED_REFERENCED_BEAN", "Referenced bean '" + beanName
							+ "' not found");
				}
			}
			catch (BeanDefinitionStoreException e) {
				// ignore unresolvable parent bean exceptions
			}
		}
	}

}
