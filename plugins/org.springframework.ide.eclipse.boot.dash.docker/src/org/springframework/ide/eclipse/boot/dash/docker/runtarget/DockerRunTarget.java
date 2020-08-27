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

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEVTOOLS;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.LIVE_PORT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.NAME;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROGRESS;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.mandas.docker.client.DefaultDockerClient;
import org.mandas.docker.client.DockerClient;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.DebuggableTarget;
import org.springframework.ide.eclipse.boot.dash.api.ProjectDeploymentTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.ui.DefaultDockerUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.OldValueDisposer;

import com.google.common.collect.ImmutableList;

public class DockerRunTarget extends AbstractRunTarget<DockerTargetParams> 
implements RemoteRunTarget<DockerClient, DockerTargetParams>, ProjectDeploymentTarget, DebuggableTarget {

	final LiveVariable<DockerClient> client;
	LiveExpression<String> sessionId;
	
	private DockerTargetParams params;
	
	final DockerDeployments deployments;
	private final LiveExpression<DockerDeployer> deployer;
	
	@SuppressWarnings("resource")
	public DockerRunTarget(DockerRunTargetType type, DockerTargetParams params, DockerClient client) {
		super(type, params.getUri());
		this.client = new OldValueDisposer<DockerClient>(this).getVar();
		this.sessionId = this.client.apply(c -> c!=null ? UUID.randomUUID().toString() : null);
		this.deployments = this.client.addDisposableChild(new DockerDeployments(getPersistentProperties()));
		this.params = params;
		this.client.setValue(client);
		this.deployer = this.client.applyFactory(c -> new DockerDeployer(DockerRunTarget.this, deployments, c));
	}

	public SimpleDIContext injections() {
		return getType().injections();
	}
	
	@Override
	public DockerRunTargetType getType() {
		return (DockerRunTargetType) super.getType();
	}

	@Override
	public RemoteBootDashModel createSectionModel(BootDashViewModel parent) {
		return new GenericRemoteBootDashModel<>(this, parent);
	}
	
	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public DockerTargetParams getParams() {
		return params;
	}

	@Override
	public LiveExpression<DockerClient> getClientExp() {
		return client;
	}

	@Override
	public Collection<App> fetchApps() throws Exception {
		DockerDeployer deployer = this.deployer.getValue();
		return deployer == null ? ImmutableList.of() : deployer.getApps();
	}

	@Override
	public synchronized void disconnect() {
		DockerClient c = client.getValue();
		if (c!=null) {
			client.setValue(null);
		}
	}

	@Override
	public synchronized void connect(ConnectMode mode) throws Exception {
		if (!isConnected()) {
			try {
				DefaultDockerClient c = DefaultDockerClient.builder().uri(params.getUri()).build();
				c.info(); //ensure docker daemon is reachable.
				this.client.setValue(c);
			} catch (Error e) {
				DefaultDockerUserInteractions.openBundleWiringError(e);
			}
		}
	}

	@Override
	public void performDeployment(Set<IProject> projects, RunState runOrDebug) throws Exception {
		for (IProject p : projects) {
			DockerDeployment d = new DockerDeployment();
			d.setName(p.getName());
			d.setRunState(runOrDebug);
			d.setSessionId(sessionId.getValue());
			d.setBuildId(UUID.randomUUID().toString());
			if (BootPropertyTester.hasDevtools(p)) {
				d.setSystemProperty(DevtoolsUtil.REMOTE_SECRET_PROP, DevtoolsUtil.getSecret(p));
			}
			deployments.createOrUpdate(d);
		}
	}
	
	@Override
	public BootDashColumn[] getDefaultColumns() {
		return new BootDashColumn[] {
				RUN_STATE_ICN,
				NAME,
				PROGRESS,
				LIVE_PORT,
				DEVTOOLS,
				INSTANCES,
				DEFAULT_PATH,
				TAGS
		};
	}

	public String getSessionId() {
		return sessionId.getValue();
	}

	@Override
	public boolean isDebuggingSupported() {
		return true;
	}
}
