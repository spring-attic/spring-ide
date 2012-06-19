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
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanPropertyRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanPropertyHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;


/**
 * Validates property nodes of a bean configuration
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class PropertyValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		IProject project = file.getProject();

		ValidationRuleDefinition propertyRuleDefinition = getValidationRule(project, BeanPropertyRule.class);
		BeanPropertyRule propertyRule = (BeanPropertyRule) (propertyRuleDefinition != null ? propertyRuleDefinition
				.getRule() : null);

		ValidationRuleDefinition depracationRuleDefinition = getValidationRule(project, BeanDeprecationRule.class);
		BeanDeprecationRule deprecationRule = (BeanDeprecationRule) (depracationRuleDefinition != null ? depracationRuleDefinition
				.getRule()
				: null);

		BeanHelper parentBean = new BeanHelper(getParentBeanNode(parent), file, project);

		BeansValidationContextHelper context = new BeansValidationContextHelper(attribute, parent, contextElement,
				project, reporter, validator, QuickfixProcessorFactory.PROPERTY, false, reportError, config);
		BeanPropertyHelper property = new BeanPropertyHelper(parent, file, parentBean);

		if (propertyRule != null) {
			context.setCurrentRuleDefinition(propertyRuleDefinition);
			propertyRule.validate(property, context, null);
		}

		if (context.getErrorFound()) {
			return true;
		}

		context = new BeansValidationContextHelper(attribute, parent, contextElement, project, reporter, validator,
				QuickfixProcessorFactory.DEPRECATED, false, reportError, config);

		ITextRegion valueRegion = attribute.getValueRegion();

		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(parentBean,
				context.getContextElement());

		// For non-factory beans validate it's init-method and
		// destroy-method
		String mergedClassName = mergedBd.getBeanClassName();

		if (valueRegion != null && mergedClassName != null) {
			// add rename refactoring option
			validator.createAndAddEmptyMessage(valueRegion, parent, "", reporter,
					QuickfixProcessorFactory.RENAME_PROPERTY, null, new ValidationProblemAttribute("CLASS",
							mergedClassName));
		}

		if (deprecationRule != null) {
			context.setCurrentRuleDefinition(depracationRuleDefinition);
			deprecationRule.validate(property, context, null);
		}

		return context.getErrorFound();
	}

}
