package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

import com.spotify.docker.client.messages.Container;

public class DockerContainer implements App, RunStateProvider {

	private final Container container;
	private final DockerRunTarget target;

	public DockerContainer(DockerRunTarget target, Container container) {
		this.target = target;
		this.container = container;
	}

	@Override
	public String getName() {
		return container.id();
	}

	@Override
	public RunState fetchRunState() {
		String state = container.state();
		if ("running".equals(state)) {
			return RunState.RUNNING;
		} else if ("exited".equals(state)) {
			return RunState.INACTIVE;
		}
		return RunState.UNKNOWN;
	}

	@Override
	public DockerRunTarget getTarget() {
		return this.target;
	}
}
