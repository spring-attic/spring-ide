package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;


public class OpenLaunchConfigAction extends AbstractBootDashAction {

	private BootDashView owner;

	public OpenLaunchConfigAction(BootDashView owner) {
		super(owner);
		this.setText("Open Launch Config");
		this.setToolTipText("Yaday");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/an-icon.png"));
	}

	@Override
	public void run() {
		Collection<BootDashElement> selecteds = owner.getSelectedElements();
		MessageDialog.openInformation(owner.getSite().getShell(), "Test", "Open launc conf for: "+selecteds);
	}

}
