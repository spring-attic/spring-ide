package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.messages.Container;

public class DockerApp implements App, ChildBearing, Deletable {

	private DockerClient client;
	public static final String APP_NAME = "sts.app.name";
	private final String appName;
	private final ImmutableList<Container> containers;

	public DockerApp(DockerClient client, String appName, Collection<Container> containers) {
		this.client = client;
		this.appName = appName;
		this.containers = ImmutableList.copyOf(containers);
	}

	@Override
	public String getName() {
		return this.appName;
	}

	@Override
	public String getId() {
		return getName();
	}

	@Override
	public RunState fetchRunState() {
		RunState state = RunState.INACTIVE;
		for (Container container : containers) {
			state = state.merge(DockerContainer.getRunState(container));
		}
		return state;
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.of(RunState.RUNNING, RunState.INACTIVE);
	}

	@Override
	public void setGoalState(RunState state) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<App> getChildren() {
		Builder<App> builder = ImmutableList.builder();
		for (Container container : containers) {
			builder.add(new DockerContainer(container));
		}
		return builder.build();
	}

	@Override
	public void delete() throws Exception {
		for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, appName))) {
			client.removeContainer(container.id(), RemoveContainerParam.forceKill());
		}
	}
}
