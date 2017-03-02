/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.cloud.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.cli.BootCliCommand;
import org.springframework.ide.eclipse.boot.core.cli.BootCliUtils;
import org.springframework.ide.eclipse.boot.core.cli.CloudCliUtils;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Launch configuration implementation for Spring Cloud CLI local cloud service launch
 *
 * @author Alex Boyko
 *
 */
public class LocalCloudServiceLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	private static final VersionRange SPRING_CLOUD_CLI_JAVA_OPTS_FORMAT_CHANGE_VERSION_RANGE = new VersionRange("1.3.0");

	public final static String TYPE_ID = "org.springframework.ide.eclipse.boot.launch.cloud.cli.service";

	public final static ILaunchConfigurationType TYPE = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(TYPE_ID);

	public final static String ATTR_CLOUD_SERVICE_ID = "local-cloud-service-id";

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

	public static boolean canUseLifeCycle(ILaunchConfiguration conf) {
		try {
			if (!TYPE_ID.equals(conf.getType().getIdentifier())) {
				return false;
			}
			IBootInstall bootInstall = BootCliUtils.getSpringBootInstall();
			if (!SPRING_CLOUD_CLI_JAVA_OPTS_FORMAT_CHANGE_VERSION_RANGE.includes(Version.valueOf(BootCliUtils.getSpringBootInstall().getVersion()))) {
				return false;
			}
			Version cloudCliVersion = CloudCliUtils.getVersion(bootInstall);
			if (cloudCliVersion == null || !CloudCliUtils.CLOUD_CLI_JAVA_OPTS_SUPPORTING_VERSIONS.includes(cloudCliVersion)) {
				return false;
			}
			return BootLaunchConfigurationDelegate.getEnableLifeCycle(conf);
		} catch (Exception e) {
			// Ignore
		}
		return false;
	}

	public static boolean canUseLifeCycle(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		return conf!=null && canUseLifeCycle(conf);
	}

	private Process createProcess(ILaunchConfiguration configuration, int jmxPort) throws Exception {
		String serviceId = configuration.getAttribute(ATTR_CLOUD_SERVICE_ID, (String) null);
		if (serviceId == null) {
			throw new IllegalArgumentException("Local Cloud Service ID is missing from launch configuration!");
		}
		IBootInstall bootInstall = BootCliUtils.getSpringBootInstall();
		BootCliCommand cmd = new BootCliCommand(bootInstall.getHome());
		List<String> vmArgs = getVmArguments(configuration, jmxPort);
		List<String> args = new ArrayList<>(3 + vmArgs.size());
		args.add("cloud");
		args.add(serviceId);
		if (!vmArgs.isEmpty()) {
			args.add("--");
			args.add("--logging.level.org.springframework.cloud.launcher.deployer=DEBUG");
			Version cloudCliVersion = CloudCliUtils.getVersion(bootInstall);
			if (cloudCliVersion != null && SPRING_CLOUD_CLI_JAVA_OPTS_FORMAT_CHANGE_VERSION_RANGE.includes(cloudCliVersion)) {
				args.add("--spring.cloud.launcher.deployables." + serviceId + ".properties.spring.cloud.deployer.local.javaOpts=" + String.join(",", vmArgs));
			} else {
				args.add("--spring.cloud.launcher.deployables." + serviceId + ".properties.JAVA_OPTS=" + String.join(",", vmArgs));
			}
		}
		return Runtime.getRuntime().exec(cmd.getProcessArguments(args.toArray(new String[args.size()])), null, cmd.getProcessWorkingFolder());
	}

	private List<String> getVmArguments(ILaunchConfiguration configuration, int jmxPort) {
		List<String> vmArgs = new ArrayList<>();
			EnumSet<JmxBeanSupport.Feature> enabled = BootLaunchConfigurationDelegate
					.getEnabledJmxFeatures(configuration);
			if (!enabled.isEmpty()) {
				String enableLiveBeanArgs = JmxBeanSupport.jmxBeanVmArgs(jmxPort, enabled);
				vmArgs.addAll(Arrays.asList(enableLiveBeanArgs.split("\n")));
			}
			if (BootLaunchConfigurationDelegate.supportsAnsiConsoleOutput()) {
				vmArgs.add("-Dspring.output.ansi.enabled=always");
			}
		return vmArgs;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask("Launching Local Cloud Service", 1);
		try {
			String serviceId = configuration.getAttribute(ATTR_CLOUD_SERVICE_ID, (String) null);
			int jmxPort = getJmxPort(configuration);
			Process bootProcess = createProcess(configuration, jmxPort);
			// Set the JMX port for launch
			launch.setAttribute(BootLaunchConfigurationDelegate.JMX_PORT, String.valueOf(jmxPort));
			launch.addProcess(new CloudCliProcess(launch, bootProcess, serviceId, Collections.emptyMap()));
		} catch (Exception e) {
			throw ExceptionUtil.coreException(e);
		} finally {
			monitor.done();
		}
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

}
