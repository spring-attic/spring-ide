package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.messages.Container;

public class DockerApp extends AbstractDisposable implements App, ChildBearing, Deletable  {

	private DockerClient client;
	private final IProject project;
	private DockerRunTarget target;
	
	public static final String APP_NAME = "sts.app.name";
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();

	public DockerApp(DockerRunTarget target, DockerClient client, IProject project) {
		this.client = client;
		this.project = project;
		this.target = target;
		AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
		console.write("Deploying Docker app " + getName() + "...", LogType.STDOUT);
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()))) {
			builder.add(new DockerContainer(target, container));
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

	public CompletableFuture<Void> startAsync() {
		Function<RefreshStateTracker, CompletableFuture<Void>> fun = (refreshTracker) -> {
			return refreshTracker.runAsync("Deploying " + getName() + "...", () -> {
				AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);

				for (int i = 0; i < 10; i++) {
					console.write("Deploying Docker app " + getName() + " " + i + "...", LogType.STDOUT);
					Thread.sleep(1000);
				}

				console.write("DONE Deploying Docker app " + getName(), LogType.STDOUT);

			});
		};
		
		return refreshTracker.thenComposeAsync(fun);
	}

	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
	}
}
