package org.springframework.ide.eclipse.boot.dash.cf.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ConnectOperation;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class UpdatePasswordHandler extends AbstractHandler {

	//	public UpdatePasswordAction(LiveExpression<BootDashModel> sectionSelection,
	//			SimpleDIContext context) {
	//		super(sectionSelection, context);
	//		this.setText("Update Password");
	//		this.setToolTipText("Update password locally for the selected target.");
	//		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/update_password.png"));
	//	}

	private CfUserInteractions cfUi() {
		return injections().getBean(CfUserInteractions.class);
	}

	private UserInteractions ui() {
		return injections().getBean(UserInteractions.class);
	}

	private SimpleDIContext injections() {
		return BootDashActivator.getDefault().getModel().getContext().injections;
	}

	private CloudFoundryBootDashModel selection(ExecutionEvent event) {
		IStructuredSelection s = HandlerUtil.getCurrentStructuredSelection(event);
		return (CloudFoundryBootDashModel) s.getFirstElement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final CloudFoundryBootDashModel targetModel = selection(event);
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
									new ConnectOperation(targetModel, false, injections()).run(monitor);
									// Disconnect will wipe out password if it's not stored, so reset it below.
									runTarget.getTargetProperties().setCredentials(savedCreds);
								}
								new ConnectOperation(targetModel, true, injections()).run(monitor);
							} catch (Exception e) {
								targetModel.setBaseRefreshState(RefreshState.error(e));
								ui().errorPopup("Failed Setting Credentials", "Credentials for " + targetId
										+ " are not valid. Ensure credentials are correct.");
							}

							// launch refresh if disconnected it would just clear out children
							targetModel.refresh(ui());
						}
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
