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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.DockerClient.ListImagesParam;
import org.mandas.docker.client.DockerClient.LogsParam;
import org.mandas.docker.client.LogStream;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ContainerCreation;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.Image;
import org.mandas.docker.client.messages.PortBinding;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.DesiredInstanceCount;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.LineBasedStreamGobler;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class DockerApp extends AbstractDisposable implements App, ChildBearing, Deletable, ProjectRelatable, DesiredInstanceCount {

	private static final String DOCKER_IO_LIBRARY = "docker.io/library/";
	private static final String[] NO_STRINGS = new String[0];
	private DockerClient client;
	private final IProject project;
	private DockerRunTarget target;
	private final String name;
	
	public static final String APP_NAME = "sts.app.name";
	public static final String BUILD_ID = "sts.app.build-id";
	public static final String JMX_PORT = "sts.app.jmx.port";
	public static final String DEBUG_PORT = "sts.app.debug.port";
	public static final String APP_LOCAL_PORT = "sts.app.port.local";
	
	private static final int STOP_WAIT_TIME_IN_SECONDS = 20;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();

	private static final String DEBUG_JVM_ARGS(String debugPort) {
		return "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n,address="+debugPort;
	}

	public DockerApp(String name, DockerRunTarget target, DockerClient client) {
		this.target = target;
		this.name = name;
		this.client = client;
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(deployment().getName());
		AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
		console.write("Creating app node " + getName(), LogType.STDOUT);
	}
	
	public DockerClient getClient() {
		return this.client;
	}

	public DockerDeployment deployment() {
		return target.deployments.get(name);
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.of(RunState.INACTIVE, RunState.RUNNING, RunState.DEBUGGING);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		if (client!=null) {
			List<Image> images = client.listImages(ListImagesParam.allImages());
			
			synchronized (this) {
				Set<String> persistedImages = new HashSet<>(Arrays.asList(getPersistedImages()));
				Set<String> existingImages = new HashSet<>();
				for (Image image : images) {
					if (persistedImages.contains(image.id())) {
						builder.add(new DockerImage(this, image));
						existingImages.add(image.id());
					}
				}
				setPersistedImages(existingImages);
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
		System.out.println(refreshTracker.isDone());
		return this.refreshTracker.thenComposeAsync(refreshTracker -> {
			DockerDeployment deployment = deployment();
			return refreshTracker.runAsync("Synchronizing deployment "+deployment.getName(), () -> {
				String currentSession = this.target.sessionId.getValue();
				if (currentSession.equals(deployment.getSessionId())) {
					RunState desiredRunState = deployment.getRunState();
					List<Container> containers = client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()));
					if (desiredRunState==RunState.INACTIVE) {
						stop(containers);
					} else if (desiredRunState.isActive()) {
						String desiredBuildId = deployment.getBuildId();
						List<Container> toStop = new ArrayList<>(containers.size());
						boolean desiredContainerFound = false;
						for (Container c : containers) {
							if (desiredBuildId.equals(c.labels().get(BUILD_ID))) {
								if (new DockerContainer(getTarget(), c).fetchRunState()==desiredRunState) {
									desiredContainerFound = true;
								}
							} else {
								toStop.add(c);
							}
						}
						stop(toStop);
						if (!desiredContainerFound) {
							start(desiredBuildId, desiredRunState);
						}
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

	public void start(String desiredBuildId, RunState desiredRunState) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Deploying " + getName() + "...", () -> {
			AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
			console.show();
			if (!project.isAccessible()) {
				throw new IllegalStateException("The project '"+project.getName()+"' is not accessible");
			}
			console.write("Deploying Docker app " + getName() +"...", LogType.STDOUT);
			String image = build(console);
			run(console, image, desiredBuildId, desiredRunState);
			console.write("DONE Deploying Docker app " + getName(), LogType.STDOUT);
		});
	}

	private void run(AppConsole console, String image, String desiredBuildId, RunState desiredRunState) throws Exception {
		if (client==null) {
			console.write("Cannot start container... Docker client is disconnected!", LogType.STDERROR);
		} else {
			console.write("Running container with '"+image+"'", LogType.STDOUT);
			JmxSupport jmx = new JmxSupport();
			String jmxUrl = jmx.getJmxUrl();
			if (jmxUrl!=null) {
				console.write("JMX URL = "+jmxUrl, LogType.STDOUT);
			}
			ImmutableMap.Builder<String,String> labels = ImmutableMap.<String,String>builder()
					.put(APP_NAME, getName())
					.put(BUILD_ID, desiredBuildId);
			
			ImmutableSet.Builder<String> exposedPorts = ImmutableSet.builder();
			ImmutableMap.Builder<String, List<PortBinding>> portBindings = ImmutableMap.builder();

			ContainerConfig.Builder cb = ContainerConfig.builder()
					.image(image);
			
			int appLocalPort = PortFinder.findFreePort();
			int appContainerPort = 8080;
			
			if (appLocalPort > 0) {
				labels.put(APP_LOCAL_PORT, ""+appLocalPort);
				portBindings.put("" + appContainerPort, ImmutableList.of(PortBinding.of("0.0.0.0", appLocalPort)));
				exposedPorts.add(""+appContainerPort);
			}

			StringBuilder javaOpts = new StringBuilder();

			if (jmxUrl!=null) {
				String jmxPort = ""+jmx.getPort();
				labels.put(JMX_PORT, jmxPort);
				
				portBindings.put(jmxPort, ImmutableList.of(PortBinding.of("0.0.0.0", jmxPort)));
				exposedPorts.add(jmxPort);
				
				javaOpts.append(jmx.getJavaOpts());
				javaOpts.append(" ");
			}
			
			if (desiredRunState==RunState.DEBUGGING) {
				String debugPort = ""+PortFinder.findFreePort();
				labels.put(DockerApp.DEBUG_PORT, debugPort);
				
				portBindings.put(debugPort, ImmutableList.of(PortBinding.of("0.0.0.0", debugPort)));
				exposedPorts.add(debugPort);
				
				javaOpts.append(DEBUG_JVM_ARGS(debugPort));
				console.write("Debug Port = "+debugPort, LogType.STDOUT);
			}
			
			String javaOptsStr = javaOpts.toString();
			if (StringUtils.hasText(javaOptsStr)) {
				cb.env("JAVA_OPTS="+javaOptsStr.trim());
				console.write("JAVA_OPTS="+javaOptsStr.trim(), LogType.STDOUT);
			}
			
			cb.hostConfig(HostConfig.builder()
					.portBindings(portBindings.build())
					.build()
			);
			cb.exposedPorts(exposedPorts.build());

			cb.labels(labels.build());
			ContainerCreation c = client.createContainer(cb.build());
			console.write("Container created: "+c.id(), LogType.STDOUT);
			console.write("Starting container: "+c.id(), LogType.STDOUT);
			console.write("Ports: "+appLocalPort+"->"+appContainerPort, LogType.STDOUT);
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
		File directory = new File(project.getLocation().toString());
		String[] command;
		if (Files.exists(directory.toPath().resolve("mvnw"))) {
			command = new String[] {"./mvnw", "spring-boot:build-image", "-DskipTests"};
		} else if (Files.exists(directory.toPath().resolve("gradlew"))) {
			command = new String[] {"./gradlew", "bootBuildImage", "-x", "test" };
		} else {
			throw new IllegalStateException("Neither Gradle nor Maven wrapper was found!");
		}
		
		ProcessBuilder builder = new ProcessBuilder(command).directory(directory);
		
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
		
		String imageTag = image.get();
		if (imageTag.startsWith(DOCKER_IO_LIBRARY)) {
			imageTag = imageTag.substring(DOCKER_IO_LIBRARY.length());
		}
		List<Image> images = client.listImages(ListImagesParam.byName(imageTag));
		
		for (Image img : images) {
			addPersistedImage(img.id());
		}
		
		return imageTag;
	}

	synchronized private void addPersistedImage(String imageId) {
		String key = imagesKey();
		try {
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			PropertyStoreApi props = getTarget().getPersistentProperties();
			builder.addAll(Arrays.asList(props.get(key, NO_STRINGS)));
			builder.add(imageId);
			props.put(key, builder.build().toArray(NO_STRINGS));

		} catch (Exception e) {
			Log.log(e);
		}
	}
	
	private void setPersistedImages(Set<String> existingImages) {
		try {
			getTarget().getPersistentProperties().put(imagesKey(), existingImages.toArray(NO_STRINGS));
		} catch (Exception e) {
			Log.log(e);
		}		
	}

	private String[] getPersistedImages() {
		try {
			return getTarget().getPersistentProperties().get(imagesKey(), NO_STRINGS);
		} catch (Exception e) {
			Log.log(e);
		}
		return NO_STRINGS;
	}

	private String imagesKey() {
		return getName() + ".images";
	}

	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
	}

	@Override
	public void restart(RunState runningOrDebugging) {
		DockerDeployment d = deployment();
		d.setBuildId(UUID.randomUUID().toString());
		d.setSessionId(target.sessionId.getValue());
		d.setRunState(runningOrDebugging);
		target.deployments.createOrUpdate(d);
	}

	@Override
	public void setGoalState(RunState newGoalState) {
		DockerDeployment deployment = deployment();
		if (deployment.getRunState()!=newGoalState) {
			target.deployments.createOrUpdate(deployment.withGoalState(newGoalState, target.sessionId.getValue()));
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public int getDesiredInstances() {
		DockerDeployment deployment = deployment();
		if (deployment != null) {
			return deployment.getDesiredInstances();
		}
		return 0;
	}
}
