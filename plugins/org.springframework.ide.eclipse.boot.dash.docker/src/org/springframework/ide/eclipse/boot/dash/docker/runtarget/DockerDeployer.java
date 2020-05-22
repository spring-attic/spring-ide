/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
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
		this.deployments.addListener(this, new DockerDeployments.Listener() {
			@Override
			public void added(DockerDeployment d) {
				createOrUpdateApp(d);
			}
			
			@Override
			public void updated(DockerDeployment d) {
				createOrUpdateApp(d);
			}
			
			@Override
			public void removed(DockerDeployment d) {
				destroyDeployment(d);
			}
		}); 
	}

	synchronized private CompletableFuture<Void> createOrUpdateApp(DockerDeployment d) {
		DockerApp app = (DockerApp) apps.computeIfAbsent(d.getName(), name -> new DockerApp(name, target, client));
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
