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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ConnectOperation;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class UpdatePasswordAction extends AbstractCloudDashModelAction {

	public UpdatePasswordAction(LiveExpression<BootDashModel> sectionSelection,
			UserInteractions ui) {
		super(sectionSelection, ui);
		this.setText("Update Password");
		this.setToolTipText("Update password locally for the selected target.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/update_password.gif"));
	}

	@Override
	public void run() {
		final CloudFoundryBootDashModel targetModel = (CloudFoundryBootDashModel) sectionSelection.getValue();
		final RunTargetWithProperties runTarget = getCredentialsHolder(targetModel);
		if (runTarget!=null) {
			final String userName = runTarget.getTargetProperties().get(TargetProperties.USERNAME_PROP);
			final String targetId = runTarget.getId();
			final StoreCredentialsMode storePassword = runTarget.getTargetProperties().getStoreCredentials();
			Job job = new Job("Updating password") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					PasswordDialogModel passwordDialogModel = new PasswordDialogModel(userName, targetId, storePassword);
					ui.openPasswordDialog(passwordDialogModel);
					if (passwordDialogModel.isOk()) {
						runTarget.getTargetProperties().setStoreCredentials(passwordDialogModel.getStoreVar().getValue());
						String password = passwordDialogModel.getPasswordVar().getValue();
						// The password cannot be null or empty string - enforced by the dialog
						// Do the check just in case for tests bypassing the UI
						if (password != null && !password.isEmpty()) {
							try {
								runTarget.getTargetProperties().setCredentials(CFCredentials.fromPassword(password));
							} catch (CannotAccessPropertyException e) {
								ui.warningPopup("Failed Storing Password",
										"Failed to store password in Secure Storage for " + targetId
												+ ". Secure Storage is most likely locked. Current password will be kept until disconnect.");
								// Set "remember password" to false. Password hasn't been stored.
								runTarget.getTargetProperties().setStoreCredentials(StoreCredentialsMode.STORE_NOTHING);
							}

							try {
								if (targetModel.isConnected()) {
									// Disconnect if connected
									CFCredentials savedCreds = runTarget.getTargetProperties().getCredentials();
									new ConnectOperation(targetModel, false, ui).run(monitor);
									// Disconnect will wipe out password if it's not stored, so reset it below.
									runTarget.getTargetProperties().setCredentials(savedCreds);
								}
								new ConnectOperation(targetModel, true, ui).run(monitor);
								if (runTarget.getTargetProperties().getStoreCredentials()==StoreCredentialsMode.STORE_TOKEN) {
									//TODO: This special case doesn't seem like it should be necessary. Instead, any interaction with
									// client should publish refresh token as it changes and credentials should be updated in target
									// properties automatically any time there is a change.
									String refreshToken = targetModel.getRunTarget().getClient().getRefreshToken();
									Assert.isNotNull(refreshToken);
									runTarget.getTargetProperties().setCredentials(CFCredentials.fromRefreshToken(refreshToken));
								}
							} catch (Exception e) {
								targetModel.setBaseRefreshState(RefreshState.error(e));
								ui.errorPopup("Failed Setting Password", "Credentials for " + targetId
										+ " are not valid. Ensure credentials are correct.");
							}

							// launch refresh if disconnected it would just clear out children
							targetModel.refresh(ui);
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(getCredentialsHolder(sectionSelection.getValue())!=null);
	}

	@Override
	public void updateVisibility() {
		setVisible(getCredentialsHolder(sectionSelection.getValue()) != null);
	}

	private RunTargetWithProperties getCredentialsHolder(BootDashModel section) {
		if (section!=null) {
			RunTarget target = section.getRunTarget();
			if (target instanceof RunTargetWithProperties) {
				RunTargetWithProperties targetWithProps = (RunTargetWithProperties) target;
				if (targetWithProps.requiresCredentials()) {
					return targetWithProps;
				}
			}
		}
		return null;
	}

}
