package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.messages.Container;

public class DockerApp implements App, ChildBearing, Deletable {

	private LiveSetVariable<String> deployments;
	private DockerClient client;
	private final IProject project;
	
	public static final String APP_NAME = "sts.app.name";


	public DockerApp(LiveSetVariable<String> deployments, DockerClient client, IProject project) {
		this.deployments = deployments;
		this.client = client;
		this.project = project;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public String getId() {
		return getName();
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
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()))) {
			builder.add(new DockerContainer(container));
		}
		return builder.build();
	}

	@Override
	public void delete() throws Exception {
		for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(APP_NAME, getName()))) {
			client.removeContainer(container.id(), RemoveContainerParam.forceKill());
		}
		deployments.remove(project.getName());
	}
}
