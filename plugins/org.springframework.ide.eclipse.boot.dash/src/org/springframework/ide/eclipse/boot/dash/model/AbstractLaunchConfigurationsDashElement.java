/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKTunnel;
import org.springframework.ide.eclipse.boot.dash.util.CollectionUtils;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.RunStateTracker.RunStateListener;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifeCycleClientManager;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifecycleClient;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

import com.google.common.collect.ImmutableSet;

/**
 * Abstracts out the commonalities between {@link BootProjectDashElement} and {@link LaunchConfDashElement}. Each can
 * be viewed as representing a collection of launch configuration.
 * <p>
 * A {@link BootProjectDashElement} element represents all the launch configurations associated with a given project whereas as
 * {@link LaunchConfDashElement} represent a single launch configuration (i.e. a singleton collection).
 *
 * @author Kris De Volder
 */
public abstract class AbstractLaunchConfigurationsDashElement<T> extends WrappingBootDashElement<T> implements Duplicatable<LaunchConfDashElement> {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	public static final EnumSet<RunState> READY_STATES = EnumSet.of(RunState.RUNNING, RunState.DEBUGGING);

	private LiveExpression<RunState> runState;
	private LiveExpression<Integer> livePort;
	private LiveExpression<Integer> actuatorPort;
	private LiveExpression<Integer> actualInstances;

	private PropertyStoreApi persistentProperties;

	private LiveExpression<URI> actuatorUrl;

	public AbstractLaunchConfigurationsDashElement(LocalBootDashModel bootDashModel, T delegate) {
		super(bootDashModel, delegate);
		this.runState = createRunStateExp();
		this.livePort = createLivePortExp(runState, "local.server.port");
		this.actuatorPort = createLivePortExp(runState, "local.management.port");
		this.actualInstances = createActualInstancesExp();
		addElementNotifier(livePort);
		addElementNotifier(runState);
		addElementNotifier(actualInstances);
	}

	protected abstract IPropertyStore createPropertyStore();

	@Override
	public abstract ImmutableSet<ILaunchConfiguration> getLaunchConfigs();

	@Override
	public abstract IProject getProject();

	@Override
	public abstract String getName();

	@Override
	public RunState getRunState() {
		return runState.getValue();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+getName()+")";
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
		return "localhost";
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		ILaunchConfiguration single = CollectionUtils.getSingle(getLaunchConfigs());
		if (single!=null) {
			return single;
		}
		return null;
	}

	@Override
	public void stopAsync(UserInteractions ui) {
		try {
			stop(false);
		} catch (Exception e) {
			//Asynch case shouldn't really throw exceptions.
			Log.log(e);
		}
	}

	private void stop(boolean sync) throws Exception {
		debug("Stopping: "+this+" "+(sync?"...":""));
		final CompletableFuture<Void> done = sync?new CompletableFuture<>():null;
		try {
			ImmutableSet<ILaunch> launches = getLaunches();
			if (sync) {
				LaunchUtils.whenTerminated(launches, new Runnable() {
					public void run() {
						done.complete(null);
					}
				});
			}
			try {
				BootLaunchUtils.terminate(launches);
				shutdownExpose();
			} catch (Exception e) {
				//why does terminating process with Eclipse debug UI fail so #$%# often?
				Log.log(new Error("Termination of "+this+" failed", e));
			}
		} catch (Exception e) {
			Log.log(e);
		}
		if (sync) {
			//Eclipse waits for 5 seconds before timing out. So we use a similar timeout but slightly
			// larger. Windows case termination seem to fail silently sometimes so its up to us
			// to handle here.
			done.get(6, TimeUnit.SECONDS);
			debug("Stopping: "+this+" "+"DONE");
		}
	}

	/**
	 * Get the launches associated with this element.
	 * <p>
	 * Note, we could implement it here by taking the union of all launches for all launch confs,
	 * but subclass can provide more efficient implementation so we make this abstract.
	 */
	protected abstract ImmutableSet<ILaunch> getLaunches();

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

	public void stopSync() throws Exception {
		try {
			stop(true);
		} catch (TimeoutException e) {
			Log.info("Termination of '"+this.getName()+"' timed-out. Retrying");
			//Try it one more time. On windows this times out occasionally... and then
			// it works the next time.
			stop(true);
		}
	}

	private void start(final String runMode, UserInteractions ui) {
		try {
			ImmutableSet<ILaunchConfiguration> configs = getLaunchConfigs();
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
			Log.log(e);
		}
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

	protected IType[] guessMainTypes() throws CoreException {
		return MainTypeFinder.guessMainTypes(getJavaProject(), new NullProgressMonitor());
	}

	protected void launch(final String runMode, final ILaunchConfiguration conf) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				DebugUITools.launch(conf, runMode);
			}
		});
	}

	@Override
	public void openConfig(UserInteractions ui) {
		try {
			IProject p = getProject();
			if (p!=null) {
				ILaunchConfiguration conf;
				ImmutableSet<ILaunchConfiguration> configs = getLaunchConfigs();
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
		}
	}

	@Override
	public boolean canDuplicate() {
		return getLaunchConfigs().size()==1;
	}

	@Override
	public LaunchConfDashElement duplicate(UserInteractions ui) {
		try {
			ILaunchConfiguration conf = CollectionUtils.getSingle(getLaunchConfigs());
			if (conf!=null) {
				ILaunchConfiguration newConf = BootLaunchConfigurationDelegate.duplicate(conf);
				return getBootDashModel().getLaunchConfElementFactory().createOrGet(newConf);
			}
		} catch (Exception e) {
			Log.log(e);
			ui.errorPopup("Couldn't duplicate config", ExceptionUtil.getMessage(e));
		}
		return null;
	}

	@Override
	public int getDesiredInstances() {
		//special case for no launch configs (a single launch conf is created on demand,
		//so we should treat it as if it already has one).
		return Math.max(1, getLaunchConfigs().size());
	}

	@Override
	public int getActualInstances() {
		return actualInstances.getValue();
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		if (persistentProperties==null) {
			IPropertyStore backingStore = createPropertyStore();
			this.persistentProperties = PropertyStoreFactory.createApi(backingStore);
		}
		return persistentProperties;
	}

	private LaunchConfRunStateTracker runStateTracker() {
		return getBootDashModel().getLaunchConfRunStateTracker();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected ILaunchConfiguration createLaunchConfigForEditing() throws Exception {
		IJavaProject jp = getJavaProject();
		RunTarget target = getTarget();
		IType[] mainTypes = guessMainTypes();
		return target.createLaunchConfig(jp, mainTypes.length==1?mainTypes[0]:null);
	}

	protected ILaunchConfiguration chooseConfig(UserInteractions ui, Collection<ILaunchConfiguration> configs) {
		//TODO: this should probably be removed. Actions etc. should either apply to all the elements at once,
		// or be disabled if that seems ill-conceived. In such a ui there should be no need to popup a dialog
		// to choose a configuration.
		ILaunchConfiguration conf = chooseConfigurationDialog(configs,
				"Choose Launch Configuration",
				"Several launch configurations are associated with '"+getName()+"' "+
				"Choose one.", ui);
		return conf;
	}

	private ILaunchConfiguration chooseConfigurationDialog(Collection<ILaunchConfiguration> configs, String dialogTitle, String message, UserInteractions ui) {
		if (configs.size()==1) {
			return CollectionUtils.getSingle(configs);
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

	public int getActuatorPort() {
		return actuatorPort.getValue();
	}

	private LiveExpression<RunState> createRunStateExp() {
		final LaunchConfRunStateTracker tracker = runStateTracker();
		final LiveExpression<RunState> exp = new LiveExpression<RunState>() {
			protected RunState compute() {
				AbstractLaunchConfigurationsDashElement<T> it = AbstractLaunchConfigurationsDashElement.this;
				debug("Computing runstate for "+it);
				LaunchConfRunStateTracker tracker = runStateTracker();
				RunState state = RunState.INACTIVE;
				for (ILaunchConfiguration conf : getLaunchConfigs()) {
					RunState confState = tracker.getState(conf);
					debug("state for conf "+conf+" = "+confState);
					state = state.merge(confState);
				}
				debug("runstate for "+it+" => "+state);
				return state;
			}

			@Override
			public void dispose() {
				super.dispose();
			}
		};
		final RunStateListener<ILaunchConfiguration> runStateListener = new RunStateListener<ILaunchConfiguration>() {
			@Override
			public void stateChanged(ILaunchConfiguration changedConf) {
				if (getLaunchConfigs().contains(changedConf)) {
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

	private LiveExpression<Integer> createActualInstancesExp() {
		final LaunchConfRunStateTracker tracker = runStateTracker();
		final LiveExpression<Integer> exp = new LiveExpression<Integer>(0) {
			protected Integer compute() {
				int activeCount = 0;
				for (ILaunchConfiguration c : getLaunchConfigs()) {
					if (READY_STATES.contains(tracker.getState(c))) {
						activeCount++;
					}
				}
				return activeCount;
			}
		};
		final RunStateListener<ILaunchConfiguration> runStateListener = new RunStateListener<ILaunchConfiguration>() {
			@Override
			public void stateChanged(ILaunchConfiguration changedConf) {
				if (getLaunchConfigs().contains(changedConf)) {
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

	private LiveExpression<Integer> createLivePortExp(final LiveExpression<RunState> runState, final String propName) {
		AsyncLiveExpression<Integer> exp = new AsyncLiveExpression<Integer>(-1, "Refreshing port info ("+propName+") for "+getName()) {
			{
				//Doesn't really depend on runState, but should be recomputed when runState changes.
				dependsOn(runState);
			}
			@Override
			protected Integer compute() {
				return getLivePort(propName);
			}
		};
		addDisposableChild(exp);
		return exp;
	}

	private int getLivePort(String propName) {
		ILaunchConfiguration conf = getActiveConfig();
		if (conf!=null && READY_STATES.contains(getRunState())) {
			if (BootLaunchConfigurationDelegate.canUseLifeCycle(conf)) {
				//TODO: what if there are several launches? Right now we ignore all but the first
				// non-terminated launch.
				for (ILaunch l : BootLaunchUtils.getLaunches(conf)) {
					if (!l.isTerminated()) {
						int jmxPort = BootLaunchConfigurationDelegate.getJMXPortAsInt(l);
						if (jmxPort>0) {
							SpringApplicationLifeCycleClientManager cm = null;
							try {
								cm = new SpringApplicationLifeCycleClientManager(() -> jmxPort);
								SpringApplicationLifecycleClient c = cm.getLifeCycleClient();
								if (c!=null) {
									//Just because lifecycle bean is ready does not mean that the port property has already been set.
									//To avoid race condition we should wait here until the port is set (some apps aren't web apps and
									//may never get a port set, so we shouldn't wait indefinitely!)
									return RetryUtil.retry(100, 1000, () -> {
										int port = c.getProperty(propName, -1);
										if (port<=0) {
											throw new IllegalStateException("port not (yet) set");
										}
										return port;
									});
								}
							} catch (Exception e) {
								debug(ExceptionUtil.getMessage(e));
								//most likely this just means the app isn't running so ignore
							} finally {
								if (cm!=null) {
									cm.disposeClient();
								}
							}
						}
					}
				}
			}
		}
		return -1;
	}

	@Override
	protected LiveExpression<URI> getActuatorUrl() {
		synchronized (this) {
			if (actuatorUrl==null) {
				actuatorUrl = new LiveExpression<URI>() {
					{
						dependsOn(actuatorPort);
					}
					protected URI compute() {
						try {
							Integer port = actuatorPort.getValue();
							if (port!=null && port>0) {
								return new URI("http://localhost:"+port);
							}
						} catch (URISyntaxException e) {
							Log.log(e);
						}
						return null;
					}
				};
				addDisposableChild(actuatorUrl);
			};
		}
		return actuatorUrl;
	}

	public void restartAndExpose(RunState runMode, NGROKClient ngrokClient, String eurekaInstance, UserInteractions ui) throws Exception {
		String launchMode = null;
		if (RunState.RUNNING.equals(runMode)) {
			launchMode = ILaunchManager.RUN_MODE;
		}
		else if (RunState.DEBUGGING.equals(runMode)) {
			launchMode = ILaunchManager.DEBUG_MODE;
		}
		else {
			throw new IllegalArgumentException("Restart and expose expects RUNNING or DEBUGGING as 'goal' state");
		}

		int port = getLivePort();
		stopSync();

		if (port <= 0) {
			port = SocketUtil.findFreePort();
		}

		String tunnelName = getName();

		NGROKTunnel tunnel = ngrokClient.startTunnel("http", Integer.toString(port));
		NGROKLaunchTracker.add(tunnelName, ngrokClient, tunnel);

		if (tunnel == null) {
			ui.errorPopup("ngrok tunnel not started", "there was a problem starting the ngrok tunnel, try again or start a tunnel manually.");
			return;
		}

		String tunnelURL = tunnel.getPublic_url();

		if (tunnelURL.startsWith("http://")) {
			tunnelURL = tunnelURL.substring(7);
		}

		Map<String, String> extraAttributes = new HashMap<>();
		extraAttributes.put("spring.boot.prop.server.port", "1" + Integer.toString(port));
		extraAttributes.put("spring.boot.prop.eureka.instance.hostname", "1" + tunnelURL);
		extraAttributes.put("spring.boot.prop.eureka.instance.nonSecurePort", "1" + "80");
		extraAttributes.put("spring.boot.prop.eureka.client.service-url.defaultZone", "1" + eurekaInstance);

		start(launchMode, ui, extraAttributes);
	}

	private void start(final String runMode, UserInteractions ui, Map<String, String> extraAttributes) {
		try {
			ImmutableSet<ILaunchConfiguration> configs = getLaunchConfigs();
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
				ILaunchConfigurationWorkingCopy workingCopy = conf.getWorkingCopy();

				removeOverriddenAttributes(workingCopy, extraAttributes);
				addAdditionalAttributes(workingCopy, extraAttributes);

				launch(runMode, workingCopy);
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

	private void addAdditionalAttributes(ILaunchConfigurationWorkingCopy workingCopy, Map<String, String> extraAttributes) {
		if (extraAttributes != null && extraAttributes.size() > 0) {
			Iterator<String> iterator = extraAttributes.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = extraAttributes.get(key);

				workingCopy.setAttribute(key, value);
			}
		}
	}

	private void removeOverriddenAttributes(ILaunchConfigurationWorkingCopy workingCopy, Map<String, String> attributesToOverride) {
		try {
			Map<String, Object> attributes = workingCopy.getAttributes();
			Set<String> keys = attributes.keySet();

			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String existingKey = iter.next();
				if (containsSimilarKey(attributesToOverride, existingKey)) {
					workingCopy.removeAttribute(existingKey);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private boolean containsSimilarKey(Map<String, String> attributesToOverride, String existingKey) {
		Iterator<String> iter = attributesToOverride.keySet().iterator();
		while (iter.hasNext()) {
			String overridingKey = iter.next();
			if (existingKey.startsWith(overridingKey)) {
				return true;
			}
		}
		return false;
	}

	public void shutdownExpose() {
		NGROKClient client = NGROKLaunchTracker.get(getName());

		if (client != null) {
			client.shutdown();
			NGROKLaunchTracker.remove(getName());
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refreshLivePorts() {
		refresh(livePort, actuatorPort);
	}

	private void refresh(LiveExpression<?>... exps) {
		for (LiveExpression<?> e : exps) {
			if (e!=null) {
				e.refresh();
			}
		}
	}

	@Override
	public LocalBootDashModel getBootDashModel() {
		return (LocalBootDashModel) super.getBootDashModel();
	}




}
