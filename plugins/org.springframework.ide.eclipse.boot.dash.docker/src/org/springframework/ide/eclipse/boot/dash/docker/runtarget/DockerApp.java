/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.LineBasedStreamGobler;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

public class DockerApp extends AbstractDisposable implements App, ChildBearing, Deletable  {

	private DockerClient client;
	private final IProject project;
	private DockerRunTarget target;
	private final String name;
	
	public static final String APP_NAME = "sts.app.name";
	public static final String BUILD_ID = "sts.app.build-id";
	
	private static final int STOP_WAIT_TIME_IN_SECONDS = 20;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();
	private final JmxSupport jmx;

	public DockerApp(String name, DockerRunTarget target, DockerClient client) {
		this.target = target;
		this.name = name;
		this.client = client;
		this.jmx = new JmxSupport();
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(deployment().getName());
		AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
		console.write("Creating app node " + getName(), LogType.STDOUT);
	}

	private DockerDeployment deployment() {
		return target.deployments.get(name);
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.of(RunState.RUNNING, RunState.INACTIVE);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		if (client!=null) {
			for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()))) {
				builder.add(new DockerContainer(target, container));
			}
		}
		return builder.build();
	}

	@Override
	public void delete() throws Exception {
		target.deployments.remove(project.getName());
	}

	@Override
	public DockerRunTarget getTarget() {
		return this.target;
	}
	
	public CompletableFuture<Void> synchronizeWithDeployment() {
		return this.refreshTracker.thenComposeAsync(refreshTracker -> {
			DockerDeployment deployment = deployment();
			return refreshTracker.runAsync("Synchronizing deployment "+deployment.getName(), () -> {
				RunState desiredRunState = deployment.getRunState();
				List<Container> containers = client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()));
				if (desiredRunState==RunState.INACTIVE) {
					stop(containers);
				} else if (desiredRunState==RunState.RUNNING) {
					String desiredBuildId = deployment.getBuildId();
					List<Container> toStop = new ArrayList<>(containers.size());
					boolean runningContainer = false;
					List<Container> toRun = new ArrayList<>(containers.size());
					for (Container c : containers) {
						if (desiredBuildId.equals(c.labels().get(BUILD_ID))) {
							if (new DockerContainer(getTarget(), c).fetchRunState()==RunState.RUNNING) {
								runningContainer = true;
							}
						} else {
							toStop.add(c);
						}
					}
					stop(toStop);
					if (!runningContainer) {
						start(desiredBuildId);
					}
				}
			});
		});
	}

	private void stop(List<Container> containers) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Stopping containers for app "+name, () -> {
			for (Container container : containers) {
				client.stopContainer(container.id(), STOP_WAIT_TIME_IN_SECONDS);
			}
		});
	}

	public void start(String desiredBuildId) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Deploying " + getName() + "...", () -> {
			AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
			if (!project.isAccessible()) {
				throw new IllegalStateException("The project '"+project.getName()+"' is not accessible");
			}
			console.write("Deploying Docker app " + getName() +"...", LogType.STDOUT);
			String image = build(console);
			run(console, image, desiredBuildId);
			console.write("DONE Deploying Docker app " + getName(), LogType.STDOUT);
		});
	}

	private void run(AppConsole console, String image, String desiredBuildId) throws Exception {
		if (client==null) {
			console.write("Cannot start container... Docker client is disconnected!", LogType.STDERROR);
		} else {
			console.write("Running container with '"+image+"'", LogType.STDOUT);
			String jmxUrl = jmx.getJmxUrl();
			if (jmxUrl!=null) {
				console.write("JMX URL = "+jmxUrl, LogType.STDOUT);
			}
			ContainerConfig.Builder cb = ContainerConfig.builder()
					.image(image)
					.labels(ImmutableMap.of(
							APP_NAME, getName(),
							BUILD_ID, desiredBuildId
					));
			if (jmxUrl!=null) {
				String port = ""+jmx.getPort();
				cb.hostConfig(HostConfig.builder()
						.portBindings(ImmutableMap.of(
								port, ImmutableList.of(PortBinding.of("0.0.0.0", port))
						))
						.build()
				);
				cb.exposedPorts(port);
				cb.env("JAVA_OPTS="+jmx.getJavaOpts());
			}
			ContainerCreation c = client.createContainer(cb.build());
			console.write("Container created: "+c.id(), LogType.STDOUT);
			console.write("Starting container: "+c.id(), LogType.STDOUT);
			client.startContainer(c.id());
			LogStream appOutput = client.logs(c.id(), LogsParam.stdout(), LogsParam.follow());
			JobUtil.runQuietlyInJob("Tracking output for docker container "+c.id(), mon -> {
				appOutput.attach(console.getOutputStream(LogType.APP_OUT), console.getOutputStream(LogType.APP_OUT));
			});
		}
	}

	private static final Pattern BUILT_IMAGE_MESSAGE = Pattern.compile("Successfully built image.*\\'(.*)\\'");
//	private static final Pattern BUILT_IMAGE_MESSAGE = Pattern.compile("Successfully built image");
	
	private String build(AppConsole console) throws Exception {
		AtomicReference<String> image = new AtomicReference<>();
		ProcessBuilder builder = new ProcessBuilder("./mvnw", "spring-boot:build-image")
				.directory(new File(project.getLocation().toString()));
		
		Process process = builder.start();
		LineBasedStreamGobler outputGobler = new LineBasedStreamGobler(process.getInputStream(), (line) -> {
			System.out.println(line);
			Matcher matcher = BUILT_IMAGE_MESSAGE.matcher(line);
			if (matcher.find()) {
				image.set(matcher.group(1));
			}
			console.write(line, LogType.APP_OUT);
		});
		new LineBasedStreamGobler(process.getErrorStream(), (line) -> console.write(line, LogType.APP_ERROR));
		int exitCode = process.waitFor();
		if (exitCode!=0) {
			throw new IOException("Command execution failed!");
		}
		outputGobler.join();
		return image.get();
	}

	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
	}

	@Override
	public void restart(RunState runningOrDebugging) {
		DockerDeployment d = deployment();
		d.setBuildId(UUID.randomUUID().toString());
		d.setRunState(runningOrDebugging);
		target.deployments.createOrUpdate(d);
	}

	@Override
	public void setGoalState(RunState newGoalState) {
		DockerDeployment deployment = deployment();
		if (deployment.getRunState()!=newGoalState) {
			target.deployments.createOrUpdate(deployment.withGoalState(newGoalState));
		}
	}

}
