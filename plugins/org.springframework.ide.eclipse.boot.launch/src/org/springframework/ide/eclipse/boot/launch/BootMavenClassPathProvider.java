/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.m2e.jdt.internal.launch.MavenRuntimeClasspathProvider;

@SuppressWarnings("restriction")
public class BootMavenClassPathProvider extends MavenRuntimeClasspathProvider {

	/**
	 * Copy a given launch config into a 'clone' that has all the same attributes but
	 * a different type id.
	 */
	private static ILaunchConfigurationWorkingCopy copyAs(ILaunchConfiguration conf,
			String newType) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigurationType = launchManager
				.getLaunchConfigurationType(newType);
		ILaunchConfigurationWorkingCopy wc = launchConfigurationType.newInstance(null,
				launchManager.generateLaunchConfigurationName(conf.getName()));
		wc.setAttributes(conf.getAttributes());
		return wc;
	}



	@Override
	protected int getArtifactScope(ILaunchConfiguration configuration) throws CoreException {
		//Trick superclass in executing the right logic (i.e. as if this is a plain JDT launch config)
		return super.getArtifactScope(copyAs(configuration, JDT_JAVA_APPLICATION));
	}
}
