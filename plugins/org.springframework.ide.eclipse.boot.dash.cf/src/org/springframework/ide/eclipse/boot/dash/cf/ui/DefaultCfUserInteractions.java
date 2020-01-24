package org.springframework.ide.eclipse.boot.dash.cf.ui;

import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.dialogs.CustomizeAppsManagerURLDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;
import org.springframework.ide.eclipse.boot.dash.views.UpdatePasswordDialog;

public class DefaultCfUserInteractions implements CfUserInteractions {

	private final SimpleDIContext context;

	public DefaultCfUserInteractions(SimpleDIContext context) {
		this.context = context;
	}

	private Shell getShell() {
		return context.getBean(UIContext.class).getShell();
	}

	@Override
	public void openPasswordDialog(PasswordDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				new UpdatePasswordDialog(getShell(), model).open();
			}
		});
	}

	@Override
	public void openEditAppsManagerURLDialog(CustomizeAppsManagerURLDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				new CustomizeAppsManagerURLDialog(model, getShell()).open();
			}
		});
	}
}
