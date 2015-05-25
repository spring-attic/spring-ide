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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
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
	public void restart() {
		restart(ILaunchManager.RUN_MODE);
	}

	public void restart(final String runMode) {
		stop(true);
		debug("starting "+this+"...");
		Display.getDefault().asyncExec(new Runnable() {
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
}
