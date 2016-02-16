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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanClassRule;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanConstructorArgumentRule;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanDeprecationRule;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.RequiredPropertyRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;


/**
 * Validates class attribute of a bean configuration.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class ClassAttributeValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		IProject project = file.getProject();
		BeanHelper parentBean = new BeanHelper(parent, file, project);

		ValidationRuleDefinition ruleDefinition = getValidationRule(project, BeanClassRule.class);
		BeanClassRule classRule = (BeanClassRule) (ruleDefinition != null ? ruleDefinition.getRule() : null);

		if (classRule != null) {
			BeansValidationContextHelper classContext = new BeansValidationContextHelper(attribute, parent,
					contextElement, project, reporter, validator, QuickfixProcessorFactory.CLASS, false, reportError,
					config);

			classContext.setCurrentRuleDefinition(ruleDefinition);
			classRule.validate(parentBean, classContext, null);

			BeansValidationContextHelper deprecatedContext = new BeansValidationContextHelper(attribute, parent,
					contextElement, project, reporter, validator, QuickfixProcessorFactory.DEPRECATED, false,
					reportError, config);
			ruleDefinition = getValidationRule(project, BeanDeprecationRule.class);
			BeanDeprecationRule deprecationRule = (BeanDeprecationRule) (ruleDefinition != null ? ruleDefinition
					.getRule() : null);

			if (deprecationRule != null) {
				classContext.setCurrentRuleDefinition(ruleDefinition);
				deprecationRule.validate(parentBean, deprecatedContext, null);
			}

			Set<String> problemIdToIgnore = new HashSet<String>();
			problemIdToIgnore.add("MISSING_CONSTRUCTOR_ARG_NAME");
			BeansValidationContextHelper constructorArgContext = new BeansValidationContextHelper(attribute, parent,
					contextElement, project, reporter, validator, QuickfixProcessorFactory.CONSTRUCTOR_ARG, true,
					reportError, config, problemIdToIgnore);
			ruleDefinition = getValidationRule(project, BeanConstructorArgumentRule.class);
			BeanConstructorArgumentRule argRule = (BeanConstructorArgumentRule) (ruleDefinition != null ? ruleDefinition
					.getRule() : null);
			if (argRule != null) {
				constructorArgContext.setCurrentRuleDefinition(ruleDefinition);
				argRule.validate(parentBean, constructorArgContext, null);
			}

			BeansValidationContextHelper requiredContext = new BeansValidationContextHelper(attribute, parent,
					contextElement, project, reporter, validator, QuickfixProcessorFactory.REQUIRED_PROPERTY, false,
					reportError, config);
			ruleDefinition = getValidationRule(project, RequiredPropertyRule.class);
			RequiredPropertyRule requiredRule = (RequiredPropertyRule) (ruleDefinition != null ? ruleDefinition
					.getRule() : null);
			if (requiredRule != null) {
				requiredContext.setCurrentRuleDefinition(ruleDefinition);
				requiredRule.validate(parentBean, classContext, null);
			}

			return classContext.getErrorFound() | deprecatedContext.getErrorFound()
					| constructorArgContext.getErrorFound();
		}

		return false;
	}
}
