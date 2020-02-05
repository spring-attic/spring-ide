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
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFExceptions;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.MissingPasswordException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.RefreshSchedulingRule;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * Operation for connecting/disconnecting CF run target
 *
 * @author Alex Boyko
 *
 */
public class ConnectOperation extends CloudOperation {

	private boolean connect;
	private final SimpleDIContext context;

	public ConnectOperation(CloudFoundryBootDashModel model, boolean connect, SimpleDIContext context) {
		super("Connecting run target " + model.getRunTarget().getName(), model);
		this.connect = connect;
		this.context = context;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (model.getRunTarget() != null) {
			if (connect && !model.getRunTarget().isConnected()) {
				try {
					model.setBaseRefreshState(RefreshState.loading("Connecting..."));
					model.getRunTarget().connect();
					model.refresh(ui());
					model.getRunTarget().getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, null);
					model.getViewModel().updateTargetPropertiesInStore();
					model.setBaseRefreshState(RefreshState.READY);
					if (model.getRunTarget().getTargetProperties().getStoreCredentials()==StoreCredentialsMode.STORE_TOKEN) {
								//TODO: This special case doesn't seem like it should be necessary. Instead, any interaction with
						// client should publish refresh token as it changes and credentials should be updated in target
						// properties automatically any time there is a change.
						model.getRunTarget().getClient().getRefreshTokens().doOnNext(refreshToken -> {
							try {
								model.getRunTarget().getTargetProperties().setCredentials(CFCredentials.fromRefreshToken(refreshToken));
							} catch (CannotAccessPropertyException e) {
								Log.log(e);
							}
						})
						.subscribe();
					}

				} catch (MissingPasswordException|CannotAccessPropertyException|AssertionFailedException e) {
					model.setBaseRefreshState(RefreshState.READY);
					if (ui() == null) {
						Log.log(e);
					} else {
						CloudFoundryRunTarget runtarget = model.getRunTarget();
						PasswordDialogModel passwordDialogModel = new PasswordDialogModel(
								runtarget.getClientFactory(),
								runtarget.getTargetProperties(),
								runtarget.getTargetProperties().getStoreCredentials()
						);
						cfUi().openPasswordDialog(passwordDialogModel);
						if (passwordDialogModel.isOk()) {
							model.getRunTarget().getTargetProperties().setStoreCredentials(passwordDialogModel.getStoreVar().getValue());
							CFCredentials credentials = passwordDialogModel.getCredentials();
							// The password cannot be null or empty string - enforced by the dialog
							try {
								model.getRunTarget().getTargetProperties().setCredentials(credentials);
							} catch (CannotAccessPropertyException e1) {
								ui().warningPopup("Failed Storing Password",
										"Failed to store password in Secure Storage for " + passwordDialogModel.getTargetId()
												+ ". Secure Storage is most likely locked. Current password will be kept until disconnect.");
								// Set "remember password" to false. Password hasn't been stored.
								model.getRunTarget().getTargetProperties().setStoreCredentials(StoreCredentialsMode.STORE_NOTHING);
							}
							// At this point the password must be set otherwise an exception from the call above would be thrown
							doCloudOp(monitor);
						}
					}
				} catch (Exception e) {
					model.setBaseRefreshState(RefreshState.error(e));
					if (!context.hasDefinitionFor(UserInteractions.class)) {
						throw e;
					} else {
						String msg = "Failed to connect to " + model.getRunTarget().getId() + ". ";
						if (CFExceptions.isAuthFailure(e)) {
							msg = msg+ " Ensure login credentials are correct.";
						} else {
							msg = msg+ " See error log for detailed message.";
							Log.log(e);
						}
						ui().errorPopup("Cannot Connect to Cloud Foundry", msg);
					}
				}
			} else if (!connect && model.getRunTarget().isConnected()) {
				model.setBaseRefreshState(RefreshState.loading("Disconnecting..."));
				model.getRunTarget().disconnect();
				model.getRunTarget().getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, "true"); //$NON-NLS-1$
				if (!model.getRunTarget().getTargetProperties().isStoreCredentials()) {
					// Forget credentials on disconnect if it's not stored
					model.getRunTarget().getTargetProperties().setCredentials(null);
				}
				model.getViewModel().updateTargetPropertiesInStore();
				model.setBaseRefreshState(RefreshState.READY);
			}
		}
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}

}
