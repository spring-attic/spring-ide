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
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanFactoryRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;
import org.w3c.dom.Node;


/**
 * Validates factory method attribute of a bean configuration.
 * @author Terry Denney
 * @author Christian Dupuis
 * @since 2.0
 */
public class FactoryBeanValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		IProject project = file.getProject();

		ValidationRuleDefinition ruleDefinition = getValidationRule(project, BeanFactoryRule.class);
		BeanFactoryRule rule = (BeanFactoryRule) (ruleDefinition != null ? ruleDefinition.getRule() : null);
		if (rule != null) {

			BeanHelper parentBean = new BeanHelper(parent, file, project);
			parentBean.getBeanDefinition().setFactoryBeanName(attribute.getNodeValue());

			Node factoryMethodAttr = parent.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_FACTORY_METHOD);
			if (factoryMethodAttr != null) {
				parentBean.getBeanDefinition().setFactoryMethodName(factoryMethodAttr.getNodeValue());
			}

			BeansValidationContextHelper context = new BeansValidationContextHelper(attribute, parent, contextElement,
					project, reporter, validator, QuickfixProcessorFactory.FACTORY_BEAN, false, reportError, config);
			context.setCurrentRuleDefinition(ruleDefinition);
			rule.validate(parentBean, context, null);

			if (context.getErrorFound()) {
				return true;
			}
		}
		return false;
	}
}
