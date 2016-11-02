/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.EnumSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel.LoginMethod;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1.DefaultClientRequestsV1;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Password dialog model. Provides ability to specify password and whether it
 * needs to be stored.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class PasswordDialogModel implements OkButtonHandler {

	public static enum StoreCredentialsMode implements Ilabelable {

		STORE_PASSWORD {
			@Override
			public String getLabel() {
				return "Store Password";
			}

			@Override
			public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) throws CannotAccessPropertyException {
				try {
					String password = context.getSecuredCredentialsStore().getCredentials(secureStoreScopeKey(type.getName(), runTargetId));
					if (password!=null) {
						return CFCredentials.fromPassword(password);
					}
					return null;
				} catch (StorageException e) {
					throw new CannotAccessPropertyException("Failed to load credentials", e);
				}
			}

			@Override
			protected void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
				try {
					String storedString = credentials.getSecret();
					context.getSecuredCredentialsStore().setCredentials(secureStoreScopeKey(type.getName(), runTargetId), storedString);
				} catch (StorageException e) {
					throw new CannotAccessPropertyException("Failed to save credentials", e);
				}
			}

			@Override
			protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
				try {
					SecuredCredentialsStore store = context.getSecuredCredentialsStore();
					//Be careful and avoid annoying password popup just to erase data in a locked secure store.
					if (store.isUnlocked()) {
						store.setCredentials(secureStoreScopeKey(type.getName(), runTargetId), null);
					}
				} catch (StorageException e) {
					Log.log(e);
				}
			}

			private String secureStoreScopeKey(String targetTypeName, String targetId) {
				return targetTypeName+":"+targetId;
			}
		},

		STORE_TOKEN {
			@Override
			public String getLabel() {
				return "Store OAuth Token";
			}

			private String privateStoreKey(String targetType, String targetId) {
				return targetType+":"+targetId + ":token";
			}

			@Override
			public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
				String token = context.getPrivatePropertyStore().get(privateStoreKey(type.getName(), runTargetId));
				if (token!=null) {
					return CFCredentials.fromRefreshToken(token);
				}
				return null;
			}

			@Override
			public void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
				try {
					String storedString = credentials.getSecret();
					context.getPrivatePropertyStore().put(privateStoreKey(type.getName(), runTargetId), storedString);
				} catch (Exception e) {
					throw new CannotAccessPropertyException("Failed to save credentials", e);
				}
			}

			@Override
			protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
				try {
					IPropertyStore store = context.getPrivatePropertyStore();
					store.put(privateStoreKey(type.getName(), runTargetId), null);
				} catch (Exception e) {
					Log.log(e);
				}
			}
		},

		STORE_NOTHING {
			@Override
			public String getLabel() {
				return "Do NOT Store";
			}

			@Override
			public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
				return null;
			}

			@Override
			protected void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) {
				//nothing to do
			}

			@Override
			protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
				//nothing to do
			}
		};

		public abstract CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) throws CannotAccessPropertyException;
		protected abstract void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId);
		protected abstract void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException;

		public final void saveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
			for (StoreCredentialsMode mode : EnumSet.allOf(StoreCredentialsMode.class)) {
				if (mode==this) {
					mode.basicSaveCredentials(context, type, runTargetId, credentials);
				} else {
					mode.eraseCredentials(context, type, runTargetId);
				}
			}
		}
	}

	private static final ValidationResult REQUEST_VALIDATION_MESSAGE = ValidationResult.info(
			"Click the 'Validate' button to verify the credentials.");

	private static final ValidationResult VALIDATION_IN_PROGRESS_MESSAGE = ValidationResult.info(
			"Please wait. Concacting CF to verify the credentials..."
	);

	private CFClientParams currentParams;
	private CloudFoundryClientFactory clientFactory;

	private String refreshToken = null; //This is set when credentials are succesfully validated.
	private LiveVariable<Boolean> needValidation = new LiveVariable<>(true);

	private LiveVariable<ValidationResult> credentialsValidationResult;
	final private LiveVariable<LoginMethod> fMethod;
	final private LiveVariable<String> fPasswordVar;
	final private LiveVariable<StoreCredentialsMode> fStoreVar;
	private boolean okButtonPressed = false;
	private Validator passwordValidator;
	private LiveExpression<ValidationResult> storeValidator;

	/**
	 * This job handles the 'expensive' part of credential validation. We don't want
	 * to run this on every keystroke while user types password. So the validation
	 * is triggered by a button click.
	 */
	private Job validateCredentialsJob = new Job("Validate CF Credentials") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				credentialsValidationResult.setValue(validate());
			} catch (Exception e) {
				credentialsValidationResult.setValue(ValidationResult.error(ExceptionUtil.getMessage(e)));
			}
			return Status.OK_STATUS;
		}

		private ValidationResult validate() throws Exception {
			String secret = fPasswordVar.getValue();
			if (!StringUtil.hasText(secret)) {
				//Don't bother verifying empty passwords.
				return REQUEST_VALIDATION_MESSAGE;
			}
			CFCredentials creds = CFCredentials.fromLogin(fMethod.getValue(), secret);
			CFClientParams params = new CFClientParams(
					currentParams.getApiUrl(),
					currentParams.getUsername(),
					creds,
					currentParams.isSelfsigned(),
					null, null,
					currentParams.skipSslValidation()
			);
			ClientRequests client = clientFactory.getClient(params);
			String actualUserName = client.getUserName();
			if (!currentParams.getUsername().equals(actualUserName)) {
				return ValidationResult.error("The credentials belong to a different user!");
			}
			refreshToken = client.getRefreshToken();
			return ValidationResult.OK;
		}
	};

	private <T> void credentialsChangedHandler(LiveExpression<T> exp, T value) {
		needValidation.setValue(true);
	}

	public PasswordDialogModel(CloudFoundryClientFactory cfFactory, CFClientParams currentParams, StoreCredentialsMode storeMode) {
		super();
		this.clientFactory = cfFactory;
		this.currentParams = currentParams;
		fPasswordVar = new LiveVariable<>("");
		fStoreVar = new LiveVariable<>(storeMode);
		fMethod = new LiveVariable<>(LoginMethod.PASSWORD);
		storeValidator = makeStoreCredentialsValidator(getMethodVar(), fStoreVar);
		credentialsValidationResult = new LiveVariable<>(REQUEST_VALIDATION_MESSAGE);
		fMethod.addListener(this::credentialsChangedHandler);
		fPasswordVar.addListener(this::credentialsChangedHandler);
	}

	public String getUser() {
		return currentParams.getUsername();
	}

	public String getTargetId() {
		return CloudFoundryTargetProperties.getId(currentParams);
	}

	public LiveVariable<String> getPasswordVar() {
		return fPasswordVar;
	}

	public LiveVariable<StoreCredentialsMode> getStoreVar() {
		return fStoreVar;
	}

	public boolean isOk() {
		return okButtonPressed;
	}

	@Override
	public void performOk() throws Exception {
		okButtonPressed = true;
	}

	public LiveExpression<ValidationResult> getPasswordValidator() {
		if (passwordValidator==null) {
			passwordValidator = new Validator() {
				{
					dependsOn(fPasswordVar);
					dependsOn(needValidation);
					dependsOn(credentialsValidationResult);
				}
				@Override
				protected ValidationResult compute() {
					String pw = fPasswordVar.getValue();
					if (!StringUtil.hasText(pw)) {
						return ValidationResult.error("Password can not be empty");
					}
					if (needValidation.getValue()) {
						return REQUEST_VALIDATION_MESSAGE;
					}
					return credentialsValidationResult.getValue();
				}
			};
		}
		return passwordValidator;
	}

	public LiveExpression<ValidationResult> getStoreValidator() {
		return storeValidator;
	}

	/**
	 * Determines the 'effective' StoreCredentialsMode. This may be different
	 * from what the user explicitly chose. If the user choice is 'invalid'
	 * we ignore it (with a warning) and replace it with STORE_NOTHING.
	 */
	public StoreCredentialsMode getEffectiveStoreMode() {
		if (storeValidator.getValue().isOk()) {
			return fStoreVar.getValue();
		}
		return StoreCredentialsMode.STORE_NOTHING;
	}

	public static Validator makeStoreCredentialsValidator(LiveExpression<CloudFoundryTargetWizardModel.LoginMethod> method, LiveExpression<StoreCredentialsMode> storeCredentials ) {
		return new Validator() {
			{
				dependsOn(method);
				dependsOn(storeCredentials);
			}

			@Override
			protected ValidationResult compute() {
				if (
					method.getValue()==CloudFoundryTargetWizardModel.LoginMethod.TEMPORARY_CODE &&
					storeCredentials.getValue()==StoreCredentialsMode.STORE_PASSWORD
				) {
					return ValidationResult.warning("'Store Password' is useless for a 'Tempory Code'. This option will be ignored!");
				}
				return ValidationResult.OK;
			}
		};
	}

	/**
	 * Represents the 'Validate' button in the dialog. This method is called when user
	 * clicks that button. It triggers validation of credentials asynchronously.
	 */
	public void requestCredentialValidation() {
		needValidation.setValue(false);
		refreshToken = null;
		credentialsValidationResult.setValue(VALIDATION_IN_PROGRESS_MESSAGE);
		validateCredentialsJob.schedule();
	}

	public LiveVariable<LoginMethod> getMethodVar() {
		return fMethod;
	}

	public CFCredentials getCredentials() {
		String refreshToken = this.refreshToken;
		if (refreshToken==null) {
			throw new IllegalStateException("Credentials must be validated before retrieving them from the model");
		}
		//Produce credential object consistent with store credentials mode
		StoreCredentialsMode storeMode = getEffectiveStoreMode();
		switch (storeMode) {
		case STORE_NOTHING:
		case STORE_TOKEN:
			return CFCredentials.fromRefreshToken(refreshToken);
		case STORE_PASSWORD:
			return CFCredentials.fromPassword(fPasswordVar.getValue());
		default:
			throw new IllegalStateException("Bug! Missing case?");
		}
	}

}
