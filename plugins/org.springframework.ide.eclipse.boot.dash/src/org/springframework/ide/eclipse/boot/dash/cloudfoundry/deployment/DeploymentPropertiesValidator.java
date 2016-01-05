/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

public abstract class DeploymentPropertiesValidator extends Validator {

	public DeploymentPropertiesValidator() {
	}

	abstract protected CloudApplicationDeploymentProperties getProperties();

	@Override
	protected ValidationResult compute() {
		String errorMessage = null;
		CloudApplicationDeploymentProperties deploymentProperties = getProperties();
		if (deploymentProperties == null) {
			errorMessage = "No deployment properties to validate. Unable to deploy or restart the application.";
		} else if (deploymentProperties.getAppName() == null
			|| deploymentProperties.getAppName().trim().length() == 0) {
			errorMessage = "Missing application name.";
		} else if (deploymentProperties.getMemory() <= 0) {
			errorMessage = "Invalid memory. Memory must be greater than 0.";
		} else if (deploymentProperties.getInstances() < 1) {
			errorMessage = "Invalid instances. There must be at least one instance for the application.";
		}

		if (errorMessage == null) {
			return ValidationResult.OK;
		} else {
			return ValidationResult.error(errorMessage);
		}
	}

}
