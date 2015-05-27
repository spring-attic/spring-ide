/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

public class LaunchUtil {

	public static IProject getProject(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		if (conf!=null) {
			return BootLaunchConfigurationDelegate.getProject(conf);
		}
		return null;
	}

	public static boolean isDebugging(ILaunch launch) {
		return ILaunchManager.DEBUG_MODE.equals(launch.getLaunchMode());
	}

	public static List<ILaunch> getLaunches(IProject project) {
		ILaunch[] allLaunches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (allLaunches!=null && allLaunches.length>0) {
			List<ILaunch> launches = new ArrayList<ILaunch>();
			for (ILaunch launch : allLaunches) {
				if (project.equals(getProject(launch))) {
					launches.add(launch);
				}
			}
			return launches;
		}
		return Collections.emptyList();
	}

	/**
	 * Get all ILaunchConfigurations of the type for "Run As >> Spring Boot App" that are
	 * associated with a given project.
	 */
	public static List<ILaunchConfiguration> getBootLaunchConfigs(IProject p) {
		try {
			ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = lm.getLaunchConfigurationType(BootLaunchConfigurationDelegate.LAUNCH_CONFIG_TYPE_ID);
			if (type!=null) {
				ILaunchConfiguration[] configs = lm.getLaunchConfigurations();
				if (configs!=null && configs.length>0) {
					ArrayList<ILaunchConfiguration> result = new ArrayList<ILaunchConfiguration>();
					for (ILaunchConfiguration conf : configs) {
						if (p.equals(BootLaunchConfigurationDelegate.getProject(conf))) {
							result.add(conf);
						}
					}
					return result;
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
	}

	public static ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configs, String dialogTitle, String message, Shell shell) {
		if (configs.size()==1) {
			return configs.get(0);
		} else if (configs.size()>0) {
			IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
			ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, labelProvider);
			dialog.setElements(configs.toArray());
			dialog.setTitle(dialogTitle);
			dialog.setMessage(message);
			dialog.setMultipleSelection(false);
			int result = dialog.open();
			labelProvider.dispose();
			if (result == Window.OK) {
				return (ILaunchConfiguration) dialog.getFirstResult();
			}
			return null;
		}
		return null;
	}

}
