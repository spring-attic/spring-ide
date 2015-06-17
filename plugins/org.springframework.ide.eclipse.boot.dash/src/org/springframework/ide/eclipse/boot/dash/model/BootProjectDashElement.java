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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.util.LaunchUtil;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ResolveableFuture;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

/**
 * Concrete BootDashElement that wraps an IProject
 *
 * @author Kris De Volder
 */
public class BootProjectDashElement extends WrappingBootDashElement<IProject> {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
	private BootDashModel context;

	public BootProjectDashElement(IProject project, BootDashModel context) {
		super(project);
		this.context = context;
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
		return runStateTracker().getState(getProject());
	}

	private ProjectRunStateTracker runStateTracker() {
		return context.getRunStateTracker();
	}

	@Override
	public RunTarget getTarget() {
		return RunTargets.LOCAL;
	}

	@Override
	public void restart(RunState runningOrDebugging, UserInteractions ui) throws Exception {
		switch (runningOrDebugging) {
		case RUNNING:
			restart(ILaunchManager.RUN_MODE, ui);
			break;
		case DEBUGGING:
			restart(ILaunchManager.DEBUG_MODE, ui);
			break;
		default:
			throw new IllegalArgumentException("Restart expects RUNNING or DEBUGGING as 'goal' state");
		}
	}

	public void restart(final String runMode, UserInteractions ui) throws Exception {
		stopSync();
		start(runMode, ui);
	}

	private void start(final String runMode, UserInteractions ui) {
		try {
			List<ILaunchConfiguration> configs = getTarget().getLaunchConfigs(this);
			ILaunchConfiguration conf = null;
			if (configs.isEmpty()) {
				IType mainType = chooseMainType(ui);
				if (mainType!=null) {
					RunTarget target = getTarget();
					IJavaProject jp = getJavaProject();
					conf = target.createLaunchConfig(jp, mainType);
				}
			} else {
				conf = chooseConfig(ui, configs);
			}
			if (conf!=null) {
				launch(runMode, conf);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	protected void launch(final String runMode, final ILaunchConfiguration conf) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				DebugUITools.launch(conf, runMode);
			}
		});
	}

	private IType chooseMainType(UserInteractions ui) throws CoreException {
		IType[] mainTypes = guessMainTypes();
		if (mainTypes.length==0) {
			ui.errorPopup("Problem launching", "Couldn't find a main type in '"+getName()+"'");
			return null;
		} else if (mainTypes.length==1){
			return mainTypes[0];
		} else {
			return ui.chooseMainType(mainTypes, "Choose Main Type", "Choose main type for '"+getName()+"'");
		}
	}

	/**
	 * Shouldn't really by public, it is only public to make easier to test this class.
	 */
	protected IType[] guessMainTypes() throws CoreException {
		return MainTypeFinder.guessMainTypes(getJavaProject(), new NullProgressMonitor());
	}

	@Override
	public void stopAsync() {
		try {
			stop(false);
		} catch (Exception e) {
			//Asynch case shouldn't really throw exceptions.
			BootActivator.log(e);
		}
	}

	public void stopSync() throws Exception {
		try {
			stop(true);
		} catch (TimeoutException e) {
			BootActivator.info("Termination of '"+this.getName()+"' timed-out. Retrying");
			//Try it one more time. On windows this times out occasionally... and then
			// it works the next time.
			stop(true);
		}
	}

	private void stop(boolean sync) throws Exception {
		debug("Stopping: "+this+" "+(sync?"...":""));
		final ResolveableFuture<Void> done = sync?new ResolveableFuture<Void>():null;
		try {
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
			} catch (Exception e) {
				//why does terminating process with Eclipse debug UI fail so #$%# often?
				BootActivator.log(new Error("Termination of "+this+" failed", e));
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		if (sync) {
			//Eclipse waits for 5 seconds before timing out. So we use a similar timeout but slightly
			// larger. Windows case termination seem to fail silently sometimes so its up to us
			// to handle here.
			done.get(6, TimeUnit.SECONDS);
			debug("Stopping: "+this+" "+"DONE");
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
	public void openConfig(UserInteractions ui) {
		try {
			IProject p = getProject();
			RunTarget target = getTarget();
			if (p!=null) {
				ILaunchConfiguration conf;
				List<ILaunchConfiguration> configs = target.getLaunchConfigs(this);
				if (configs.isEmpty()) {
					conf = createLaunchConfigForEditing();
				} else {
					conf = chooseConfig(ui, configs);
				}
				if (conf!=null) {
					ui.openLaunchConfigurationDialogOnGroup(conf, getLaunchGroup());
				}
			}
		} catch (Exception e) {
			ui.errorPopup("Couldn't open config for "+getName(), ExceptionUtil.getMessage(e));
			BootActivator.log(e);
		}
	}

	protected ILaunchConfiguration createLaunchConfigForEditing() throws Exception {
		IJavaProject jp = getJavaProject();
		RunTarget target = getTarget();
		IType[] mainTypes = guessMainTypes();
		return target.createLaunchConfig(jp, mainTypes.length==1?mainTypes[0]:null);
	}

	protected ILaunchConfiguration chooseConfig(UserInteractions ui, List<ILaunchConfiguration> configs) {
		ILaunchConfiguration preferredConf = getConfig();
		if (preferredConf!=null && configs.contains(preferredConf)) {
			return preferredConf;
		}
		ILaunchConfiguration conf = chooseConfigurationDialog(configs,
				"Choose Launch Configuration",
				"Several launch configurations are associated with '"+getName()+"' "+
				"Choose one.", ui);
		if (conf!=null) {
			setConfig(conf);
		}
		return conf;
	}

	private ILaunchConfiguration chooseConfigurationDialog(List<ILaunchConfiguration> configs, String dialogTitle, String message, UserInteractions ui) {
		if (configs.size()==1) {
			return configs.get(0);
		} else if (configs.size()>0) {
			ILaunchConfiguration chosen = ui.chooseConfigurationDialog(dialogTitle, message, configs);
			return chosen;
		}
		return null;
	}

	private String getLaunchGroup() {
		switch (getRunState()) {
		case RUNNING:
			return IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
		case DEBUGGING:
			return IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		default:
			return IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		}
	}

	@Override
	public ILaunchConfiguration getConfig() {
		return context.getPreferredConfigs(this);
	}

	@Override
	public void setConfig(ILaunchConfiguration config) {
		context.setPreferredConfig(this, config);
	}

	@Override
	public int getLivePort() {
		//TODO: provide a real implementation
		return 1234;
	}


}
