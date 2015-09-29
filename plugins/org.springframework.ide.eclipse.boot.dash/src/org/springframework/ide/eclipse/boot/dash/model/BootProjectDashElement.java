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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKTunnel;
import org.springframework.ide.eclipse.boot.dash.util.LaunchUtil;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifeCycleClientManager;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifecycleClient;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.async.ResolvableFuture;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

/**
 * Concrete BootDashElement that wraps an IProject
 *
 * @author Kris De Volder
 */
public class BootProjectDashElement extends WrappingBootDashElement<IProject> implements ElementStateListener, Disposable {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private LocalBootDashModel context;
	private PropertyStoreApi persistentProperties;
	private LiveExpression<RunState> runState;
	private LiveExpression<Integer> livePort;
	private LiveExpression<Integer> actuatorPort;
	private BootDashElementFactory factory;

	public BootProjectDashElement(IProject project, LocalBootDashModel context, IScopedPropertyStore<IProject> projectProperties, BootDashElementFactory factory) {
		super(context, project);
		this.context = context;
		this.persistentProperties = PropertyStoreFactory.createApi(
				PropertyStoreFactory.createForScope(project, projectProperties));
		this.runState = createRunStateExp();
		this.livePort = createLivePortExp(runState, "local.server.port");
		livePort.addListener(new ValueListener<Integer>() {
			public void gotValue(LiveExpression<Integer> exp, Integer value) {
				BootProjectDashElement.this.context.notifyElementChanged(BootProjectDashElement.this);
			}
		});
		this.actuatorPort = createLivePortExp(runState, "local.management.port");
		this.factory = factory;
	}

	public IProject getProject() {
		return delegate;
	}

	@Override
	public RunState getRunState() {
		ProjectRunStateTracker tracker = runStateTracker();
		// Tracker, may be null sometimes during initialization (when restoring persisted state,
		//  elements are created before model is fully intialized)
		if (tracker!=null) {
			return tracker.getState(getProject());
		}
		return RunState.UNKNOWN;
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
	 * Shouldn't really be public, it is only public to make easier to test this class.
	 */
	protected IType[] guessMainTypes() throws CoreException {
		return MainTypeFinder.guessMainTypes(getJavaProject(), new NullProgressMonitor());
	}

	@Override
	public void stopAsync(UserInteractions ui) {
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
		final ResolvableFuture<Void> done = sync?new ResolvableFuture<Void>():null;
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
				BootLaunchUtils.terminate(launches);
				shutdownExpose();
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
		ILaunchConfiguration preferredConf = getPreferredConfig();
		if (preferredConf!=null && configs.contains(preferredConf)) {
			return preferredConf;
		}
		ILaunchConfiguration conf = chooseConfigurationDialog(configs,
				"Choose Launch Configuration",
				"Several launch configurations are associated with '"+getName()+"' "+
				"Choose one.", ui);
		if (conf!=null) {
			setPreferredConfig(conf);
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
	public ILaunchConfiguration getPreferredConfig() {
		return context.getPreferredConfigs(this);
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {
		context.setPreferredConfig(this, config);
	}

	@Override
	public String getLiveHost() {
		return "localhost";
	}

	@Override
	public int getLivePort() {
		return livePort.getValue();
	}

	public int getActuatorPort() {
		return actuatorPort.getValue();
	}

	private LiveExpression<RunState> createRunStateExp() {
		context.addElementStateListener(this);
		LiveExpression<RunState> exp = new LiveExpression<RunState>() {
			protected RunState compute() {
				return getRunState();
			}
		};
		return exp;
	}

	private LiveExpression<Integer> createLivePortExp(final LiveExpression<RunState> runState, final String propName) {
		return new AsyncLiveExpression<Integer>(-1) {
			{
				//Doesn't really depend on runState, but should be recomputed when runState changes.
				dependsOn(runState);
			}
			@Override
			protected Integer compute() {
				return getLivePort(propName);
			}
		};
	}

	private int getLivePort(String propName) {
		ILaunchConfiguration conf = getActiveConfig();
		if (conf!=null) {
			if (BootLaunchConfigurationDelegate.canUseLifeCycle(conf)) {
				int jmxPort = BootLaunchConfigurationDelegate.getJMXPortAsInt(conf);
				if (jmxPort>0) {
					SpringApplicationLifeCycleClientManager cm = null;
					try {
						cm = new SpringApplicationLifeCycleClientManager(jmxPort);

						SpringApplicationLifecycleClient c = cm.getLifeCycleClient();
						if (c!=null) {
							return c.getProperty(propName, -1);
						}
					} catch (Exception e) {
						//most likely this just means the app isn't running so ignore
					} finally {
						if (cm!=null) {
							cm.disposeClient();
						}
					}
				}
			}
		}
		return -1;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		try {
			URI target = getActuatorUrl();
			if (target!=null) {
				ActuatorClient client = new ActuatorClient(target, getTypeLookup());
				return client.getRequestMappings();
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	protected URI getActuatorUrl() {
		try {
			int actuatorPort = getActuatorPort();
			if (actuatorPort>0) {
					return new URI("http://localhost:"+actuatorPort);
			}
		} catch (URISyntaxException e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	public ILaunchConfiguration getActiveConfig() {
		List<ILaunchConfiguration> allConfigs = getTarget().getLaunchConfigs(this);
		if (allConfigs.size()==1) {
			return allConfigs.get(0);
		} else if (allConfigs.size()>1) {
			ILaunchConfiguration preferred = getPreferredConfig();
			if (preferred!=null && allConfigs.contains(preferred)) {
				return preferred;
			}
		}
		return null;
	}

	@Override
	public int getActualInstances() {
		RunState s = getRunState();
		if (s==RunState.RUNNING || s==RunState.DEBUGGING) {
			return 1;
		}
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		return 1;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
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

		Map<String, String> extraAttributes = new HashMap<String, String>();
		extraAttributes.put("spring.boot.prop.server.port", "1" + Integer.toString(port));
		extraAttributes.put("spring.boot.prop.eureka.instance.hostname", "1" + tunnelURL);
		extraAttributes.put("spring.boot.prop.eureka.instance.nonSecurePort", "1" + "80");
		extraAttributes.put("spring.boot.prop.eureka.client.service-url.defaultZone", "1" + eurekaInstance);

		start(launchMode, ui, extraAttributes);
	}

	private void start(final String runMode, UserInteractions ui, Map<String, String> extraAttributes) {
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
				ILaunchConfigurationWorkingCopy workingCopy = conf.getWorkingCopy();

				removeOverriddenAttributes(workingCopy, extraAttributes);
				addAdditionalAttributes(workingCopy, extraAttributes);

				launch(runMode, workingCopy);
			}
		} catch (Exception e) {
			BootActivator.log(e);
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
	public void stateChanged(BootDashElement e) {
		if (this.equals(e)) {
			if (runState!=null) {
				runState.refresh();
			}
		}
	}

	@Override
	public void dispose() {
		this.context.removeElementStateListener(this);
		factory.disposed(this);
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

}
