package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class OpenLaunchConfigAction extends AbstractBootDashAction {

	public OpenLaunchConfigAction(BootDashView owner) {
		super(owner);
		this.setText("Open Launch Config");
		this.setToolTipText("Open launch config associated with the selected element, if one exists.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj_disabled.gif"));
	}

	@Override
	public void run() {
		Collection<BootDashElement> selecteds = owner.getSelectedElements();
		for (BootDashElement bootDashElement : selecteds) {
			bootDashElement.openConfig(owner.getSite().getShell());
		}
	}

}
