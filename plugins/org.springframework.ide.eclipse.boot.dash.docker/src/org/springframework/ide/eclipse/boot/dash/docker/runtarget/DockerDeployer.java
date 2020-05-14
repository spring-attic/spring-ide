package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.livexp.ElementwiseListener;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.messages.Container;

public class DockerDeployer extends AbstractDisposable {

	private final ObservableSet<String> deployments;
	private final LiveExpression<DockerClient> client;
	private Map<String, App> apps = new HashMap<>();
	private final DockerRunTarget target;

	public DockerDeployer(DockerRunTarget target, ObservableSet<String> deployments, LiveExpression<DockerClient> client) {
		this.target = target;
		this.deployments = deployments;
		this.client = client;
		this.deployments.onChange(this, new ElementwiseListener<String>() {

			@Override
			protected void added(LiveExpression<ImmutableSet<String>> exp, String projectName) {	
				createDeployment(projectName);
			}


			@Override
			protected void removed(LiveExpression<ImmutableSet<String>> exp, String projectName) {
				destroyDeployment(projectName);
			}

		});

	}
	

	synchronized private void createDeployment(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		DockerClient clientVal = client.getValue();
		DockerApp app = new DockerApp(target, clientVal, project);
		apps.put(projectName, app);
		
		app.startAsync();
		
	}
	
	

	synchronized private void destroyDeployment(String projectName) {
		App app = apps.get(projectName);
		if (app != null) {
			DockerClient clientVal = client.getValue();
			if (clientVal != null) {
				try {
					for (Container container : clientVal.listContainers(ListContainersParam.allContainers(),
							ListContainersParam.withLabel(DockerApp.APP_NAME, projectName))) {
						clientVal.removeContainer(container.id(), RemoveContainerParam.forceKill());
					}
					apps.remove(projectName);
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}
	}

	synchronized public Collection<App> getApps() {
		return ImmutableList.copyOf(apps.values());
	}

}
