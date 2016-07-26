package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.cli.LocalCloudServiceLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.RunStateTracker.RunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

public class LocalCloudServiceDashElement extends WrappingBootDashElement<String> {

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME, BootDashColumn.LIVE_PORT, BootDashColumn.RUN_STATE_ICN};

	private LiveExpression<RunState> runState;
	private LiveExpression<Integer> livePort;

	public LocalCloudServiceDashElement(BootDashModel bootDashModel, String id) {
		super(bootDashModel, id);
		this.runState = createRunStateExp();
		this.livePort = createLivePortExp(runState, "local.server.port");
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		return runState.getValue();
	}

	@Override
	public RunTarget getTarget() {
		return getBootDashModel().getRunTarget();
	}

	@Override
	public int getLivePort() {
		return livePort.getValue();
	}

	@Override
	public String getLiveHost() {
		return runState.getValue() == RunState.RUNNING ? "localhost" : null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(LocalCloudServiceLaunchConfigurationDelegate.ID);
		try {
			for (ILaunchConfiguration config : launchManager.getLaunchConfigurations(type)) {
				if (delegate.equals(config.getAttribute(LocalCloudServiceLaunchConfigurationDelegate.ATTR_CLOUD_SERVICE_ID, (String) null))) {
					return config;
				}
			}
		} catch (CoreException e) {
			Log.log(e);
		}
		// There is no launch config for the service found, create it and return it then.
		try {
			ILaunchConfigurationWorkingCopy config = type.newInstance(null, getName());
			config.setAttribute(LocalCloudServiceLaunchConfigurationDelegate.ATTR_CLOUD_SERVICE_ID, delegate);
			return config;
		} catch (CoreException e) {
			Log.log(e);
		}
		return null;
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void openConfig(UserInteractions ui) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getActualInstances() {
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		return 0;
	}

	@Override
	public Object getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return delegate;
	}

	@Override
	public BootDashColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	private LiveExpression<Integer> createLivePortExp(final LiveExpression<RunState> runState, final String propName) {
		AsyncLiveExpression<Integer> exp = new AsyncLiveExpression<Integer>(-1, "Refreshing port info ("+propName+") for "+getName()) {
			{
				//Doesn't really depend on runState, but should be recomputed when runState changes.
				dependsOn(runState);
			}
			@Override
			protected Integer compute() {
				return -1;
			}
		};
		addDisposableChild(exp);
		return exp;
	}

	private LiveExpression<RunState> createRunStateExp() {
		final LaunchConfRunStateTracker tracker = runStateTracker();
		final LiveExpression<RunState> exp = new LiveExpression<RunState>() {
			protected RunState compute() {
				return RunState.INACTIVE;
			}

			@Override
			public void dispose() {
				super.dispose();
			}
		};
		final RunStateListener<ILaunchConfiguration> runStateListener = new RunStateListener<ILaunchConfiguration>() {
			@Override
			public void stateChanged(ILaunchConfiguration changedConf) {
			}
		};
		tracker.addListener(runStateListener);
		exp.onDispose(new DisposeListener() {
			public void disposed(Disposable disposed) {
				tracker.removeListener(runStateListener);
			}
		});
		addDisposableChild(exp);
		exp.refresh();
		return exp;
	}

	private LaunchConfRunStateTracker runStateTracker() {
		return getBootDashModel().getLaunchConfRunStateTracker();
	}

	@Override
	public LocalBootDashModel getBootDashModel() {
		return (LocalBootDashModel) super.getBootDashModel();
	}



}
