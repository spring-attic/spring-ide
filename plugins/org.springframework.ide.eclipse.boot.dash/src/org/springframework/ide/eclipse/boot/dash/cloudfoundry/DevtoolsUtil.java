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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.devtools.BootDevtoolsClientLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

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

}
