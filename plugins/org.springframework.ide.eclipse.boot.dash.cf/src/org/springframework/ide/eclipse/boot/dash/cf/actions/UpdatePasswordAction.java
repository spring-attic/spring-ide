/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.ops.ConnectOperation;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class UpdatePasswordAction extends AbstractCloudDashModelAction {

	public UpdatePasswordAction(LiveExpression<BootDashModel> sectionSelection, SimpleDIContext context) {
		super(sectionSelection, context);
		this.setText("Update Password");
		this.setToolTipText("Update password locally for the selected target.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/update_password.png"));
	}

	CompletableFuture<Void> lastRunCompletion = null;

	/**
	 * This is meant for test-code only, so that test can we wait for the action (part f which is executed in
	 * a job) to complete.
	 */
	public void waitFor() throws InterruptedException, ExecutionException {
		if (lastRunCompletion!=null) {
			lastRunCompletion.get();
		}
	}

	@Override
	public void run() {
		CompletableFuture<Void> lastRunCompletion = this.lastRunCompletion = new CompletableFuture<>();
		try {
			final CloudFoundryBootDashModel targetModel = (CloudFoundryBootDashModel) sectionSelection.getValue();
			final CloudFoundryRunTarget runTarget = targetModel.getRunTarget();
			if (runTarget!=null) {
				final String targetId = runTarget.getId();
				final StoreCredentialsMode storePassword = runTarget.getTargetProperties().getStoreCredentials();
				Job job = new Job("Updating password") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						PasswordDialogModel passwordDialogModel = new PasswordDialogModel(runTarget.getClientFactory(), runTarget.getTargetProperties(), storePassword);
						cfUi().openPasswordDialog(passwordDialogModel);
						if (passwordDialogModel.isOk()) {
							runTarget.getTargetProperties().setStoreCredentials(
									passwordDialogModel.getEffectiveStoreMode());
							CFCredentials credentials = passwordDialogModel.getCredentials();
							// The credentials cannot be null or empty string - enforced by the dialog
							try {
								runTarget.getTargetProperties().setCredentials(credentials);
							} catch (CannotAccessPropertyException e) {
								ui().warningPopup("Failed Storing Password",
										"Failed to store password in Secure Storage for " + targetId
												+ ". Secure Storage is most likely locked. Current password will be kept until disconnect.");
								// Set "remember password" to false. Password hasn't been stored.
								runTarget.getTargetProperties().setStoreCredentials(StoreCredentialsMode.STORE_NOTHING);
							}

							try {
								if (targetModel.isConnected()) {
									// Disconnect if connected
									CFCredentials savedCreds = runTarget.getTargetProperties().getCredentials();
									new ConnectOperation(targetModel, false, context).run(monitor);
									// Disconnect will wipe out password if it's not stored, so reset it below.
									runTarget.getTargetProperties().setCredentials(savedCreds);
								}
								new ConnectOperation(targetModel, true, context).run(monitor);
							} catch (Exception e) {
								targetModel.setBaseRefreshState(RefreshState.error(e));
								ui().errorPopup("Failed Setting Credentials", "Credentials for " + targetId
										+ " are not valid. Ensure credentials are correct.");
							}

							// launch refresh if disconnected it would just clear out children
							targetModel.refresh(ui());
						}
						lastRunCompletion.complete(null);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		} catch (Exception e) {
			Log.log(e);
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
