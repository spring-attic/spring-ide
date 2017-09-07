/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import static org.eclipse.debug.core.DebugPlugin.ATTR_PROCESS_FACTORY_ID;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
import static org.springsource.ide.eclipse.commons.core.util.StringUtil.hasText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport.Feature;
import org.springframework.ide.eclipse.boot.launch.process.BootProcessFactory;
import org.springframework.ide.eclipse.boot.launch.profiles.ProfileHistory;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

/**
 * @author Kris De Volder
 */
public class BootLaunchConfigurationDelegate extends AbstractBootLaunchConfigurationDelegate {

	private static DeletedLaunchConfTerminator deletedLaunchConfTerminator = null;

	public synchronized static void ensureDeletedLaunchConfTerminator() {
		if (deletedLaunchConfTerminator==null) {
			deletedLaunchConfTerminator = new DeletedLaunchConfTerminator(DebugPlugin.getDefault().getLaunchManager(), (ILaunch l) -> {
				try {
					return l!=null && Boolean.valueOf(l.getAttribute(BOOT_LAUNCH_MARKER));
				} catch (Exception e) {
					Log.log(e);
					return false;
				}
			});
		}
	}



//	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
//	private static void debug(String string) {
//		if (DEBUG) {
//			System.out.println(string);
//		}
//	}

	public static final String TYPE_ID = "org.springframework.ide.eclipse.boot.launch";

	/**
	 * Launch attribute that helps recognize a launch as a boot launch even after the launch configuration has
	 * been deleted.
	 */
	public static final String BOOT_LAUNCH_MARKER = "isBootLaunch";

	public static final String ENABLE_LIVE_BEAN_SUPPORT = "spring.boot.livebean.enable";
	public static final boolean DEFAULT_ENABLE_LIVE_BEAN_SUPPORT() {
		BootLaunchActivator ins = BootLaunchActivator.getInstance();
		return ins!=null && ins.isLiveBeanSupported();
	}

	public static final String ENABLE_JMX = "spring.boot.jmx.enable";
	public static final boolean DEFAULT_ENABLE_JMX = true;

	public static final String JMX_PORT = "spring.boot.livebean.port";

	public static final int DEFAULT_JMX_PORT = 0; //means pick it dynamically

	public static final String ANSI_CONSOLE_OUTPUT = "spring.boot.ansi.console";

	public static final String FAST_STARTUP = "spring.boot.fast.startup";

	private static final String PROFILE = "spring.boot.profile";
	public static final String DEFAULT_PROFILE = "";

	public static final String ENABLE_LIFE_CYCLE = "spring.boot.lifecycle.enable";
	public static final boolean DEFAULT_ENABLE_LIFE_CYCLE = true;

	public static final String HIDE_FROM_BOOT_DASH = "spring.boot.dash.hidden";
	public static final boolean DEFAULT_HIDE_FROM_BOOT_DASH = false;

	public static final String PROCESS_ID = "spring.boot.process.id";

	private static final String ENABLE_CHEAP_ENTROPY_VM_ARGS = "-Djava.security.egd=file:/dev/./urandom ";
	private static final String TERMINATION_TIMEOUT = "spring.boot.lifecycle.termination.timeout";
	public static final long DEFAULT_TERMINATION_TIMEOUT = 15000; // 15 seconds

	private ProfileHistory profileHistory = new ProfileHistory();

	/**
	 * Use threadlocal to gain access to current launch in some of the methods
	 * (i.e. getVMArguments in particular) of the {@link AbstractBootLaunchConfigurationDelegate}
	 * framework that, unfortunately don't pass it along as parameters. It's either this, or copy
	 * a whole bunch of inherited code just so we can modify it to add an extra argument.
	 */
	private static final ThreadLocal<ILaunch> CURRENT_LAUNCH = new ThreadLocal<>();

	@Override
	public void launch(ILaunchConfiguration conf, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ensureDeletedLaunchConfTerminator();
		launch.setAttribute(BOOT_LAUNCH_MARKER, "true");
		CURRENT_LAUNCH.set(launch);
		try {
			profileHistory.updateHistory(getProject(conf), getProfile(conf));
			super.launch(conf, mode, launch, monitor);
		} finally {
			CURRENT_LAUNCH.remove();
		}
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration conf) throws CoreException {
		List<PropVal> props = getProperties(conf);
		String profile = getProfile(conf);
		boolean debugOutput = getEnableDebugOutput(conf);
		boolean enableAnsiConsole = supportsAnsiConsoleOutput() && getEnableAnsiConsoleOutput(conf);
		if ((props==null || props.isEmpty()) && !debugOutput && !hasText(profile) && !enableAnsiConsole) {
			//shortcut for case where no boot-specific customizations are specified.
			return super.getProgramArguments(conf);
		}
		ArrayList<String> args = new ArrayList<>();
		if (debugOutput) {
			args.add("--debug");
		}
		if (hasText(profile)) {
			args.add(propertyAssignmentArgument("spring.profiles.active", profile));
		}
		if (enableAnsiConsole) {
			args.add(propertyAssignmentArgument("spring.output.ansi.enabled", "always"));
		}
		addPropertiesArguments(args, props);
		args.addAll(Arrays.asList(DebugPlugin.parseArguments(super.getProgramArguments(conf))));
		return DebugPlugin.renderArguments(args.toArray(new String[args.size()]), null);
	}

	@Override
	public String getVMArguments(ILaunchConfiguration conf)
			throws CoreException {
		try {
			StringBuilder vmArgs = new StringBuilder(super.getVMArguments(conf));
			// VM args for JMX connection
			EnumSet<JmxBeanSupport.Feature> enabled = getEnabledJmxFeatures(conf);
			if (!enabled.isEmpty()) {
				int port = 0;
				try {
					port = Integer.parseInt(getJMXPort(conf));
				} catch (Exception e) {
					//ignore: bad data in launch config.
				}
				if (port==0) {
					port = PortFinder.findFreePort(); //slightly better than calling JmxBeanSupport.randomPort()
				}
				String enableLiveBeanArgs = JmxBeanSupport.jmxBeanVmArgs(port, enabled);
				vmArgs.insert(0, enableLiveBeanArgs);
				CURRENT_LAUNCH.get().setAttribute(JMX_PORT, ""+port);
			}
			// Fast startup VM args
			String fastStartupArgs = BootActivator.getDefault().getPreferenceStore().getString(BootPreferences.PREF_BOOT_FAST_STARTUP_JVM_ARGS);
			boolean fastStartup = getFastStartup(conf) && fastStartupArgs != null && !fastStartupArgs.isEmpty();
			if (fastStartup && !fastStartupArgs.trim().isEmpty()) {
				// Add space to separate fast startup args from the preceding arguments
				vmArgs.append('\n');
				vmArgs.append(fastStartupArgs);
			}
			return vmArgs.toString();
		} catch (Exception e) {
			Log.log(e);
		}
		return super.getVMArguments(conf);
	}

	public static EnumSet<Feature> getEnabledJmxFeatures(ILaunchConfiguration conf) {
		EnumSet<Feature> enabled = EnumSet.noneOf(Feature.class);
		if (getEnableJmx(conf)) {
			enabled.add(Feature.JMX);
		}
		if (getEnableLiveBeanSupport(conf)) {
			 enabled.add(Feature.LIVE_BEAN_GRAPH);
		}
		if (getEnableLifeCycle(conf)) {
			enabled.add(Feature.LIFE_CYCLE);
		}
		return enabled;
	}

	public static boolean isHiddenFromBootDash(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(HIDE_FROM_BOOT_DASH, DEFAULT_HIDE_FROM_BOOT_DASH);
		} catch (CoreException e) {
			Log.log(e);
		}
		return DEFAULT_HIDE_FROM_BOOT_DASH;
	}

	public static void setHiddenFromBootDash(ILaunchConfigurationWorkingCopy conf, boolean hide) {
		conf.setAttribute(HIDE_FROM_BOOT_DASH, hide);
	}

	/**
	 * Retrieve the 'Enable Life Cycle Tracking' option from the config. Note that
	 * this doesn't necesarily mean that this feature is effectively enabled as
	 * it is only supported on recent enough versions of Boot.
	 * <p>
	 * See also the 'supportsLifeCycleManagement' method.
	 */
	public static boolean getEnableLifeCycle(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_LIFE_CYCLE, DEFAULT_ENABLE_LIFE_CYCLE);
		} catch (Exception e) {
			Log.log(e);
		}
		return DEFAULT_ENABLE_LIFE_CYCLE;
	}

	public static boolean getFastStartup(ILaunchConfiguration conf) {
		boolean defaultValue = BootActivator.getDefault().getPreferenceStore().getBoolean(BootPreferences.PREF_BOOT_FAST_STARTUP_DEFAULT);
		try {
			return conf.getAttribute(FAST_STARTUP, defaultValue);
		} catch (Exception e) {
			Log.log(e);
		}
		return defaultValue;
	}

	public static void setEnableJMX(ILaunchConfigurationWorkingCopy wc, boolean enable) {
		wc.setAttribute(ENABLE_JMX, enable);
	}

	public static void setEnableLifeCycle(ILaunchConfigurationWorkingCopy wc, boolean enable) {
		wc.setAttribute(ENABLE_LIFE_CYCLE, enable);
	}

	public static void setFastStartup(ILaunchConfigurationWorkingCopy wc, boolean enable) {
		wc.setAttribute(FAST_STARTUP, enable);
	}

	public static boolean canUseLifeCycle(ILaunchConfiguration conf) {
		return BootLaunchConfigurationDelegate.getEnableLifeCycle(conf)
				&& BootLaunchConfigurationDelegate.supportsLifeCycleManagement(conf);
	}

	public static boolean canUseLifeCycle(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		return conf!=null && canUseLifeCycle(conf);
	}

	public static boolean supportsLifeCycleManagement(ILaunchConfiguration conf) {
		IProject p = getProject(conf);
		if (p!=null) {
			return BootPropertyTester.supportsLifeCycleManagement(p);
		}
		return false;
	}

	/**
	 * Sets minimal default values to create a runnable launch configuration.
	 */
	public static void setDefaults(ILaunchConfigurationWorkingCopy wc,
			IProject project,
			String mainType
	) {
		setProcessFactory(wc, BootProcessFactory.class);
		enableClasspathProviders(wc);
		setProject(wc, project);
		if (mainType!=null) {
			setMainType(wc, mainType);
		}
		setEnableJMX(wc, DEFAULT_ENABLE_JMX);
		setEnableLiveBeanSupport(wc, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT());
		setEnableLifeCycle(wc, DEFAULT_ENABLE_LIFE_CYCLE);
		setTerminationTimeout(wc,""+DEFAULT_TERMINATION_TIMEOUT);
		setJMXPort(wc, ""+DEFAULT_JMX_PORT);
		if (!OsUtils.isWindows()) {
			setVMArgs(wc, ENABLE_CHEAP_ENTROPY_VM_ARGS);
		}
	}

	public static void setTerminationTimeout(ILaunchConfigurationWorkingCopy wc, String value) {
		wc.setAttribute(TERMINATION_TIMEOUT, ""+value);
	}

	public static String getTerminationTimeout(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(TERMINATION_TIMEOUT, ""+DEFAULT_TERMINATION_TIMEOUT);
		} catch (Exception e) {
			Log.log(e);
			return ""+DEFAULT_TERMINATION_TIMEOUT;
		}
	}

	public static long getTerminationTimeoutAsLong(ILaunchConfiguration conf) {
		String v = getTerminationTimeout(conf);
		if (StringUtil.hasText(v)) {
			try {
				return Long.parseLong(v);
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return DEFAULT_TERMINATION_TIMEOUT;
	}


	private static void setVMArgs(ILaunchConfigurationWorkingCopy wc, String vmArgs) {
		wc.setAttribute(ATTR_VM_ARGUMENTS, vmArgs);
	}

	/**
	 * Notes:
	 * <p>
	 *  1. we are assuming that the processFactoryId is the same as the classname of
	 *  the class that implements it. This is not a given, but a convenient and logical convention.
	 *  <p>
	 *  2. The class must be registered to this ID using plugin.xml (extension point
	 *  org.eclipse.debug.core.processFactories)
	 */
	public static void setProcessFactory(ILaunchConfigurationWorkingCopy wc, Class<? extends BootProcessFactory> klass) {
		wc.setAttribute(ATTR_PROCESS_FACTORY_ID, klass.getName());
	}

	public static boolean getEnableJmx(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_JMX, DEFAULT_ENABLE_JMX);
		} catch (Exception e) {
			Log.log(e);
		}
		return DEFAULT_ENABLE_JMX;
	}

	public static boolean getEnableLiveBeanSupport(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_LIVE_BEAN_SUPPORT, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT());
		} catch (Exception e) {
			Log.log(e);
		}
		return DEFAULT_ENABLE_LIVE_BEAN_SUPPORT();
	}

	public static String getJMXPort(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(JMX_PORT, "");
		} catch (CoreException e) {
			Log.log(e);
		}
		return "";
	}

	public static void setEnableLiveBeanSupport(ILaunchConfigurationWorkingCopy conf, boolean value) {
		conf.setAttribute(ENABLE_LIVE_BEAN_SUPPORT, value);
	}

	public static void setJMXPort(ILaunchConfigurationWorkingCopy conf, String portAsStr) {
		conf.setAttribute(JMX_PORT, portAsStr);
	}

	public static String getProfile(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(PROFILE, DEFAULT_PROFILE);
		} catch (CoreException e) {
			Log.log(e);
			return DEFAULT_PROFILE;
		}
	}

	public static void setProfile(ILaunchConfigurationWorkingCopy conf, String profile) {
		conf.setAttribute(PROFILE, profile);
	}

	public static ILaunchConfiguration duplicate(ILaunchConfiguration conf) throws CoreException {
		String newName = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(conf.getName());
		ILaunchConfigurationWorkingCopy copy = conf.copy(newName);

		int existingJmxPort = getJMXPortAsInt(conf);
		if (existingJmxPort>0) {
			//change port on duplicated config, but only if it was set to a specific port.
			setJMXPort(copy, ""+JmxBeanSupport.randomPort());
		}
		return copy.doSave();
	}


	public static ILaunchConfigurationWorkingCopy createWorkingCopy(String nameHint) throws CoreException {
		String name = getLaunchMan().generateLaunchConfigurationName(nameHint);
		return getConfType().newInstance(null, name);
	}

	public static ILaunchConfigurationType getConfType() {
		return getLaunchMan().getLaunchConfigurationType(TYPE_ID);
	}

	public static ILaunchConfiguration createConf(IType type) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy(type);
		return wc.doSave();
	}

	public static ILaunchConfigurationWorkingCopy createWorkingCopy(IType type) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = null;
		ILaunchConfigurationType configType = getConfType();
		IProject project = type.getJavaProject().getProject();
		String projectName = type.getJavaProject().getElementName();
		String shortTypeName = type.getTypeQualifiedName('.');
		String typeName = type.getFullyQualifiedName();
		wc = configType.newInstance(null, getLaunchMan().generateLaunchConfigurationName(
				projectName+" - "+shortTypeName));
		BootLaunchConfigurationDelegate.setDefaults(wc, project, typeName);
		wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});
		return wc;
	}

	public static ILaunchConfiguration createConf(IProject project) throws CoreException {
		return createConf(JavaCore.create(project));
	}

	public static ILaunchConfiguration createConf(IJavaProject project) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = null;
		ILaunchConfigurationType configType = getConfType();
		String projectName = project.getElementName();
		wc = configType.newInstance(null, getLaunchMan().generateLaunchConfigurationName(projectName));
		BootLaunchConfigurationDelegate.setDefaults(wc, project.getProject(), null);
		wc.setMappedResources(new IResource[] {project.getUnderlyingResource()});
		return wc.doSave();
	}

	public static int getJMXPortAsInt(ILaunchConfiguration conf) {
		String jmxPortStr = getJMXPort(conf);
		if (jmxPortStr!=null) {
			try {
				return Integer.parseInt(jmxPortStr);
			} catch (Exception e) {
				//Ignore
			}
		}
		return -1;
	}

	public static int getJMXPortAsInt(ILaunch launch) {
		String jmxPortStr = launch.getAttribute(JMX_PORT);
		if (jmxPortStr!=null) {
			try {
				return Integer.parseInt(jmxPortStr);
			} catch (Exception e) {
				//Ignore
			}
		}
		return -1;
	}

	public static long getTerminationTimeoutAsLong(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		if (conf!=null) {
			return BootLaunchConfigurationDelegate.getTerminationTimeoutAsLong(conf);
		}
		return BootLaunchConfigurationDelegate.DEFAULT_TERMINATION_TIMEOUT;
	}

	public static boolean supportsAnsiConsoleOutput() {
		Bundle bundle = Platform.getBundle("net.mihai-nita.ansicon.plugin");
		return bundle != null && bundle.getState() != Bundle.UNINSTALLED;
	}

	public static boolean getEnableAnsiConsoleOutput(ILaunchConfiguration conf) {
		boolean defaultValue = supportsAnsiConsoleOutput();
		try {
			return conf.getAttribute(ANSI_CONSOLE_OUTPUT, defaultValue);
		} catch (CoreException e) {
			return defaultValue;
		}
	}

	public static void setEnableAnsiConsoleOutput(ILaunchConfigurationWorkingCopy wc, boolean enable) {
		wc.setAttribute(ANSI_CONSOLE_OUTPUT, enable);
	}

}
