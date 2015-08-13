/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
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
import static org.springframework.ide.eclipse.boot.util.StringUtil.hasText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport.Feature;
import org.springframework.ide.eclipse.boot.launch.process.BootProcessFactory;
import org.springframework.ide.eclipse.boot.launch.profiles.ProfileHistory;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;

/**
 * @author Kris De Volder
 */
public class BootLaunchConfigurationDelegate extends AbstractBootLaunchConfigurationDelegate {

	//private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	public static final String LAUNCH_CONFIG_TYPE_ID = "org.springframework.ide.eclipse.boot.launch";

	public static final String ENABLE_LIVE_BEAN_SUPPORT = "spring.boot.livebean.enable";
	public static final boolean DEFAULT_ENABLE_LIVE_BEAN_SUPPORT = true;

	private static final String JMX_PORT = "spring.boot.livebean.port";

	private static final String PROFILE = "spring.boot.profile";
	public static final String DEFAULT_PROFILE = "";

	public static final String ENABLE_LIFE_CYCLE = "spring.boot.lifecycle.enable";
	public static final boolean DEFAULT_ENABLE_LIFE_CYCLE = true;

	private static final String ENABLE_CHEAP_ENTROPY_VM_ARGS = "-Djava.security.egd=file:/dev/./urandom ";

	private ProfileHistory profileHistory = new ProfileHistory();


	@Override
	public void launch(ILaunchConfiguration conf, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		profileHistory.updateHistory(getProject(conf), getProfile(conf));
		super.launch(conf, mode, launch, monitor);
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration conf) throws CoreException {
		List<PropVal> props = getProperties(conf);
		String profile = getProfile(conf);
		boolean debugOutput = getEnableDebugOutput(conf);
		if ((props==null || props.isEmpty()) && !debugOutput && !hasText(profile)) {
			//shortcut for case where no boot-specific customizations are specified.
			return super.getProgramArguments(conf);
		}
		ArrayList<String> args = new ArrayList<String>();
		if (debugOutput) {
			args.add("--debug");
		}
		if (hasText(profile)) {
			args.add(propertyAssignmentArgument("spring.profiles.active", profile));
		}
		addPropertiesArguments(args, props);
		args.addAll(Arrays.asList(DebugPlugin.parseArguments(super.getProgramArguments(conf))));
		return DebugPlugin.renderArguments(args.toArray(new String[args.size()]), null);
	}

	@Override
	public String getVMArguments(ILaunchConfiguration conf)
			throws CoreException {
		try {
			String vmArgs = super.getVMArguments(conf);
			EnumSet<JmxBeanSupport.Feature> enabled = getEnabledJmxFeatures(conf);
			if (!enabled.isEmpty()) {
				int port = 0;
				try {
					port = Integer.parseInt(getJMXPort(conf));
				} catch (Exception e) {
					//ignore: bad data in launch config.
				}
				if (port==0) {
					port = JmxBeanSupport.randomPort();
				}
				String enableLiveBeanArgs = JmxBeanSupport.jmxBeanVmArgs(port, enabled);
				vmArgs = enableLiveBeanArgs + vmArgs;
			}
			return vmArgs;
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return super.getVMArguments(conf);
	}

	public static EnumSet<Feature> getEnabledJmxFeatures(ILaunchConfiguration conf) {
		EnumSet<Feature> enabled = EnumSet.noneOf(Feature.class);
		if (getEnableLiveBeanSupport(conf)) {
			 enabled.add(Feature.LIVE_BEAN_GRAPH);
		}
		if (getEnableLifeCycle(conf)) {
			enabled.add(Feature.LIFE_CYCLE);
		}
		return enabled;
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
			BootActivator.log(e);
		}
		return DEFAULT_ENABLE_LIFE_CYCLE;
	}

	public static void setEnableLifeCycle(ILaunchConfigurationWorkingCopy wc, boolean enable) {
		wc.setAttribute(ENABLE_LIFE_CYCLE, enable);
	}

	public static boolean canUseLifeCycle(ILaunchConfiguration conf) {
		return BootLaunchConfigurationDelegate.getEnableLifeCycle(conf)
				&& BootLaunchConfigurationDelegate.supportsLifeCycleManagement(conf);
	}

	public static boolean supportsLifeCycleManagement(ILaunchConfiguration conf) {
		IProject p = getProject(conf);
		if (p!=null) {
			return BootPropertyTester.supportsLifeCycleManagement(p);
		}
		return false;
	}

	/**
	 * Get all ILaunchConfigurations  for "Run As >> Spring Boot App" that are
	 * associated with a given project.
	 */
	public static List<ILaunchConfiguration> getLaunchConfigs(IProject p) {
		return getLaunchConfigs(p, LAUNCH_CONFIG_TYPE_ID);
	}

	/**
	 * Sets minimal default values to create a runnable launch configuration.
	 */
	public static void setDefaults(ILaunchConfigurationWorkingCopy wc,
			IProject project,
			String mainType
	) {
		setProcessFactory(wc, BootProcessFactory.class);
		setProject(wc, project);
		if (mainType!=null) {
			setMainType(wc, mainType);
		}
		setEnableLiveBeanSupport(wc, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		setEnableLifeCycle(wc, DEFAULT_ENABLE_LIFE_CYCLE);
		setJMXPort(wc, ""+JmxBeanSupport.randomPort());
		if (!OsUtils.isWindows()) {
			setVMArgs(wc, ENABLE_CHEAP_ENTROPY_VM_ARGS);
		}
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
	public static void setProcessFactory(ILaunchConfigurationWorkingCopy wc, Class<BootProcessFactory> klass) {
		wc.setAttribute(ATTR_PROCESS_FACTORY_ID, klass.getName());
	}


	public static boolean getEnableLiveBeanSupport(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_LIVE_BEAN_SUPPORT, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
	}

	public static String getJMXPort(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(JMX_PORT, "");
		} catch (CoreException e) {
			BootActivator.log(e);
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
			BootActivator.log(e);
			return DEFAULT_PROFILE;
		}
	}

	public static void setProfile(ILaunchConfigurationWorkingCopy conf, String profile) {
		conf.setAttribute(PROFILE, profile);
	}

	public static ILaunchConfigurationWorkingCopy createWorkingCopy(String nameHint) throws CoreException {
		String name = getLaunchMan().generateLaunchConfigurationName(nameHint);
		return getConfType().newInstance(null, name);
	}

	public static ILaunchConfigurationType getConfType() {
		return getLaunchMan().getLaunchConfigurationType(LAUNCH_CONFIG_TYPE_ID);
	}

	public static ILaunchConfiguration createConf(IType type) throws CoreException {
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
		return wc.doSave();
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
}
