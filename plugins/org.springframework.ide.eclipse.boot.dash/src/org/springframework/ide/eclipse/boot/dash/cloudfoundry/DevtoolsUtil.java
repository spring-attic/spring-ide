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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.devtools.BootDevtoolsClientLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

/**
 * @author Kris De Volder
 */
public class DevtoolsUtil {

	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private static ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(BootDevtoolsClientLaunchConfigurationDelegate.TYPE_ID);
	}


	private static ILaunchConfiguration createConfiguration(IProject project, String host, String secret) throws CoreException {
		ILaunchConfigurationType configType = getConfigurationType();
		String projectName = project.getName();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName("cf-devtools-client["+projectName+"]"));

		BootLaunchConfigurationDelegate.setProject(wc, project);
		BootDevtoolsClientLaunchConfigurationDelegate.setRemoteUrl(wc, remoteUrl(host));
		BootDevtoolsClientLaunchConfigurationDelegate.setRemoteSecret(wc, secret);

		wc.setMappedResources(new IResource[] {project});
		ILaunchConfiguration config = wc.doSave();
		return config;
	}

	public static String remoteUrl(String host) {
		return "http://"+host;
	}

	public static void launchDevtoolsDebugging(IProject project, String host, String debugSecret) throws CoreException {
		if (host==null) {
			throw ExceptionUtil.coreException("Can not launch devtools client: Host not specified");
		}
		ILaunchConfiguration conf = getOrCreateLaunchConfig(project, host, debugSecret);
		conf.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor());
	}

	private static ILaunchConfiguration getOrCreateLaunchConfig(IProject project, String host, String debugSecret) throws CoreException {
		ILaunchConfiguration existing = findConfig(project, host);
		if (existing!=null) {
			ILaunchConfigurationWorkingCopy wc = existing.getWorkingCopy();
			BootDevtoolsClientLaunchConfigurationDelegate.setRemoteSecret(wc, debugSecret);
			existing = wc.doSave();
		} else {
			existing = createConfiguration(project, host, debugSecret);
		}
		return existing;
	}

	private static ILaunchConfiguration findConfig(IProject project, String host) {
		String remoteUrl = remoteUrl(host);
		try {
			for (ILaunchConfiguration c : getLaunchManager().getLaunchConfigurations(getConfigurationType())) {
				if (project.equals(BootLaunchConfigurationDelegate.getProject(c))
					&& remoteUrl.equals(BootDevtoolsClientLaunchConfigurationDelegate.getRemoteUrl(c))) {
					return c;
				}
			}
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

	public static boolean isDebuggerAttached(BootDashElement bde) {
		if (bde.getTarget().getType()==RunTargetTypes.LOCAL) {
			//Clients really shouldn't ask this about local apps. We don't attach devtools debugger to them
			// (Well we can, buts is not really useful as there's much better ways to debug local apps).
			throw new IllegalArgumentException("This operation is not implemented for LOCAL runttargets");
		}
		IProject project = bde.getProject();
		if (project!=null) { // else not associated with a local project... can't really attach debugger then
			String host = bde.getLiveHost();
			if (host!=null) { // else app not running, can't attach debugger then
				ILaunchConfiguration conf = findConfig(project, host);
				return isDebugging(conf);
			}
		}
		return false;
	}

	private static boolean isDebugging(ILaunchConfiguration conf) {
		for (ILaunch launch : LaunchUtils.getLaunches(conf)) {
			if (!launch.isTerminated()) {
				if (ILaunchManager.RUN_MODE.equals(launch.getLaunchMode())) {
					return true;
				}
			}
		}
		return false;
	}

}
