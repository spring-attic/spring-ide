package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;

public class DockerImage implements App, ChildBearing {
	
	private final DockerApp app;
	private final Image image;

	public DockerImage(DockerApp app, Image image) {
		this.app = app;
		this.image = image;
	}

	@Override
	public String getName() {
		return image.id();
	}

	@Override
	public DockerRunTarget getTarget() {
		return this.app.getTarget();
	}
	
	@Override
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		DockerClient client = app.getClient();
		if (client!=null) {
			for (Container container : client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(DockerApp.APP_NAME, app.getName()))) {
				if (container.imageId().equals(image.id())) {
					builder.add(new DockerContainer(getTarget(), container));
				}
			}
		}
		return builder.build();
	}
	
	
	@Override
	public String toString() {
		return "DockerImage("+image.id()+")";
	}
}
