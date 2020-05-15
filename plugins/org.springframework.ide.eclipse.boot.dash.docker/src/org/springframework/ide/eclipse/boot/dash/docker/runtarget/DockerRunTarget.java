package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.ProjectDeploymentTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.OnDispose;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.OldValueDisposer;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

public class DockerRunTarget extends AbstractRunTarget<DockerTargetParams> 
implements RemoteRunTarget<DockerClient, DockerTargetParams>, ProjectDeploymentTarget {

	LiveVariable<DockerClient> client = new LiveVariable<>();
	private DockerTargetParams params;
	
	final DockerDeployments deployments;
	private final LiveExpression<DockerDeployer> deployer;
	
	public DockerRunTarget(DockerRunTargetType type, DockerTargetParams params, DockerClient client) {
		super(type, params.getUri());
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
	public void dispose() {
		client.dispose();
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
			c.close();
		}
	}

	@Override
	public synchronized void connect(ConnectMode mode) throws Exception {
		if (!isConnected()) {
			this.client.setValue(DefaultDockerClient.builder().uri(params.getUri()).build());
		}
	}

	@Override
	public void performDeployment(Set<IProject> projects, RunState runOrDebug) throws Exception {
		for (IProject p : projects) {
			DockerDeployment d = new DockerDeployment();
			d.setName(p.getName());
			d.setRunState(RunState.RUNNING);
			deployments.createOrUpdate(d);
		}
	}
}
