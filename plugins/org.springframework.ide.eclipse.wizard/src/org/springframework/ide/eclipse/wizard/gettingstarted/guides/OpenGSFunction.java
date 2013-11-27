package org.springframework.ide.eclipse.wizard.gettingstarted.guides;

import org.eclipse.ui.PlatformUI;
import org.springsource.ide.eclipse.commons.gettingstarted.dashboard.IDashboardFunction;

public class OpenGSFunction implements IDashboardFunction {

	public void call(String argument) {
		GSImportWizard.open(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), argument);
	}

}
