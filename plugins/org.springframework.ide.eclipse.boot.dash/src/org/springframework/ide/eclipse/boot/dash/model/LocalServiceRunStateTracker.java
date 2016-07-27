package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.cli.LocalCloudServiceLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.dash.util.RunStateTracker;
import org.springframework.ide.eclipse.boot.util.Log;

public class LocalServiceRunStateTracker extends RunStateTracker<String> {

	@Override
	protected String getOwner(ILaunch l) {
		try {
			ILaunchConfiguration configuration = l.getLaunchConfiguration();
			if (configuration.getType() == LocalCloudServiceLaunchConfigurationDelegate.TYPE) {
				return configuration.getAttribute(LocalCloudServiceLaunchConfigurationDelegate.ATTR_CLOUD_SERVICE_ID, (String) null);
			}
		} catch (CoreException e) {
			Log.log(e);
		}
		return null;
	}

	@Override
	protected boolean isInteresting(ILaunch l) {
		try {
			return l.getLaunchConfiguration().getType() == LocalCloudServiceLaunchConfigurationDelegate.TYPE;
		} catch (CoreException e) {
			Log.log(e);
		}
		return false;
	}

}
