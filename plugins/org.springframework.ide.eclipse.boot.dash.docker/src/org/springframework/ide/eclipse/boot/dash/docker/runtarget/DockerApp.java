package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.Container;

public class DockerApp implements App {

	private static final String APP_NAME = "sts.app.name";
	private final Container container;

	public DockerApp(Container container) {
		this.container = container;
	}

	@Override
	public String getName() {
		 ImmutableMap<String, String> labels = container.labels();
		 String name = labels.get(APP_NAME);
		 if (name == null) {
			 return getId();
		 }
		 return name;
	}

	@Override
	public String getId() {
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
		 else {
			 return RunState.UNKNOWN;
		 }
	}

}
