package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRemoteRunTargetType;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.spotify.docker.client.DefaultDockerClient;

public class DockerRunTargetType extends AbstractRemoteRunTargetType<DockerTargetParams> {
	
	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.docker";
	private static final String[] DEFAULT_DOCKER_URLS = {
			
	};

	public DockerRunTargetType(SimpleDIContext injections) {
		super(injections, "Docker");
	}

	@Override
	public void openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		JobUtil.runInJob("Azure Target Creation", mon -> {
			DockerRunTarget target = login(targets);
			if (target!=null) {
				targets.add(target);
			}
		});
	}

	private DockerRunTarget login(LiveSetVariable<RunTarget> targets) {
		String uri = ui().inputDialog("Connect to Docker Daemon", "Enter docker url:", "unix:///var/run/docker.sock");
		if (StringUtils.hasText(uri)) {
			Set<String> existing = new HashSet<>(targets.getValues().size());
			for (RunTarget t : targets.getValues()) {
				if (t instanceof DockerRunTarget) {
					DockerRunTarget dt = (DockerRunTarget) t;
					existing.add(dt.getParams().getUri());
				}
			}
			if (existing.contains(uri)) {
				ui().errorPopup("Duplicate Target", "A target with the same uri ("+uri+") already exists!");
			} else {
				DefaultDockerClient client = DefaultDockerClient.builder().uri(uri).build();
				return new DockerRunTarget(this, new DockerTargetParams(uri), client);
			}
		}
		return null;
	}

	@Override
	public RunTarget<DockerTargetParams> createRunTarget(DockerTargetParams params) {
		return new DockerRunTarget(this, params, null);
	}

	@Override
	public ImageDescriptor getIcon() {
		return imageDescriptorFromPlugin(PLUGIN_ID, "/icons/docker.png");
	}

	@Override
	public ImageDescriptor getDisconnectedIcon() {
		return BootDashActivator.getImageDescriptor("icons/cloud-inactive.png");
	}


	@Override
	public DockerTargetParams parseParams(String uri) {
		return new DockerTargetParams(uri);
	}

	@Override
	public String serialize(DockerTargetParams p) {
		return p==null ? null : p.getUri();
	}

}
