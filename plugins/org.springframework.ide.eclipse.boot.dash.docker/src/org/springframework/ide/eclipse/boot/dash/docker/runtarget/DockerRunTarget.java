package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.internal.DPIUtil;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.ProjectDeploymentTarget;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;

public class DockerRunTarget extends AbstractRunTarget<DockerTargetParams> 
implements RemoteRunTarget<DockerClient, DockerTargetParams>, ProjectDeploymentTarget {

	private static final String[] NO_STRINGS = new String[0];
	private static final String DEPLOYMENTS = "deployments";
	
	LiveVariable<DockerClient> client = new LiveVariable<>();
	private DockerTargetParams params;
	
	LiveSetVariable<String> deployments = new LiveSetVariable<>();
	private List<Disposable> disposables = new ArrayList<>();
	
	public DockerRunTarget(DockerRunTargetType type, DockerTargetParams params, DockerClient client) {
		super(type, params.getUri());
		this.params = params;
		this.client.setValue(client);
		try {
			String[] restoredDeployments = getPersistentProperties().get(DEPLOYMENTS, NO_STRINGS);
			if (restoredDeployments!=null) {
				deployments.replaceAll(ImmutableList.copyOf(restoredDeployments));
			}
			disposables.add(deployments.onChange((_e, v) -> {
				try {
					getPersistentProperties().put(DEPLOYMENTS, deployments.getValues().toArray(NO_STRINGS));
				} catch (Exception e) {
					Log.log(e);
				}
			}));
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public GenericRemoteBootDashModel<?, ?> createSectionModel(BootDashViewModel parent) {
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
		for (Disposable d : disposables) {
			d.dispose();
		}
		disposables.clear();
	}

	@Override
	public LiveExpression<DockerClient> getClientExp() {
		return client;
	}

	@Override
	public Collection<App> fetchApps() throws Exception {
		DockerClient client = this.getClient();
		if (client!=null) {
			ImmutableSet<String> projectNames = deployments.getValues();
			Builder<App> builder = ImmutableList.builder();
			for (String name : projectNames) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				builder.add(new DockerApp(deployments, client, project));
			}
			return  builder.build();
		}
		return ImmutableList.of();
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
			deployments.add(p.getName());
		}
	}
}
