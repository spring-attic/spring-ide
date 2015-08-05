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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class UpdatePasswordAction extends AbstractBootDashModelAction {

	private BootDashViewModel model;

	public UpdatePasswordAction(LiveExpression<BootDashModel> sectionSelection, BootDashViewModel model,
			UserInteractions ui) {
		super(sectionSelection, ui);
		this.model = model;
		this.setText("Update Password");
		this.setToolTipText("Update password locally for the selected target.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/update_password.gif"));
	}

	@Override
	public void run() {
		final BootDashModel targetModel = sectionSelection.getValue();
		final RunTargetWithProperties runTarget = getCredentialsHolder(targetModel);
		if (runTarget!=null) {
			final String userName = runTarget.getTargetProperties().get(TargetProperties.USERNAME_PROP);
			final String targetId = runTarget.getId();
			Job job = new Job("Updating password") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					String password = ui.updatePassword(userName, targetId);
					if (password != null) {
						runTarget.getTargetProperties().put(TargetProperties.PASSWORD_PROP, password);
						try {
							runTarget.validate();

							// Only store if it validates
							model.updatePropertiesInStore(runTarget);

							// launch refresh if it validates
							targetModel.refresh();

						} catch (Exception e) {
							ui.errorPopup("Update Password Failure", "Credentials for " + targetId
									+ " are not valid. Please ensure that you entered the right credentials.");
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
