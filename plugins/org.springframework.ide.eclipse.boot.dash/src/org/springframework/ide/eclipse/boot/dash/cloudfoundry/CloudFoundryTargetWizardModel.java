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

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
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
	private LiveVariable<CloudSpace> space = new LiveVariable<CloudSpace>();
	private LiveVariable<Boolean> selfsigned = new LiveVariable<Boolean>(false);
	private LiveVariable<String> userName = new LiveVariable<String>();
	private LiveVariable<String> password = new LiveVariable<String>();

	private Validator credentialsValidator = new CredentialsValidator();
	private Validator spacesValidator = new SpacesValidator();
	private CompositeValidator allPropertiesValidator = new CompositeValidator();


	public CloudFoundryTargetWizardModel(RunTargetType runTargetType) {
		super(runTargetType);
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

		// Aggregate of the credentials and space validator.
		allPropertiesValidator.addChild(credentialsValidator);
		allPropertiesValidator.addChild(spacesValidator);

		setUrl(getDefaultTargetUrl());
	}

	/**
	 *
	 * @param allPropertiesValidationListener
	 *            listener that is notified when any property is validated
	 * @param credentialsValidationListener
	 *            listener that is notified when only credential properties are
	 *            validated (but not org/space)
	 * @param cloudSpaceChangeListener
	 *            listener that is notified when Cloud space is changed
	 */
	public void addListeners(ValueListener<ValidationResult> allPropertiesValidationListener,
			ValueListener<ValidationResult> credentialsValidationListener,
			ValueListener<CloudSpace> cloudSpaceChangeListener) {
		allPropertiesValidator.addListener(allPropertiesValidationListener);
		credentialsValidator.addListener(credentialsValidationListener);
		space.addListener(cloudSpaceChangeListener);
	}

	public void removeListeners(ValueListener<ValidationResult> allPropertiesValidationListener,
			ValueListener<ValidationResult> credentialsValidationListener,
			ValueListener<CloudSpace> cloudSpaceChangeListener) {
		allPropertiesValidator.removeListener(allPropertiesValidationListener);
		credentialsValidator.removeListener(credentialsValidationListener);
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

	public void setSpace(CloudSpace space) {

		if (space != null) {
			put(ORG_PROP, space.getOrganization().getName());
			put(ORG_GUID, space.getOrganization().getMeta().getGuid().toString());
			put(SPACE_PROP, space.getName());
			put(SPACE_GUID, space.getMeta().getGuid().toString());
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

	public void setPassword(String password) {
		put(PASSWORD_PROP, password);

		this.password.setValue(password);
	}

	class CredentialsValidator extends Validator {
		@Override
		protected ValidationResult compute() {
			String infoMessage = null;

			if (isEmpty(getUsername())) {
				infoMessage = "Enter a username";
			} else if (isEmpty(getPassword())) {
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

	protected String getDefaultTargetUrl() {
		return "https://api.run.pivotal.io";
	}

	class SpacesValidator extends Validator {

		@Override
		protected ValidationResult compute() {
			if (getSpaceName() == null || getOrganizationName() == null) {
				return ValidationResult.info("Select a Cloud space");
			}
			return ValidationResult.OK;
		}
	}

	public RunTarget finish() {
		put(TargetProperties.RUN_TARGET_ID, CloudFoundryTargetProperties.getId(this));
		return getRunTargetType().createRunTarget(this);
	}
}
