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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerApp;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerContainer;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerImage;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerTargetParams;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteAppElement;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.DeleteElementsAction;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import com.google.common.collect.ImmutableList;
import org.mandas.docker.client.DefaultDockerClient;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.DockerClient.ListImagesParam;
import org.mandas.docker.client.DockerClient.RemoveContainerParam;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.Image;

public class BootDashDockerTests {

	private static final String DEFAULT_DOCKER_URL = "unix:///var/run/docker.sock";

	@Test
	public void testCreateDockerTarget() throws Exception {
		createDockerTarget();
	}

	private GenericRemoteBootDashModel<DockerClient, DockerTargetParams> createDockerTarget() throws Exception {
		DockerRunTargetType target = injections().getBean(DockerRunTargetType.class);
		AddRunTargetAction createTarget = getCreateTargetAction(target);
		assertNotNull(createTarget);
		assertTrue(createTarget.isEnabled());

		when(ui().inputDialog(eq("Connect to Docker Daemon"), eq("Enter docker url:"), anyString())).then(invocation -> {
			Object[] args = invocation.getArguments();
			assertEquals(DEFAULT_DOCKER_URL, args[2]);
			return args[2];
		});
		createTarget.run();
		createTarget.waitFor(/*Duration.ofMillis(2000)*/);

		BootDashModel model;
			//		ACondition.waitFor("Run target model to appear", 2000, () -> {
			assertNotNull(model = harness.getRunTargetModel(target));
//		});
		return (GenericRemoteBootDashModel<DockerClient, DockerTargetParams>) model;
	}

	@Test
	public void dragAndDropAProject() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", 5_000, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		verifyNoMoreInteractions(ui());
	}

	@Test
	public void deleteDeployment() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", 5_000, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		verifyNoMoreInteractions(ui());
		Mockito.reset(ui());

		String imageId = ((DockerImage)img.getAppData()).getName();

		DeleteElementsAction<?> deleteAction = actions().getDeleteAppsAction();
		assertTrue(harness.selection.getElements().isEmpty());
		assertFalse(deleteAction.isEnabled());
		harness.selection.setElements(dep);
		assertTrue(deleteAction.isEnabled());

		//if (ui().confirmOperation("Deleting Elements", modifiable.getDeletionConfirmationMessage(workitem.getValue()))) {

		when(ui().confirmOperation("Deleting Elements", "Delete webby ?")).thenReturn(true);
		deleteAction.run();

		ACondition.waitFor("Everything is deleted", 5_000, () -> {
			assertTrue(model.getElements().getValues().isEmpty()); // deployment node disapear from model
			assertNoImage(imageId);

			assertTrue(
				client().listContainers(
					ListContainersParam.allContainers(), ListContainersParam.withLabel(DockerApp.APP_NAME)
				)
				.isEmpty()
			);

//			client().listImages(ListImagesParam.allImages()).stream().
		});
	}

	private void assertNoImage(String imageId) throws Exception {
		for (Image img : client().listImages(ListImagesParam.allImages())) {
			assertFalse(imageId.equals(img.id()));
		}
	}

	private GenericRemoteAppElement waitForChild(GenericRemoteAppElement dep, Predicate<App> selector) throws Exception {
		ACondition.waitFor("image node to appear", 60_000, () -> {
			getChild(dep, selector);
		});
		return getChild(dep, selector);
	}

	private GenericRemoteAppElement getChild(GenericRemoteAppElement node, Predicate<App> selector) throws Exception {
		List<GenericRemoteAppElement> selected = new ArrayList<>();
		for (BootDashElement _child : node.getChildren().getValues()) {
			if (_child instanceof GenericRemoteAppElement) {
				GenericRemoteAppElement child = (GenericRemoteAppElement) _child;
				App data = child.getAppData();
				if (selector.test(data)) {
					selected.add(child);
				}
			}
		}
		assertEquals(1, selected.size());
		return selected.get(0);
	}

	private GenericRemoteAppElement waitForDeployment(GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model,
			IProject project) throws Exception {
		ACondition.waitFor("project deployment node", 2_000, () -> {
			getDeployment(model, project);
		});
		GenericRemoteAppElement d = getDeployment(model, project);
		return d;
	}

	//////////////////////////////////////////////
	/// harness

	DockerClient _client;

	private DockerClient client() {
		if (_client==null) {
			_client = DefaultDockerClient.builder().uri(DEFAULT_DOCKER_URL).build();
		}
		return _client;
	}

	@After
	public void tearDown() throws Exception {
		try {
			List<Container> cons = client().listContainers(
					ListContainersParam.allContainers(),
					ListContainersParam.withLabel(DockerApp.APP_NAME)
			);
			//Delete all 'our' containers
			for (Container c : cons) {
				String label = c.labels().getOrDefault(DockerApp.APP_NAME, "");
				assertTrue(StringUtil.hasText(label));
				System.out.println("removing container: "+c.id());
				client().removeContainer(c.id(), RemoveContainerParam.forceKill(), RemoveContainerParam.removeVolumes());
			}
			//Delete all dangline images
			for (Image img : client().listImages(ListImagesParam.danglingImages())) {
				System.out.println("removing image: "+img.id());
				client().removeImage(img.id(), /*force*/true, /*noPrune*/ false);
			}
		} finally {
			if (_client!=null) {
				_client.close();
			}
		}
	}

	private GenericRemoteAppElement getDeployment(BootDashModel model, IProject project) {
		for (BootDashElement e : model.getElements().getValues()) {
			if (project.equals(e.getProject())) {
				return (GenericRemoteAppElement) e;
			}
		}
		throw new NoSuchElementException("No element for project "+project.getName());
	}


	private void dragAndDrop(IProject project, GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model) throws Exception {
		assertTrue(model.canBeAdded(ImmutableList.of(project)));
		model.add(ImmutableList.<Object>of(project));
	}

	private TestBootDashModelContext context = new TestBootDashModelContext(
			ResourcesPlugin.getWorkspace(),
			DebugPlugin.getDefault().getLaunchManager()
	);
	{
		context.injections.def(DockerRunTargetType.class, DockerRunTargetType::new);
	}
	BootDashViewModelHarness harness = new BootDashViewModelHarness(context);
	BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	BootDashActions actions;

	private BootDashActions actions() {
		if (actions==null) {
			actions = new BootDashActions(harness.model, harness.selection.forReading(), injections(), null);
		}
		return actions;
	}

	private SimpleDIContext injections() {
		return context.injections;
	}

	private AllUserInteractions ui() {
		return injections().getBean(AllUserInteractions.class);
	}

	private AddRunTargetAction getCreateTargetAction(RunTargetType<?> type) {
		for (AddRunTargetAction a : actions().getAddRunTargetActions()) {
			if (a.runTargetType.equals(type)) {
				return a;
			}
		}
		throw new NoSuchElementException("Add target action not found for "+type);
	}


}
