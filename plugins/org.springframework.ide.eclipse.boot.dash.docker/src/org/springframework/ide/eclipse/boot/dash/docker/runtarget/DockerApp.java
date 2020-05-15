package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.internal.DPIUtil;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
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

public class DockerApp extends AbstractDisposable implements App, ChildBearing, Deletable  {

	private DockerClient client;
	private final IProject project;
	private DockerRunTarget target;
	
	public static final String APP_NAME = "sts.app.name";
	private static final int STOP_WAIT_TIME_IN_SECONDS = 20;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();
	private DockerDeployment deployment;

	public DockerApp(DockerRunTarget target, DockerClient client, DockerDeployment deployment) {
		this.client = client;
		this.deployment = deployment;
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(deployment.getName());
		this.target = target;
		AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
		console.write("Creating app node" + getName(), LogType.STDOUT);
	}

	@Override
	public String getName() {
		return deployment.getName();
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
			return refreshTracker.runAsync("Synchronizing deployment "+deployment.getName(), () -> {

				RunState desiredRunState = deployment.getRunState();
				List<Container> containers = client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()));
				RunState actualRunState = RunState.INACTIVE;
				for (Container _c : containers) {
					DockerContainer c = new DockerContainer(getTarget(), _c);
					actualRunState = actualRunState.merge(c.fetchRunState());
				}
				if (actualRunState==desiredRunState) {
					return;
				}
				if (desiredRunState==RunState.RUNNING) {
					start();
				} else if (desiredRunState==RunState.INACTIVE) {
					stop(containers);
				}
			});
		});
	}

	private void stop(List<Container> containers) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Stopping containers for deployment "+deployment.getName(), () -> {
			for (Container container : containers) {
				client.stopContainer(container.id(), STOP_WAIT_TIME_IN_SECONDS);
			}
		});
	}

	public void start() throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Deploying " + getName() + "...", () -> {
			AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
			if (!project.isAccessible()) {
				throw new IllegalStateException("The project '"+project.getName()+"' is not accessible");
			}
			console.write("Deploying Docker app " + getName() +"...", LogType.STDOUT);
			String image = build(console);
			run(console, image);
			console.write("DONE Deploying Docker app " + getName(), LogType.STDOUT);
		});
	}

	private void run(AppConsole console, String image) throws Exception {
		if (client==null) {
			console.write("Cannot start container... Docker client is disconnected!", LogType.STDERROR);
		}
		console.write("Running container with '"+image+"'", LogType.STDOUT);
		ContainerCreation c = client.createContainer(ContainerConfig.builder()
				.image(image)
				.labels(ImmutableMap.of(APP_NAME, getName()))
				.build()
		);
		console.write("Container created: "+c.id(), LogType.STDOUT);
		console.write("Starting container: "+c.id(), LogType.STDOUT);
		client.startContainer(c.id());
		LogStream appOutput = client.logs(c.id(), LogsParam.stdout(), LogsParam.follow());
		JobUtil.runQuietlyInJob("Tracking output for docker container "+c.id(), mon -> {
			appOutput.attach(console.getOutputStream(LogType.APP_OUT), console.getOutputStream(LogType.APP_OUT));
		});
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

}
