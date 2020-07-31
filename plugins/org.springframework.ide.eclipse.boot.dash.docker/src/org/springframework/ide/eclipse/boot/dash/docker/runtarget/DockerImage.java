package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.Image;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class DockerImage implements App, ChildBearing, Styleable, ProjectRelatable {
	
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
			List<Container> containers = JobUtil.interruptAfter(Duration.ofSeconds(15), 
					() -> client.listContainers(ListContainersParam.allContainers(), ListContainersParam.withLabel(DockerApp.APP_NAME, app.getName()))
			);
			for (Container container : containers) {
				if (container.imageId().equals(image.id())) {
					builder.add(new DockerContainer(getTarget(), container));
				}
			}
		}
		return builder.build();
	}

	@Override
	public StyledString getStyledName(Stylers stylers) {
		if (stylers == null) {
			stylers = new Stylers(null);
		}
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

	@Override
	public IProject getProject() {
		return app.getProject();
	}
}
