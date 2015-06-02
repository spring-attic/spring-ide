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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
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

	public static ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configs, String dialogTitle, String message, Shell shell) {
		if (configs.size()==1) {
			return configs.get(0);
		} else if (configs.size()>0) {
			IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
			try {
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
			} finally {
				labelProvider.dispose();
			}
		}
		return null;
	}

	public static IType chooseMainType(IType[] mainTypes, String title, String message, Shell shell) {
		if (mainTypes.length==1) {
			return mainTypes[0];
		} else if (mainTypes.length>0) {
			IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
			try {
				ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, labelProvider);
				dialog.setElements(mainTypes);
				dialog.setTitle(title);
				dialog.setMessage(message);
				dialog.setMultipleSelection(false);
				int result = dialog.open();
				labelProvider.dispose();
				if (result == Window.OK) {
					return (IType) dialog.getFirstResult();
				}
				return null;
			} finally {
				labelProvider.dispose();
			}
		}
		return null;
	}

}
