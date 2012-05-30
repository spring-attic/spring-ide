/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.validator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanDeprecationRule;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanInitDestroyMethodRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;


/**
 * Validates init method attribute of a bean configuration.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class InitDestroyMethodValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		ITextRegion valueRegion = attribute.getValueRegion();

		IProject project = file.getProject();

		ValidationRuleDefinition initRuleDefinition = getValidationRule(project, BeanInitDestroyMethodRule.class);
		BeanInitDestroyMethodRule initDestroyMethodRule = (BeanInitDestroyMethodRule) (initRuleDefinition != null ? initRuleDefinition
				.getRule()
				: null);

		BeanHelper parentBean = new BeanHelper(parent, file, project);

		BeansValidationContextHelper context = new BeansValidationContextHelper(attribute, parent, contextElement,
				project, reporter, validator, QuickfixProcessorFactory.INIT_DESTROY_METHOD, false, reportError, config);

		AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) parentBean.getBeanDefinition();
		if (attribute.getNodeName().equals(BeansSchemaConstants.ATTR_INIT_METHOD)) {
			beanDefinition.setInitMethodName(attribute.getNodeValue());
		}
		else if (attribute.getNodeName().equals(BeansSchemaConstants.ATTR_DESTROY_METHOD)) {
			beanDefinition.setDestroyMethodName(attribute.getNodeValue());
		}

		// add rename refactoring option
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(parentBean,
				context.getContextElement());

		// For non-factory beans validate it's init-method and
		// destroy-method
		String mergedClassName = mergedBd.getBeanClassName();
		if (valueRegion != null && mergedClassName != null) {

			validator.createAndAddEmptyMessage(valueRegion, parent, "", reporter,
					QuickfixProcessorFactory.RENAME_METHOD, null, new ValidationProblemAttribute("CLASS",
							mergedClassName), new ValidationProblemAttribute("METHOD", attribute.getNodeValue()));
		}

		if (initDestroyMethodRule != null) {
			context.setCurrentRuleDefinition(initRuleDefinition);
			initDestroyMethodRule.validate(parentBean, context, null);
		}

		if (context.getErrorFound()) {
			return true;
		}

		ValidationRuleDefinition depracationRuleDefinition = getValidationRule(project, BeanDeprecationRule.class);
		BeanDeprecationRule deprecationRule = (BeanDeprecationRule) (depracationRuleDefinition != null ? depracationRuleDefinition
				.getRule()
				: null);

		context = new BeansValidationContextHelper(attribute, parent, config, project, reporter, validator,
				QuickfixProcessorFactory.DEPRECATED, false, reportError, config);

		if (deprecationRule != null) {
			context.setCurrentRuleDefinition(depracationRuleDefinition);
			deprecationRule.validate(parentBean, context, null);
		}

		return context.getErrorFound();

	}
}
