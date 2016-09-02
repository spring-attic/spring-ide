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

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

import org.springsource.ide.eclipse.commons.livexp.core.Validator;

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
					String storedString = credentials.getPassword();
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
					String storedString = credentials.getRefreshToken();
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

	final private String fUser;
	final private String fTargetId;
	final private LiveVariable<String> fPasswordVar;
	final private LiveVariable<StoreCredentialsMode> fStoreVar;
	private boolean okButtonPressed = false;
	private Validator passwordValidator;

	public PasswordDialogModel(String user, String targetId, String password, StoreCredentialsMode secureStore) {
		super();
		fUser = user;
		fTargetId = targetId;
		fPasswordVar = new LiveVariable<>(password);
		fStoreVar = new LiveVariable<>(secureStore);
	}

	public PasswordDialogModel(String user, String targetId, StoreCredentialsMode secureStore) {
		this(user, targetId, null, secureStore);
	}

	public String getUser() {
		return fUser;
	}

	public String getTargetId() {
		return fTargetId;
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
				}
				@Override
				protected ValidationResult compute() {
					String pw = fPasswordVar.getValue();
					if (!StringUtil.hasText(pw)) {
						return ValidationResult.error("Password can not be empty");
					}
					return ValidationResult.info("Please press 'OK' to set the password.");
				}
			};
		}
		return passwordValidator;
	}

}
