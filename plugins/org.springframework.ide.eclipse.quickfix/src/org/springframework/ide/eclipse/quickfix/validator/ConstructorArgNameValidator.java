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
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanConstructorArgumentRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;


/**
 * Validates constructor-arg name attribute
 * @author Terry Denney
 * @since 2.8
 */
public class ConstructorArgNameValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		IProject project = file.getProject();
		IDOMNode parentNode = getParentBeanNode(parent);
		BeanHelper parentBean = new BeanHelper(parentNode, file, project);

		ValidationRuleDefinition ruleDefinition = getValidationRule(project, BeanConstructorArgumentRule.class);
		BeanConstructorArgumentRule constructorArgRule = (BeanConstructorArgumentRule) (ruleDefinition != null ? ruleDefinition
				.getRule() : null);

		if (constructorArgRule != null) {
			BeansValidationContextHelper constructorArgContext = new BeansValidationContextHelper(attribute, parent,
					contextElement, project, reporter, validator, QuickfixProcessorFactory.CONSTRUCTOR_ARG, false,
					reportError, config);
			ruleDefinition = getValidationRule(project, BeanConstructorArgumentRule.class);
			BeanConstructorArgumentRule argRule = (BeanConstructorArgumentRule) (ruleDefinition != null ? ruleDefinition
					.getRule() : null);
			if (argRule != null) {
				constructorArgContext.setCurrentRuleDefinition(ruleDefinition);

				// NodeList childNodes = parentNode.getChildNodes();
				// int counter = 0;
				// for(int i=0; i<childNodes.getLength(); i++) {
				// Node childNode = childNodes.item(i);
				// String localName = childNode.getLocalName();
				// if (localName != null) {
				// if
				// (localName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG))
				// {
				// // BeanConstructorArgumentHelper currConstructorArg = new
				// BeanConstructorArgumentHelper(counter, (IDOMNode) childNode,
				// file, parentBean);
				// counter++;
				// if (childNode.equals(parent)) {
				// BeanConstructorArgumentHelper constructorArg = new
				// BeanConstructorArgumentHelper(counter, (IDOMNode) childNode,
				// file, parentBean);
				// }
				// }
				// }
				// }

				argRule.validate(parentBean, constructorArgContext, null);
			}

			return constructorArgContext.getErrorFound();
		}

		return false;
	}
}
