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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.util.LaunchUtil;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ResolveableFuture;
import org.springframework.ide.eclipse.boot.launch.BootLaunchShortcut;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

/**
 * Concrete BootDashElement that wraps an IProject
 */
public class BootProjectDashElement extends WrappingBootDashElement<IProject> {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
	private ProjectRunStateTracker runStateTracker;

	public BootProjectDashElement(IProject project, ProjectRunStateTracker runStateTracker) {
		super(project);
		this.runStateTracker = runStateTracker;
	}

	public IProject getProject() {
		return delegate;
	}

	@Override
	public IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	@Override
	public RunState getRunState() {
		return runStateTracker.getState(getProject());
	}

	@Override
	public RunTarget getTarget() {
		return RunTargets.LOCAL;
	}

	@Override
	public void restart(RunState runningOrDebugging) {
		switch (runningOrDebugging) {
		case RUNNING:
			restart(ILaunchManager.RUN_MODE);
			break;
		case DEBUGGING:
			restart(ILaunchManager.DEBUG_MODE);
			break;
		default:
			throw new IllegalArgumentException("Restart expects RUNNING or DEBUGGING as 'goal' state");
		}
	}

	public void restart(final String runMode) {
		stop(true);
		debug("starting "+this+"...");
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				BootLaunchShortcut.launch(getProject(), runMode);
				debug("starting "+this+" DONE");
			}
		});
	}

	@Override
	public void stop() {
		stop(false);
	}

	public void stop(boolean sync) {
		debug("Stopping: "+this+" "+(sync?"...":""));
		try {
			final ResolveableFuture<Void> done = sync?new ResolveableFuture<Void>():null;
			List<ILaunch> launches = LaunchUtil.getLaunches(getProject());
			if (sync) {
				LaunchUtils.whenTerminated(launches, new Runnable() {
					public void run() {
						done.resolve(null);
					}
				});
			}
			try {
				LaunchUtils.terminate(launches);
			} catch (DebugException e) {
				//why does terminating process with Eclipse debug UI fail so #$%# often?
				BootActivator.log(new Error("Termination of "+this+" failed", e));
			}
			if (sync) {
				done.get();
				debug("Stopping: "+this+" "+"DONE");
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	public static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	@Override
	public String getName() {
		return getProject().getName();
	}

	@Override
	public void openConfig(Shell shell) {
		IProject p = getProject();
		if (p!=null) {
			List<ILaunchConfiguration> configs = LaunchUtil.getBootLaunchConfigs(p);
			//TODO: if there's more than one, then how do we choose?
			if (!configs.isEmpty()) {
				ILaunchConfiguration conf = configs.get(0);
				IStructuredSelection selection = new StructuredSelection(new Object[] {conf});
				DebugUITools.openLaunchConfigurationDialogOnGroup(shell, selection, getLaunchGroup());
			}
		}
	}

	private String getLaunchGroup() {
		switch (getRunState()) {
		case RUNNING:
			return IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
		case DEBUGGING:
			return IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		default:
			//TODO: remember the last one used?
			return IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		}
	}
}
