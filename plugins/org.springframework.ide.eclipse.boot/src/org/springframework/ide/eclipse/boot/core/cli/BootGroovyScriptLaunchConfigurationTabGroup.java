package org.springframework.ide.eclipse.boot.core.cli;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class BootGroovyScriptLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public BootGroovyScriptLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
//				new SpringCommandTab()
//				new AppletParametersTab(),
//				new JavaArgumentsTab(),
//				new JavaJRETab(),
//				new JavaClasspathTab(), 
//				new CommonTab()
		};
		setTabs(tabs);
		
	}

}
