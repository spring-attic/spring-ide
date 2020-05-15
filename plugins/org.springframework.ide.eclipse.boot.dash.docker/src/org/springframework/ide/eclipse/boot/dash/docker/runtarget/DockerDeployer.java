package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.livexp.ElementwiseListener;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.messages.Container;

public class DockerDeployer extends AbstractDisposable {

	private final DockerDeployments deployments;
	private final DockerClient client;
	private Map<String, App> apps = new HashMap<>();
	private final DockerRunTarget target;
	
	public DockerDeployer(DockerRunTarget target, DockerDeployments deployments, DockerClient client) {
		this.target = target;
		this.deployments = deployments;
		this.client = client;
		this.deployments.getDeployments().onChange(this, new ElementwiseListener<DockerDeployment>() {

			@Override
			protected void added(LiveExpression<ImmutableSet<DockerDeployment>> exp, DockerDeployment d) {	
				createDeployment(d);
			}


			@Override
			protected void removed(LiveExpression<ImmutableSet<DockerDeployment>> exp, DockerDeployment d) {
				destroyDeployment(d);
			}
		});
	}
	

	synchronized private CompletableFuture<Void> createDeployment(DockerDeployment d) {
		DockerApp app = new DockerApp(target, client, d);
		apps.put(d.getName(), app);
		return app.synchronizeWithDeployment();
	}

	synchronized private void destroyDeployment(DockerDeployment d) {
		App app = apps.get(d.getName());
		if (app != null) {
			if (client != null) {
				try {
					for (Container container : client.listContainers(ListContainersParam.allContainers(),
							ListContainersParam.withLabel(DockerApp.APP_NAME, d.getName()))) {
						client.removeContainer(container.id(), RemoveContainerParam.forceKill());
					}
					apps.remove(d.getName());
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
