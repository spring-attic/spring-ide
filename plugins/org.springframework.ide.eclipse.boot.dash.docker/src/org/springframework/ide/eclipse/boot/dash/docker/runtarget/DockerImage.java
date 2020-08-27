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
import static org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType.PLUGIN_ID;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.DockerClient.RemoveContainerParam;
import org.mandas.docker.client.exceptions.ImageNotFoundException;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.Image;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateIconProvider;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

public class DockerImage implements App, ChildBearing, Styleable, ProjectRelatable, 
	RunStateIconProvider, Deletable
{
	
	private final DockerApp app;
	private final Image image;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();

	private static Map<RunState, ImageDescriptor> RUNSTATE_ICONS = null;

	public DockerImage(DockerApp app, Image image) {
		this.app = app;
		this.image = image;
	}

	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
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
					builder.add(new DockerContainer(getTarget(), app, container));
				}
			}
		}
		return builder.build();
	}

	/**
	 * @param dockerImage
	 *            the {@link IDockerImage} to process
	 * @return the {@link StyledString} to be displayed.
	 */
//	public static StyledString getStyledText(final IDockerImage dockerImage) {
//		final StyledString result = new StyledString(dockerImage.repo());
//		if (!dockerImage.tags().isEmpty()) {
//			final List<String> tags = new ArrayList<>(dockerImage.tags());
//			Collections.sort(tags);
//			result.append(":");
//			result.append(tags.stream().collect(Collectors.joining(", ")), //$NON-NLS-1$
//					StyledString.COUNTER_STYLER);
//		}
//		// TODO: remove the cast to 'DockerImage' once the 'shortId()'
//		// method is in the public API
//		result.append(" (", StyledString.QUALIFIER_STYLER) //$NON-NLS-1$
//				.append(((DockerImage) dockerImage).shortId(),
//						StyledString.QUALIFIER_STYLER)
//				.append(')', StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
//		return result;
//	}

	@Override
	public StyledString getStyledName(Stylers stylers) {
		List<String> repoTags = image.repoTags();
		String repo = extractRepo(repoTags);
		List<String> tags = extractTags(repoTags);
		final StyledString result = new StyledString(repo);
		if (!tags.isEmpty()) {
			result.append(":");
			result.append(tags.stream().collect(Collectors.joining(", ")),
					StyledString.COUNTER_STYLER);
		}
		result
			.append(" (", StyledString.QUALIFIER_STYLER)
			.append(getShortHash(), StyledString.QUALIFIER_STYLER)
			.append(')', StyledString.QUALIFIER_STYLER); 
		return result;
	}
	
	private List<String> extractTags(List<String> repoTags) {
		if (repoTags!=null && !repoTags.isEmpty()) {
			ArrayList<String> tags = new ArrayList<>();
			for (String repoTag : repoTags) {
				int colon = repoTag.indexOf(':');
				if (colon>=0) {
					String tag = repoTag.substring(colon+1);
					tags.add(tag);
				}
			}
			Collections.sort(tags);
			return tags;
		}
		return ImmutableList.of();
	}

	private String extractRepo(List<String> repoTags) {
		if (repoTags!=null && !repoTags.isEmpty()) {
			String repoTag = repoTags.get(0);
			int colon = repoTag.indexOf(':');
			if (colon>=0) {
				return repoTag.substring(0, colon);
			}
		}
		return null;
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

	@Override
	public ImageDescriptor getRunStateIcon(RunState runState) {
		try {
			if (RUNSTATE_ICONS==null) {
				RUNSTATE_ICONS = ImmutableMap.of(
						RunState.RUNNING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/image_started.png"),
						RunState.INACTIVE, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/image_stopped.png"),
						RunState.DEBUGGING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/image_debugging.png"),
						RunState.PAUSED, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/image_paused.png")
				);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		if (RUNSTATE_ICONS!=null) {
			return RUNSTATE_ICONS.get(runState);
		}
		return null;
	}
	@Override
	public void delete() throws Exception {
		DockerClient client = getTarget().getClient();
		if (client != null) {
			RefreshStateTracker rt = this.refreshTracker.get();
			rt.run("Deleting " + getShortHash(), () -> {
				//Delete containers (if there are running containers, 'force' option on removeImage
				// will not work.
				for (Container container : client.listContainers(
						ListContainersParam.allContainers(), 
						ListContainersParam.filter("ancestor", image.id())
				)) {
					client.removeContainer(container.id(), RemoveContainerParam.forceKill());
				}
				
				
				client.removeImage(getName(), /*force*/true, /*noPrune*/false);

				RetryUtil.until(100, DockerContainer.WAIT_BEFORE_KILLING.toMillis(),
					exception -> exception instanceof ImageNotFoundException, 
					() -> {
						try {
							client.inspectImage(image.id());
						} catch (Exception e) {
							return e;
						}
						return null;
					}
				);
			});
		}
	}
}
