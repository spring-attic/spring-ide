/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.core.cli.install.CloudCliInstall;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport;
import org.springframework.ide.eclipse.boot.launch.process.BootProcessFactory;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * Spring Cloud CLI service launch configuration
 *
 * @author Alex Boyko
 *
 */
public class CloudCliServiceLaunchConfigurationDelegate extends BootCliLaunchConfigurationDelegate {

	private static final VersionRange SPRING_CLOUD_CLI_SINGLE_PROCESS_VERSION_RANGE = new VersionRange("1.3.0");

	public final static String TYPE_ID = "org.springframework.ide.eclipse.boot.launch.cloud.cli.service";

	public final static String ATTR_CLOUD_SERVICE_ID = "local-cloud-service-id";

	private List<String> getCloudCliServiceLifeCycleVmArguments(ILaunchConfiguration configuration, int jmxPort) {
		List<String> vmArgs = new ArrayList<>();
			EnumSet<JmxBeanSupport.Feature> enabled = BootLaunchConfigurationDelegate
					.getEnabledJmxFeatures(configuration);
			if (!enabled.isEmpty()) {
				String enableLiveBeanArgs = JmxBeanSupport.jmxBeanVmArgs(jmxPort, enabled);
				vmArgs.addAll(Arrays.asList(enableLiveBeanArgs.split("\n")));
			}
		return vmArgs;
	}

	protected String[] getProgramArgs(IBootInstall bootInstall, ILaunch launch, ILaunchConfiguration configuration) {
		try {
			CloudCliInstall cloudCliInstall = bootInstall.getExtension(CloudCliInstall.class);
			if (cloudCliInstall == null) {
				Log.error("No Spring Cloud CLI installation found");
			} else {
				String serviceId = configuration.getAttribute(ATTR_CLOUD_SERVICE_ID, (String) null);
				Version cloudCliVersion = cloudCliInstall.getVersion();
				List<String> vmArgs = new ArrayList<>();
				List<String> args = new ArrayList<>();

				args.add(CloudCliInstall.COMMAND_PREFIX);
				args.add(serviceId);

				if (cloudCliVersion != null && SPRING_CLOUD_CLI_SINGLE_PROCESS_VERSION_RANGE.includes(cloudCliVersion)) {
					args.add("--deployer=thin");
				}

				args.add("--");
				args.add("--logging.level.org.springframework.cloud.launcher.deployer=DEBUG");

				// VM argument for the service log output
				if (BootLaunchConfigurationDelegate.supportsAnsiConsoleOutput()) {
					vmArgs.add("-Dspring.output.ansi.enabled=always");
				}

				if (canUseLifeCycle(cloudCliVersion)) {
					int jmxPort = getJmxPort(configuration);
					// Set the JMX port for launch
					launch.setAttribute(BootLaunchConfigurationDelegate.JMX_PORT, String.valueOf(jmxPort));
					vmArgs.addAll(getCloudCliServiceLifeCycleVmArguments(configuration, jmxPort));
					// Set the JMX port connection jvm args for the service
					if (!vmArgs.isEmpty()) {
						args.add("--spring.cloud.launcher.deployables." + serviceId + ".properties.JAVA_OPTS=" + String.join(",", vmArgs));
					}
				} else {
					if (!vmArgs.isEmpty()) {
						args.add("--spring.cloud.launcher.deployables." + serviceId + ".properties.spring.cloud.deployer.local.javaOpts=" + String.join(",", vmArgs));
					}
				}
				return args.toArray(new String[args.size()]);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return new String[0];
	}

	private int getJmxPort(ILaunchConfiguration configuration) {
		int port = 0;
		try {
			port = Integer.parseInt(BootLaunchConfigurationDelegate.getJMXPort(configuration));
		} catch (Exception e) {
			// ignore: bad data in launch config.
		}
		if (port == 0) {
			try {
				// slightly better than calling JmxBeanSupport.randomPort()
				port = PortFinder.findFreePort();
			} catch (IOException e) {
				Log.log(e);
			}
		}
		return port;
	}

	public static boolean isLocalCloudServiceLaunch(ILaunchConfiguration conf) {
		try {
			if (conf!=null) {
					String type = conf.getType().getIdentifier();
				return TYPE_ID.equals(type);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfig(String serviceId) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(TYPE_ID);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, serviceId);

		// Set default config with life cycle tracking support because it should cover with life cycle tracking and without
		BootLaunchConfigurationDelegate.setDefaults(config, null, null);

		config.setAttribute(ATTR_CLOUD_SERVICE_ID, serviceId);

		// Overwrite process factory class because for latest version of Cloud CLI life cycle tracking through JMX port is not available for services
		BootLaunchConfigurationDelegate.setProcessFactory(config, CloudCliProcessFactory.class);
		return config;
	}

	public static boolean canUseLifeCycle(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		return conf!=null && canUseLifeCycle(conf);
	}

	public static boolean canUseLifeCycle(ILaunchConfiguration conf) {
		try {
			if (!TYPE_ID.equals(conf.getType().getIdentifier())) {
				return false;
			}
			IBootInstall bootInstall = BootInstallManager.getInstance().getDefaultInstall();
			if (bootInstall == null) {
				return false;
			}
			Version cloudCliVersion = bootInstall.getExtension(CloudCliInstall.class) == null ? null : bootInstall.getExtension(CloudCliInstall.class).getVersion();
			// Cloud CLI version below 1.2.0 and over 1.3.0 can't have JMX connection to cloud service hence life cycle should be disabled.
			if (!canUseLifeCycle(cloudCliVersion)) {
				return false;
			}
			return BootLaunchConfigurationDelegate.getEnableLifeCycle(conf);
		} catch (Exception e) {
			// Ignore
		}
		return false;
	}

	private static boolean canUseLifeCycle(Version cloudCliVersion) {
		// Cloud CLI version below 1.2.0 and over 1.3.0 can't have JMX connection to cloud service hence life cycle should be disabled.
		if (cloudCliVersion == null
				|| !CloudCliInstall.CLOUD_CLI_JAVA_OPTS_SUPPORTING_VERSIONS.includes(cloudCliVersion)
				|| SPRING_CLOUD_CLI_SINGLE_PROCESS_VERSION_RANGE.includes(cloudCliVersion)) {
			return false;
		}
		return true;
	}

	public static class CloudCliProcessFactory extends BootProcessFactory {

		@Override
		public IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes) {
			try {
				IBootInstall bootInstall = BootInstallManager.getInstance().getDefaultInstall();
				if (bootInstall != null) {
					Version cloudCliVersion = bootInstall.getExtension(CloudCliInstall.class) == null ? null : bootInstall.getExtension(CloudCliInstall.class).getVersion();
					if (canUseLifeCycle(cloudCliVersion)) {
						return super.newProcess(launch, process, label, attributes);
					}
				}
			} catch (Exception e) {
				Log.log(e);
			}
			return new RuntimeProcess(launch, process, label, attributes);
		}

	}

}
