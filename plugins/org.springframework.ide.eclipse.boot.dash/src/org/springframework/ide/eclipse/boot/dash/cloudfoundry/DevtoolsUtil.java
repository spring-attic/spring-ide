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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.devtools.BootDevtoolsClientLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * @author Kris De Volder
 */
public class DevtoolsUtil {

	private static final String TARGET_ID = "boot.dash.target.id";
	private static final String APP_NAME = "boot.dash.cloudfoundry.app-name";

	private static final QualifiedName REMOTE_CLIENT_SECRET_PROPERTY = new QualifiedName(BootDashActivator.PLUGIN_ID, "spring.devtools.remote.secret");

	private static final String JAVA_OPTS_ENV_VAR = "JAVA_OPTS";
	private static final String REMOTE_SECRET_JVM_ARG = "-Dspring.devtools.remote.secret=";
//	private static final String REMOTE_DEBUG_JVM_ARGS = "-Dspring.devtools.restart.enabled=false -Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n";

	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private static ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(BootDevtoolsClientLaunchConfigurationDelegate.TYPE_ID);
	}


	private static ILaunchConfigurationWorkingCopy createConfiguration(IProject project, String host) throws CoreException {
		ILaunchConfigurationType configType = getConfigurationType();
		String projectName = project.getName();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName("cf-devtools-client["+projectName+"]"));

		BootLaunchConfigurationDelegate.setProject(wc, project);
		BootDevtoolsClientLaunchConfigurationDelegate.setRemoteUrl(wc, remoteUrl(host));

		wc.setMappedResources(new IResource[] {project});
		return wc;
	}

	public static String remoteUrl(String host) {
		return "http://"+host;
	}

	public static ILaunch launchDevtools(IProject project, String host, String debugSecret, CloudDashElement cde, String mode, IProgressMonitor monitor) throws CoreException {
		if (host==null) {
			throw ExceptionUtil.coreException("Can not launch devtools client: Host not specified");
		}
		ILaunchConfiguration conf = getOrCreateLaunchConfig(project, host, debugSecret, cde);
		return conf.launch(mode, monitor == null ? new NullProgressMonitor() : monitor);
	}

	private static ILaunchConfiguration getOrCreateLaunchConfig(IProject project, String host, String debugSecret, CloudDashElement cde) throws CoreException {
		ILaunchConfiguration existing = findConfig(project, host);
		ILaunchConfigurationWorkingCopy wc;
		if (existing!=null) {
			wc = existing.getWorkingCopy();
		} else {
			wc = createConfiguration(project, host);
		}
		BootDevtoolsClientLaunchConfigurationDelegate.setRemoteSecret(wc, debugSecret);
		setElement(wc, cde);
		return wc.doSave();
	}

	private static ILaunchConfiguration findConfig(IProject project, String host) {
		String remoteUrl = remoteUrl(host);
		try {
			for (ILaunchConfiguration c : getLaunchManager().getLaunchConfigurations(getConfigurationType())) {
				if (project.equals(BootLaunchConfigurationDelegate.getProject(c))
					&& remoteUrl.equals(BootDevtoolsClientLaunchConfigurationDelegate.getRemoteUrl(c))) {
					return c;
				}
			}
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

	private static List<ILaunch> findLaunches(IProject project, String host) {
		String remoteUrl = remoteUrl(host);
		List<ILaunch> launches = new ArrayList<ILaunch>();
		for (ILaunch l : getLaunchManager().getLaunches()) {
			try {
				ILaunchConfiguration c = l.getLaunchConfiguration();
				if (c!=null) {
					if (project.equals(BootLaunchConfigurationDelegate.getProject(c))
						&& remoteUrl.equals(BootDevtoolsClientLaunchConfigurationDelegate.getRemoteUrl(c))) {
						launches.add(l);
					}
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}
		return launches;
	}


	public static boolean isDevClientAttached(CloudDashElement cde, String launchMode) {
		IProject project = cde.getProject();
		if (project!=null) { // else not associated with a local project... can't really attach debugger then
			String host = cde.getLiveHost();
			if (host!=null) { // else app not running, can't attach debugger then
				return isLaunchMode(findLaunches(project, host), launchMode);
			}
		}
		return false;
	}

	private static boolean isLaunchMode(List<ILaunch> launches, String launchMode) {
		for (ILaunch l : launches) {
			if (!l.isTerminated()) {
				if (ILaunchManager.DEBUG_MODE.equals(launchMode) && launchMode.equals(l.getLaunchMode())) {
					for (IDebugTarget p : l.getDebugTargets()) {
						if (!p.isDisconnected() && !p.isTerminated()) {
							return true;
						}
					}
				} else if (ILaunchManager.RUN_MODE.equals(launchMode) && launchMode.equals(l.getLaunchMode())) {
					for (IProcess p : l.getProcesses()) {
						if (!p.isTerminated()) {
							return true;
						}
					}
				} else if (launchMode == null) {
					// Launch mode not specified? Launch is not terminated hence just return true
					return true;
				}
			}
		}
		return false;
	}

	public static void launchDevtools(CloudDashElement cde, String debugSecret, String mode, IProgressMonitor monitor) throws CoreException {
		launchDevtools(cde.getProject(), cde.getLiveHost(), debugSecret, cde, mode, monitor);
	}

	public static void setElement(ILaunchConfigurationWorkingCopy l, CloudDashElement cde) {
		//Tag the launch so we can easily determine what CDE it belongs to later.
		l.setAttribute(TARGET_ID, cde.getTarget().getId());
		l.setAttribute(APP_NAME, cde.getName());
	}

	public static boolean isLaunchFor(ILaunch l, CloudDashElement cde) {
		String targetId = getAttribute(l, TARGET_ID);
		String appName = getAttribute(l, APP_NAME);
		if (targetId!=null && appName!=null) {
			return targetId.equals(cde.getTarget().getId())
					&& appName.equals(cde.getName());
		}
		return false;
	}

	/**
	 * Retreive corresponding CDE for a given launch.
	 */
	public static CloudDashElement getElement(ILaunchConfiguration l, BootDashViewModel model) {
		String targetId = getAttribute(l, TARGET_ID);
		String appName = getAttribute(l, APP_NAME);
		if (targetId!=null && appName!=null) {
			BootDashModel section = model.getSectionByTargetId(targetId);
			if (section instanceof CloudFoundryBootDashModel) {
				CloudFoundryBootDashModel cfModel = (CloudFoundryBootDashModel) section;
				return cfModel.getElement(appName);
			}
		}
		return null;
	}

	public static CloudDashElement getElement(ILaunch l, BootDashViewModel viewModel) {
		ILaunchConfiguration conf = l.getLaunchConfiguration();
		if (conf!=null) {
			return getElement(conf, viewModel);
		}
		return null;
	}


	private static String getAttribute(ILaunch l, String name) {
		try {
			ILaunchConfiguration c = l.getLaunchConfiguration();
			if (c!=null) {
				return c.getAttribute(name, (String)null);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private static String getAttribute(ILaunchConfiguration l, String name) {
		try {
			return l.getAttribute(name, (String)null);
		} catch (CoreException e) {
			BootActivator.log(e);
			return null;
		}
	}

	public static ProcessTracker createProcessTracker(final BootDashViewModel viewModel) {
		return new ProcessTracker(new ProcessListenerAdapter() {
			@Override
			public void debugTargetCreated(ProcessTracker tracker, IDebugTarget target) {
				handleStateChange(target.getLaunch());
			}
			@Override
			public void debugTargetTerminated(ProcessTracker tracker, IDebugTarget target) {
				handleStateChange(target.getLaunch());
			}

			@Override
			public void processTerminated(ProcessTracker tracker, IProcess process) {
				handleStateChange(process.getLaunch());
			}
			@Override
			public void processCreated(ProcessTracker tracker, IProcess process) {
				handleStateChange(process.getLaunch());
			}
			private void handleStateChange(ILaunch l) {
				CloudDashElement e = DevtoolsUtil.getElement(l, viewModel);
				if (e!=null) {
					BootDashModel model = e.getParent();
					model.notifyElementChanged(e);
				}
			}
		});
	}

	public static DevtoolsDebugTargetDisconnector createDebugTargetDisconnector(CloudFoundryBootDashModel model) {
		return new DevtoolsDebugTargetDisconnector(model);
	}

	public static void disconnectDevtoolsClientsFor(CloudDashElement e) {
		ILaunchManager lm = getLaunchManager();
		for (ILaunch l : lm.getLaunches()) {
			if (!l.isTerminated() && isLaunchFor(l, e)) {
				if (l.canTerminate()) {
					try {
						l.terminate();
					} catch (DebugException de) {
						BootActivator.log(de);
					}
				}
			}
		}
	}

	public static String getSecret(IProject project) throws CoreException {
		String secret = project.getPersistentProperty(REMOTE_CLIENT_SECRET_PROPERTY);
		if (secret == null) {
			secret = RandomStringUtils.randomAlphabetic(20);
			project.setPersistentProperty(REMOTE_CLIENT_SECRET_PROPERTY, secret);
		}
		return secret;
	}

	public static boolean isEnvVarSetupForRemoteClient(Map<String, String> envVars, String secret, RunState runOrDebug) {
		String javaOpts = envVars.get(JAVA_OPTS_ENV_VAR);
		if (javaOpts.matches("(.*\\s+|^)" + REMOTE_SECRET_JVM_ARG + secret + "(\\s+.*|$)")) {
//			if (runOrDebug == RunState.DEBUGGING) {
//				return javaOpts.matches("(.*\\s+|^)" + REMOTE_DEBUG_JVM_ARGS + "(\\s+.*|$)");
//			} else {
//				return !javaOpts.matches("(.*\\s+|^)" + REMOTE_DEBUG_JVM_ARGS + "(\\s+.*|$)");
//			}
		}
		return false;
	}

	public static void setupEnvVarsForRemoteClient(Map<String, String> envVars, String secret, RunState runOrDebug) {
		String javaOpts = clearJavaOpts(envVars.get(JAVA_OPTS_ENV_VAR));
		StringBuilder sb = javaOpts == null ? new StringBuilder() : new StringBuilder(javaOpts);
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(REMOTE_SECRET_JVM_ARG);
		sb.append(secret);
//		if (runOrDebug == RunState.DEBUGGING) {
//			sb.append(' ');
//			sb.append(REMOTE_DEBUG_JVM_ARGS);
//		}
		envVars.put(JAVA_OPTS_ENV_VAR, sb.toString());
	}

	private static String clearJavaOpts(String opts) {
		if (opts!=null) {
//			opts = opts.replaceAll(REMOTE_DEBUG_JVM_ARGS + "\\s*", "");
			opts = opts.replaceAll(REMOTE_SECRET_JVM_ARG +"\\w+\\s*", "");
		}
		return opts;
	}

}
