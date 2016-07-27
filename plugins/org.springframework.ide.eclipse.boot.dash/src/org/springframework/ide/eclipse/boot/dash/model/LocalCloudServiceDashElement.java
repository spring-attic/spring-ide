package org.springframework.ide.eclipse.boot.dash.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.cli.LocalCloudServiceLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.util.RunStateTracker.RunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

import com.google.common.collect.ImmutableSet;

public class LocalCloudServiceDashElement extends WrappingBootDashElement<String> {

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME, BootDashColumn.LIVE_PORT, BootDashColumn.RUN_STATE_ICN};

	private LiveExpression<RunState> runState;
	private LiveExpression<Integer> livePort;

	public LocalCloudServiceDashElement(BootDashModel bootDashModel, String id) {
		super(bootDashModel, id);
		this.runState = createRunStateExp();
		this.livePort = createLivePortExp(runState, "local.server.port");
		addElementNotifier(runState);
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
		stop(false);
	}

	private ImmutableSet<ILaunch> getLaunches() {
		List<ILaunch> launches = new ArrayList<>();
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(LocalCloudServiceLaunchConfigurationDelegate.ID);
		for (ILaunch launch : launchManager.getLaunches()) {
			ILaunchConfiguration configuration = launch.getLaunchConfiguration();
			try {
				if (configuration.getType() == type && delegate.equals(configuration.getAttribute(LocalCloudServiceLaunchConfigurationDelegate.ATTR_CLOUD_SERVICE_ID, (String) null))) {
					launches.add(launch);
				}
			} catch (CoreException e) {
				Log.log(e);
			}
		}
		return ImmutableSet.copyOf(launches);
	}

	private void stop(boolean sync) throws Exception {
		final CompletableFuture<Void> done = sync ? new CompletableFuture<>() : null;
		ImmutableSet<ILaunch> launches = getLaunches();
		if (sync) {
			LaunchUtils.whenTerminated(launches, new Runnable() {
				public void run() {
					done.complete(null);
				}
			});
		}
		for (ILaunch launch : launches) {
			launch.terminate();
		}
		if (sync) {
			//Eclipse waits for 5 seconds before timing out. So we use a similar timeout but slightly
			// larger. Windows case termination seem to fail silently sometimes so its up to us
			// to handle here.
			done.get(6, TimeUnit.SECONDS);
		}
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		stop(true);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				DebugUITools.launch(getActiveConfig(), ILaunchManager.RUN_MODE);
			}
		});
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
		final LocalServiceRunStateTracker tracker = getBootDashModel().getLaunchConfLocalServiceRunStateTracker();
		final LiveExpression<RunState> exp = new LiveExpression<RunState>() {
			protected RunState compute() {
				return tracker.getState(delegate);
			}

			@Override
			public void dispose() {
				super.dispose();
			}
		};
		final RunStateListener<String> runStateListener = new RunStateListener<String>() {
			@Override
			public void stateChanged(String serviceId) {
				if (delegate.equals(serviceId)) {
					exp.refresh();
				}
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

	@Override
	public LocalBootDashModel getBootDashModel() {
		return (LocalBootDashModel) super.getBootDashModel();
	}



}
