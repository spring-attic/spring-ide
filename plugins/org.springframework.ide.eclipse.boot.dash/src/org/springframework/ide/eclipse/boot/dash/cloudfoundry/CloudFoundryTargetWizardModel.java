/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.jface.operation.IRunnableContext;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Cloud Foundry Target properties that uses {@link LiveExpression} and
 * {@link Validator}.
 *
 *
 *
 */
public class CloudFoundryTargetWizardModel extends CloudFoundryTargetProperties {

	private LiveVariable<String> url = new LiveVariable<String>();
	private LiveVariable<CFSpace> space = new LiveVariable<CFSpace>();
	private LiveVariable<Boolean> selfsigned = new LiveVariable<Boolean>(false);
	private LiveVariable<String> userName = new LiveVariable<String>();
	private LiveVariable<String> password = new LiveVariable<String>();
	private LiveVariable<OrgsAndSpaces> allSpaces = new LiveVariable<OrgsAndSpaces>();

	private Validator credentialsValidator = new CredentialsValidator();
	private Validator spacesValidator = new CloudSpaceValidator();
	private Validator orgsSpacesValidator = new OrgsSpacesValidator();
	private CompositeValidator allPropertiesValidator = new CompositeValidator();

	private CloudFoundryClientFactory clientFactory;
	private List<RunTarget> existingTargets;

	public CloudFoundryTargetWizardModel(RunTargetType runTargetType, CloudFoundryClientFactory clientFactory,
			List<RunTarget> existingTargets, BootDashModelContext context) {
		super(runTargetType, context);
		this.existingTargets = existingTargets == null ? new ArrayList<RunTarget>() : existingTargets;
		this.clientFactory = clientFactory;
		// The credentials validator should be notified any time there are
		// changes
		// to url, username, password and selfsigned setting.
		credentialsValidator.dependsOn(url);
		credentialsValidator.dependsOn(selfsigned);
		credentialsValidator.dependsOn(userName);
		credentialsValidator.dependsOn(password);

		// Spaces validator is notified when there are changes to the space
		// variable. This is a separate validator as space validation and spave
		// value setting may only occur AFTER ALL credentials/URL are entered or
		// validated, and different listeners may need to be registered for
		// credential validation
		// vs space validation
		spacesValidator.dependsOn(space);

		orgsSpacesValidator.dependsOn(allSpaces);

		// Aggregate of the credentials and space validator.
		allPropertiesValidator.addChild(credentialsValidator);
		allPropertiesValidator.addChild(spacesValidator);
		allPropertiesValidator.addChild(orgsSpacesValidator);

		setUrl(getDefaultTargetUrl());
	}

	/**
	 * @param credentialsValidationListener
	 *            listener that is notified when only credential properties are
	 *            validated (but not org/space)
	 *
	 */
	public void addCredentialsListener(ValueListener<ValidationResult> credentialsValidationListener) {
		credentialsValidator.addListener(credentialsValidationListener);
	}

	/**
	 * @param cloudSpaceChangeListener
	 *            listener that is notified when Cloud space is changed
	 *
	 */
	public void addSpaceSelectionListener(ValueListener<CFSpace> cloudSpaceChangeListener) {
		space.addListener(cloudSpaceChangeListener);
	}

	/**
	 * @param allPropertiesValidationListener
	 *            listener that is notified when any property is validated
	 */
	public void addAllPropertiesListener(ValueListener<ValidationResult> allPropertiesValidationListener) {
		allPropertiesValidator.addListener(allPropertiesValidationListener);
	}

	public void removeAllPropertiesListeners(ValueListener<ValidationResult> allPropertiesValidationListener) {
		allPropertiesValidator.removeListener(allPropertiesValidationListener);

	}

	public void removeCredentialsListeners(ValueListener<ValidationResult> credentialsValidationListener) {
		credentialsValidator.removeListener(credentialsValidationListener);
	}

	public void removeSpaceSelectionListeners(ValueListener<CFSpace> cloudSpaceChangeListener) {
		space.removeListener(cloudSpaceChangeListener);
	}

	/*
	 *
	 * NOTE: for the setters, make sure the values are placed in the underlying
	 * backed map first before setting them in the live variables, as live
	 * variables trigger validation events.
	 *
	 * This hybrid model with both live expressions and a non-event driven map
	 * may not be ideal, but to avoid regressions in implementation it is kept
	 * as it is for now, with the observation that the map needs to be updated
	 * first before updating the live variable.
	 *
	 */
	public void setUrl(String url) {
		put(URL_PROP, url);

		this.url.setValue(url);
	}

	public void setSpace(CFSpace space) {

		if (space != null) {
			put(ORG_PROP, space.getOrganization().getName());
			put(ORG_GUID, space.getOrganization().getGuid().toString());
			put(SPACE_PROP, space.getName());
			put(SPACE_GUID, space.getGuid().toString());
		} else {
			put(ORG_PROP, null);
			put(ORG_GUID, null);
			put(SPACE_PROP, null);
			put(SPACE_GUID, null);
		}
		this.space.setValue(space);
	}

	public void setSelfsigned(boolean selfsigned) {
		put(SELF_SIGNED_PROP, Boolean.toString(selfsigned));

		this.selfsigned.setValue(selfsigned);
	}

	public void setUsername(String userName) {
		put(USERNAME_PROP, userName);

		this.userName.setValue(userName);
	}

	public void setPassword(String password) throws CannotAccessPropertyException {
		if (get(TargetProperties.RUN_TARGET_ID) == null) {
			this.password.setValue(password);
		} else {
			super.setPassword(password);
		}
	}

	@Override
	public String getPassword() throws CannotAccessPropertyException {
		if (get(TargetProperties.RUN_TARGET_ID) == null) {
			return password.getValue();
		} else {
			return super.getPassword();
		}
	}

	protected String getDefaultTargetUrl() {
		return "https://api.run.pivotal.io";
	}

	public OrgsAndSpaces resolveSpaces(IRunnableContext context) throws Exception {
		OrgsAndSpaces spaces = clientFactory.getCloudSpaces(this, context);
		allSpaces.setValue(spaces);
		return allSpaces.getValue();

	}

	public OrgsAndSpaces getSpaces() {
		return allSpaces.getValue();
	}

	protected RunTarget getExistingRunTarget(CFSpace space) {
		if (space != null) {
			String targetId = CloudFoundryTargetProperties.getId(getUsername(), getUrl(),
					space.getOrganization().getName(), space.getName());
			for (RunTarget target : existingTargets) {
				if (targetId.equals(target.getId())) {
					return target;
				}
			}
		}
		return null;
	}

	public CloudFoundryRunTarget finish() throws Exception {
		String id = CloudFoundryTargetProperties.getId(this);
		put(TargetProperties.RUN_TARGET_ID, id);
		super.setPassword(password.getValue());
		return (CloudFoundryRunTarget) getRunTargetType().createRunTarget(this);
	}

	class CredentialsValidator extends Validator {
		@Override
		protected ValidationResult compute() {
			String infoMessage = null;

			if (isEmpty(getUsername())) {
				infoMessage = "Enter a username";
			} else if (isEmpty(password.getValue())) {
				infoMessage = "Enter a password";
			} else if (isEmpty(getUrl())) {
				infoMessage = "Enter a target URL";
			} else {
				try {
					new URL(getUrl());
				} catch (MalformedURLException e) {
					return ValidationResult.error(e.getMessage());
				}
			}
			if (infoMessage != null) {
				return ValidationResult.info(infoMessage);
			}

			return ValidationResult.OK;
		}

		protected boolean isEmpty(String value) {
			return value == null || value.trim().length() == 0;
		}
	}

	class CloudSpaceValidator extends Validator {

		@Override
		protected ValidationResult compute() {
			if (getSpaceName() == null || getOrganizationName() == null) {
				return ValidationResult.info("Select a Cloud space");
			}

			if (space.getValue() != null) {
				RunTarget existing = CloudFoundryTargetWizardModel.this.getExistingRunTarget(space.getValue());
				if (existing != null) {
					return ValidationResult.error("A run target for that space already exists: '" + existing.getName()
							+ "'. Please select another space.");
				}
			}
			return ValidationResult.OK;
		}
	}

	class OrgsSpacesValidator extends Validator {
		@Override
		protected ValidationResult compute() {

			if (allSpaces.getValue() == null || allSpaces.getValue().getAllSpaces() == null) {
				return ValidationResult.info("Enter credentials and select a space to validate the credentials.");
			}

			if (allSpaces.getValue().getAllSpaces().isEmpty()) {
				return ValidationResult.error(
						"No spaces available to select. Please check that the credentials and target URL are correct, and spaces are defined in the target.");
			}

			return ValidationResult.OK;
		}
	}

	/**
	 * @return A 'complete' validator that reflects the validation state of all the inputs in this 'ui'.
	 */
	public LiveExpression<ValidationResult> getValidator() {
		return allPropertiesValidator;
	}
}
