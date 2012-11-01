/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.bestpractices.tests.ruletests;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.bestpractices.tests.AbstractBeansCoreTestCase;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;

/**
 * @author Tomasz Zarna
 * @since 3.1.0
 *
 */
public abstract class AbstractRuleTest extends AbstractBeansCoreTestCase {

	private ValidatorDefinition validatorDef;

	private ValidationRuleDefinition ruleDefinition;

	private IProject project;

	abstract String getRuleId();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final String validatorId = "org.springframework.ide.eclipse.bestpractices.beansvalidator";
		validatorDef = ValidatorDefinitionFactory.getValidatorDefinition(validatorId);
		assertNotNull(validatorDef);
		ruleDefinition = getRuleDefinition(validatorId, getRuleId());
		assertNotNull(ruleDefinition);
	}

	private ValidationRuleDefinition getRuleDefinition(final String validatorId, final String ruleId)
			throws CoreException {
		String qualifiedRuleId = "org.springframework.ide.eclipse.bestpractices" + "." + ruleId + "-" + validatorId;
		Set<ValidationRuleDefinition> ruleDefinitions = ValidationRuleDefinitionFactory.getRuleDefinitions(validatorId);
		for (ValidationRuleDefinition ruleDefinition : ruleDefinitions) {
			if (qualifiedRuleId.equals(ruleDefinition.getId())) {
				return ruleDefinition;
			}
		}
		return null;
	}

	@Override
	protected IProject createPredefinedProject(String projectName) throws CoreException, IOException {
		project = super.createPredefinedProject(projectName);
		ruleDefinition.setEnabled(true, project);
		validatorDef.setEnabled(true, project);
		return project;
	}
}
