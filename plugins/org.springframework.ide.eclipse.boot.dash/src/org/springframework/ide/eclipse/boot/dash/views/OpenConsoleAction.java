package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class OpenConsoleAction extends AbstractBootDashAction {

	public OpenConsoleAction(BootDashView owner) {
		super(owner);
		this.setText("Open Console");
		this.setToolTipText("Yada yada");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.gif"));
	}

	@Override
	public void run() {
		Collection<BootDashElement> selecteds = owner.getSelectedElements();
		MessageDialog.openInformation(owner.getSite().getShell(), "Test", "Open Console for: "+selecteds);
	}


}
