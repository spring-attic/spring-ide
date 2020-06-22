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

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.resource.ImageDescriptor;
import org.mandas.docker.client.DefaultDockerClient;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.ui.DockerUserInteractions;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog.Model;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRemoteRunTargetType;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

public class DockerRunTargetType extends AbstractRemoteRunTargetType<DockerTargetParams> {
	
	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.docker";

	public DockerRunTargetType(SimpleDIContext injections) {
		super(injections, "Docker");
	}

	@Override
	public CompletableFuture<?> openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		return JobUtil.runInJob("Docker Target Creation", mon -> {
			DockerRunTarget target = login(targets);
			if (target!=null) {
				targets.add(target);
			}
		});
	}

	private DockerRunTarget login(LiveSetVariable<RunTarget> targets) {
		String uri = inputDockerUrl();
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

	private String inputDockerUrl() {
		DockerUserInteractions ui = injections().getBean(DockerUserInteractions.class);
		Model model = new SelectDockerDaemonDialog.Model();
		ui.selectDockerDaemonDialog(model);
		if (model.okPressed.getValue()) {
			return model.daemonUrl.getValue();
		} else {
			return null;
		}
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
