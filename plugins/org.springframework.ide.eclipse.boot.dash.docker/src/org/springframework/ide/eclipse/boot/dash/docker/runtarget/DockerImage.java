package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.Image;

public class DockerImage implements App, ChildBearing, Styleable {
	
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
	public StyledString getStyledName(Stylers stylers) {
		List<String> repoTags = image.repoTags();
		if (repoTags != null && !repoTags.isEmpty()) {
			StyledString styledString = new StyledString(repoTags.get(0))
					.append(" ")
					.append(getShortHash(), stylers.italicColoured(SWT.COLOR_DARK_GRAY));
			return styledString;
		} else {
			return null;
		}
	}
	
	private String getShortHash() {
		String id = StringUtil.removePrefix(image.id(), "sha256:");
		if (id.length() > 12) {
			id = id.substring(0, 12);
		}
		return id;
	}

	@Override
	public String toString() {
		return "DockerImage("+image.id()+")";
	}

}
