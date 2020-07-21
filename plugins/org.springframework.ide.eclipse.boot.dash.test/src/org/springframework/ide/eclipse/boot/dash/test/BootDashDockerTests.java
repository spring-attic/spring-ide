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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withImportStrategy;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertContains;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.IAction;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mandas.docker.client.DefaultDockerClient;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.ListContainersParam;
import org.mandas.docker.client.DockerClient.ListImagesParam;
import org.mandas.docker.client.DockerClient.RemoveContainerParam;
import org.mandas.docker.client.exceptions.DockerException;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.ContainerInfo;
import org.mandas.docker.client.messages.Image;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeployToRemoteTargetAction;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerApp;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerContainer;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerImage;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTarget;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerTargetParams;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog.Model;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Failable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteAppElement;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.dash.model.remote.RemoteJavaLaunchUtil;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget.ConnectMode;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.util.CollectionUtils;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.DeleteElementsAction;
import org.springframework.ide.eclipse.boot.dash.views.EnableRemoteDevtoolsAction;
import org.springframework.ide.eclipse.boot.dash.views.RestartDevtoolsClientAction;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springframework.ide.eclipse.boot.launch.devtools.BootDevtoolsClientLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class BootDashDockerTests {

	private static final int BUILD_IMAGE_TIMEOUT = 30_000;
	private static final String DEFAULT_DOCKER_URL = "unix:///var/run/docker.sock";

	@Rule
	public LaunchCleanups launches = new LaunchCleanups();

	@Test
	public void testCreateDockerTarget() throws Exception {
		createDockerTarget();
	}

	@Test
	public void devtoolsFullScenario() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"),
				withStarters("devtools")
		);

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		EnableRemoteDevtoolsAction enableDevtools = actions().getEnableDevtoolsAction();

		harness.selection.setElements(con);
		assertFalse(enableDevtools.isVisible());
		harness.selection.setElements(img);
		assertFalse(enableDevtools.isVisible());

		harness.selection.setElements(dep);
		assertTrue(enableDevtools.isVisible());
		assertTrue(enableDevtools.isEnabled());
		assertEquals("Enable Remote DevTools Server", enableDevtools.getText());

		assertNull(getDevtoolsSecret(dep));

		enableDevtools.run();
		enableDevtools.lastOperation.get();

		String deploymentSecret = getDevtoolsSecret(dep);
		assertNotNull(deploymentSecret);

		ACondition.waitFor("old container stopped", 5_000, () -> {
			assertEquals(RunState.INACTIVE, con.getRunState());
		});

		GenericRemoteAppElement img2 = waitForChild(dep, d -> d instanceof DockerImage && !d.getName().equals(img.getName()));
		GenericRemoteAppElement con2 = waitForChild(img2, d -> d instanceof DockerContainer);
		String containerSecret = getDevtoolsSecret(con2);
		assertEquals(deploymentSecret, containerSecret);

		ACondition.waitFor("second container running", 5_000, () -> {
			assertEquals(RunState.RUNNING, con2.getRunState());
		});

		RestartDevtoolsClientAction restartClient = actions().getRestartDevtoolsClientAction();

		harness.selection.setElements(dep);
		assertFalse(restartClient.isVisible());
		harness.selection.setElements(img2);
		assertFalse(restartClient.isVisible());
		harness.selection.setElements(con);
		assertTrue(restartClient.isVisible());
		assertFalse(restartClient.isEnabled());

		harness.selection.setElements(con2);
		assertTrue(restartClient.isVisible());
		assertTrue(restartClient.isEnabled());

		restartClient.run();
		ACondition.waitFor("active devtools client", 10_000, () -> {
			assertActiveDevtoolsClientLaunch(con2);
		});
		ILaunch launch = assertActiveDevtoolsClientLaunch(con2);
		try {
			ILaunchConfiguration conf = launch.getLaunchConfiguration();
			assertEquals(containerSecret, BootDevtoolsClientLaunchConfigurationDelegate.getRemoteSecret(conf));

			createFile(project, "src/main/java/com/example/demo/HelloController.java", helloController("Good"));
			ACondition.waitFor("Good controller", 15_000, () -> {
				String url = con2.getUrl();
				assertEquals("Good", IOUtils.toString(new URI(url), "UTF8"));
			});

			createFile(project, "src/main/java/com/example/demo/HelloController.java", helloController("Better"));
			ACondition.waitFor("Better controller", 15_000, () -> {
				String url = con2.getUrl();
				assertEquals("Better", IOUtils.toString(new URI(url), "UTF8"));
			});

			con2.stopAsync();

			ACondition.waitFor("container node deleted", 10_000, () -> {
				assertEquals(RunState.INACTIVE, dep.getRunState());
				assertEquals(RunState.INACTIVE, img2.getRunState());
				BootDashElement child = CollectionUtils.getSingle(img2.getChildren().getValues());
				assertEquals(RunState.INACTIVE, child.getRunState());
	// TODO:
				assertNoActiveDevtoolsClientLaunch(con2);
	//			assertNoLaunchConfigs(BootDevtoolsClientLaunchConfigurationDelegate.TYPE_ID);
			});
		} finally {
			launch.terminate();
		}
	}

	private static String helloController(String message) {
		return "package com.example.demo;\n" +
				"\n" +
				"import org.springframework.web.bind.annotation.GetMapping;\n" +
				"import org.springframework.web.bind.annotation.RestController;\n" +
				"\n" +
				"@RestController\n" +
				"public class HelloController {\n" +
				"\n" +
				"	@GetMapping(\"/\")\n" +
				"	public String hello() {\n" +
				"		return \""+message+"\";\n" +
				"	}\n" +
				"}";
	}

	private String getDevtoolsSecret(GenericRemoteAppElement el) {
		App data = el.getAppData();
		System.out.println("getDevtoolsSecret data ="+data);
		if (data instanceof DockerApp) {
			@SuppressWarnings("resource")
			DockerApp app = (DockerApp)data;
			return app.deployment().getSystemProperties().getOrDefault(DevtoolsUtil.REMOTE_SECRET_PROP, null);
		} else if (data instanceof DockerContainer) {
			DockerContainer con = (DockerContainer)data;
			return con.getSystemProps().getOrDefault(DevtoolsUtil.REMOTE_SECRET_PROP, null);
		} else {
			return null;
		}
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

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		verifyNoMoreInteractions(ui());
	}

	@Test
	public void deployAndDebugOnTarget() throws Exception {
		RemoteBootDashModel model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		BootProjectDashElement localElement = harness.waitForElement(2_000, project);
		DeployToRemoteTargetAction<?,?> a = debugOnDockerAction();
		harness.selection.setElements(localElement);
		assertTrue(a.isEnabled());
		a.run();

		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all debugging", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.DEBUGGING, dep.getRunState());
			assertEquals(RunState.DEBUGGING, img.getRunState());
			assertEquals(RunState.DEBUGGING, con.getRunState());
		});
		ACondition.waitFor("remote debug launch", 5_000, () -> assertActiveDebugLaunch(con));


		ILaunch launch = assertActiveDebugLaunch(con);
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		assertEquals(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION, conf.getType().getIdentifier());

		assertEquals(con.getStyledName(null).toString(), conf.getName());

		assertTrue(launch.canTerminate());
		launch.terminate();
		ACondition.waitFor("launch termination", 5_000, () -> {
			assertTrue(launch.isTerminated());
		});

		assertEquals(ImmutableSet.of(conf), con.getLaunchConfigs());

		ACondition.waitFor("all stopped", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.INACTIVE, dep.getRunState());
			assertEquals(RunState.INACTIVE, img.getRunState());
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
	}

	@Test
	public void deleteRunningContainer() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		verifyNoMoreInteractions(ui());
		Mockito.reset(ui());

		String containerId = con.getName();
		assertEquals(1, listContainersWithId(containerId).size());

//		when(ui().confirmOperation(eq("Deleting Elements"), any())).thenAnswer(answer);
		DeleteElementsAction<?> delete = actions().getDeleteAppsAction();
		harness.selection.setElements(con);
		assertTrue(delete.isEnabled());
		delete.run();

		ACondition.waitFor("container deleted", 5_000, () -> {
			assertTrue(img.getChildren().getValue().isEmpty());
			assertEquals(0, listContainersWithId(containerId).size());
		});
		verifyNoMoreInteractions(ui());
	}

	private List<Container> listContainersWithId(String containerId)
			throws DockerException, InterruptedException {
		return
				client().listContainers(
						ListContainersParam.allContainers(),
						ListContainersParam.withLabel(DockerApp.APP_NAME)
				)
				.stream()
				.filter(c -> c.id().equals(containerId))
				.collect(Collectors.toList());
	}

	@Test
	public void stopAppCurrentSession() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});
		verifyNoMoreInteractions(ui());
		reset(ui());

		RunStateAction stop = stopAction();
		harness.selection.setElements(dep);
		assertTrue(stop.isEnabled());
		stop.run();

		ACondition.waitFor("all started", 5_000, () -> {
			assertEquals(RunState.INACTIVE, dep.getRunState());
			assertEquals(RunState.INACTIVE, img.getRunState());
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
	}

	@Test
	public void stopAppPreviousSession() throws Exception {
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));
		String firstSession;

		{
			GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
			Mockito.reset(ui());
			dragAndDrop(project, model);
			GenericRemoteAppElement dep = waitForDeployment(model, project);
			GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
			GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

			ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
				assertEquals(RunState.RUNNING, dep.getRunState());
				assertEquals(RunState.RUNNING, img.getRunState());
				assertEquals(RunState.RUNNING, con.getRunState());
			});
			verifyNoMoreInteractions(ui());
			reset(ui());

			firstSession = getSessionId(model);
		}
		harness.reload();
		{
			RemoteBootDashModel model = (RemoteBootDashModel) harness.getRunTargetModel(DockerRunTargetType.class);

			assertFalse(firstSession.equals(getSessionId(model)));

			GenericRemoteAppElement dep = waitForDeployment(model, project);
			GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
			GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
			ACondition.waitFor("all started", 5_000, () -> {
				assertEquals(RunState.RUNNING, dep.getRunState());
				assertEquals(RunState.RUNNING, img.getRunState());
				assertEquals(RunState.RUNNING, con.getRunState());
			});
			RunStateAction stop = stopAction();
			harness.selection.setElements(dep);
			assertTrue(stop.isEnabled());
			stop.run();

			ACondition.waitFor("all stopped", 5_000, () -> {
				assertEquals(RunState.INACTIVE, dep.getRunState());
				assertEquals(RunState.INACTIVE, img.getRunState());
				assertEquals(RunState.INACTIVE, con.getRunState());
			});
		}
	}

	@Test
	public void liveBeans() throws Exception {
		IProject project = projects.createBootProject("webby-actuator",
				bootVersionAtLeast("2.3.0"),
				withStarters("web", "actuator")
		);
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
		GenericRemoteAppElement[] nodes = {
				con, img, dep
		};

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});
		verifyNoMoreInteractions(ui());

		String jmxUrl = con.getJmxUrl();
		ACondition.waitFor("live beans model", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertEquals(ImmutableSet.of(jmxUrl), node.getActuatorUrls().getValue());
				LiveBeansModel beans = node.getLiveBeans().orElse(null);
				assertNotNull(beans);
				assertFalse(beans.getBeans().isEmpty());
			}
		});

		RunStateAction stop = stopAction();
		harness.selection.setElements(dep);
		stop.run();

		ACondition.waitFor("Container stopped", 15_000, () -> { //Sometimes stopping container takes a long time. Not sure why.
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
		ACondition.waitFor("live beans gone", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertTrue(node.getActuatorUrls().getValue().isEmpty());
				assertNull(node.getLiveBeans().orElse(null));
			}
		});
	}

	@Test
	public void liveRequestMappings() throws Exception {
		IProject project = projects.createBootProject("webby-actuator",
				bootVersionAtLeast("2.3.0"),
				withStarters("web", "actuator")
		);
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
		GenericRemoteAppElement[] nodes = {
				con, img, dep
		};

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});
		verifyNoMoreInteractions(ui());

		String jmxUrl = con.getJmxUrl();
		ACondition.waitFor("live requestmappings", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertEquals(ImmutableSet.of(jmxUrl), node.getActuatorUrls().getValue());
				List<RequestMapping> rm = node.getLiveRequestMappings().orElse(null);
				assertNotNull(rm);
				assertFalse(rm.isEmpty());
			}
		});

		RunStateAction stop = stopAction();
		harness.selection.setElements(dep);
		stop.run();

		ACondition.waitFor("Container stopped", 15_000, () -> { //Sometimes stopping container takes a long time. Not sure why.
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
		ACondition.waitFor("live requestmappings gone", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertTrue(node.getActuatorUrls().getValue().isEmpty());
				assertNull(node.getLiveBeans().orElse(null));
			}
		});
	}

	@Test
	public void liveDataNotAvailable() throws Exception {
		IProject project = projects.createBootProject("webby-actuator",
				bootVersionAtLeast("2.3.0"),
				withStarters("web")
		);
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
		GenericRemoteAppElement[] nodes = {
				con, img, dep
		};

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});
		verifyNoMoreInteractions(ui());

		ACondition.waitFor("live requestmappings", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				Failable<ImmutableList<RequestMapping>> rm = node.getLiveRequestMappings();
				Failable<LiveBeansModel> beans = node.getLiveBeans();
				Failable<LiveEnvModel> env = node.getLiveEnv();

				assertTrue(rm.hasFailed());
				assertContains("Enable actuator endpoint <b>mappings</b>", rm.getErrorMessage().toHtml());

				assertTrue(beans.hasFailed());
				assertContains("Enable actuator endpoint <b>beans</b>", beans.getErrorMessage().toHtml());

				assertTrue(env.hasFailed());
				assertContains("Enable actuator endpoint <b>env</b>", env.getErrorMessage().toHtml());
			}
		});

		RunStateAction stop = stopAction();
		harness.selection.setElements(dep);
		stop.run();

		ACondition.waitFor("Container stopped", 15_000, () -> { //Sometimes stopping container takes a long time. Not sure why.
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
		ACondition.waitFor("live requestmappings gone", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertTrue(node.getActuatorUrls().getValue().isEmpty());
				assertNull(node.getLiveBeans().orElse(null));
			}
		});
	}

	@Test
	public void liveEnv() throws Exception {
		IProject project = projects.createBootProject("webby-actuator",
				bootVersionAtLeast("2.3.0"),
				withStarters("web", "actuator")
		);
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
		GenericRemoteAppElement[] nodes = {
				con, img, dep
		};

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});
		verifyNoMoreInteractions(ui());

		String jmxUrl = con.getJmxUrl();
		ACondition.waitFor("live env", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertEquals(ImmutableSet.of(jmxUrl), node.getActuatorUrls().getValue());
				LiveEnvModel env = node.getLiveEnv().orElse(null);
				assertNotNull(env);
				assertFalse(env.getPropertySources().getPropertySources().isEmpty());
			}
		});

		RunStateAction stop = stopAction();
		harness.selection.setElements(dep);
		stop.run();

		ACondition.waitFor("Container stopped", 15_000, () -> { //Sometimes stopping container takes a long time. Not sure why.
			assertEquals(RunState.INACTIVE, con.getRunState());
		});
		ACondition.waitFor("live env gone", 5_000, () -> {
			for (GenericRemoteAppElement node : nodes) {
				assertTrue(node.getActuatorUrls().getValue().isEmpty());
				assertNull(node.getLiveEnv().orElse(null));
			}
		});
	}

	@Test
	public void dragAndDropGradleProject() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"), withImportStrategy("GRADLE"));

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, dep.getRunState());
			assertEquals(RunState.RUNNING, img.getRunState());
			assertEquals(RunState.RUNNING, con.getRunState());
		});

		verifyNoMoreInteractions(ui());
	}

	@Test
	public void tagsPersistence() throws Exception {
	IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

	{
			GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
			Mockito.reset(ui());

			dragAndDrop(project, model);
			GenericRemoteAppElement dep = waitForDeployment(model, project);
			GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
			GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

			assertTrue("Tags empty initally", dep.getTags().isEmpty());
			assertTrue("Tags empty initally", img.getTags().isEmpty());
			assertTrue("Tags empty initally", con.getTags().isEmpty());

			setTags(dep, "tag1", "tag2");
			assertTags(dep, "tag1", "tag2");
			harness.assertLabelContains("[tag1, tag2]", dep);
		}

		harness.reload();

		{
			RemoteBootDashModel model = (RemoteBootDashModel) harness
					.getRunTargetModel(DockerRunTargetType.class);
			GenericRemoteAppElement dep = waitForDeployment(model, project);
			GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
			GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);
			assertTags(dep, "tag1", "tag2");
			assertTrue("Should be still empty", img.getTags().isEmpty());
			assertTrue("Should be still empty", con.getTags().isEmpty());
			harness.assertLabelContains("[tag1, tag2]", dep);
		}

	}

	@Test
	public void urlComputation() throws Exception {
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		img.setDefaultRequestMappingPath("/hello");

		ACondition.waitFor("Wait for port to be defined", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(1, con.getLivePorts().size());
			assertEquals(1, img.getLivePorts().size());
			assertEquals(1, dep.getLivePorts().size());
		});

		int port = con.getLivePort();
		assertEquals(ImmutableSet.of(port), img.getLivePorts());
		assertEquals(ImmutableSet.of(port), dep.getLivePorts());
		assertEquals(ImmutableSet.of(port), con.getLivePorts());

		assertEquals("http" + "://localhost:" + port + "/", dep.getUrl());
		assertEquals("http" + "://localhost:" + port + "/hello", img.getUrl());

		harness.assertLabelContains("/hello", img);

	}

	@Test
	public void instanceCount() throws Exception {
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());

		dragAndDrop(project, model);
		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		ACondition.waitFor("Previous container stopped and deployment started", BUILD_IMAGE_TIMEOUT, () -> {
			assertTrue(dep.getRunState() == RunState.RUNNING);
			assertEquals(1, dep.getDesiredInstances());
			assertEquals(-1, img.getDesiredInstances());
			assertEquals(-1, con.getDesiredInstances());
			assertEquals(1, dep.getActualInstances());
			assertEquals(1, img.getActualInstances());
			assertEquals(1, con.getActualInstances());
		});

		harness.selection.setElements(dep);
		restartAction().run();

		ACondition.waitFor("Second container to appear", BUILD_IMAGE_TIMEOUT, () -> {
			assertEquals(2, img.getChildren().getValues().size());
		});

		String id = con.getName();

		GenericRemoteAppElement con2 = getChild(img, d -> d instanceof DockerContainer && !id.equals(d.getName()));

		ACondition.waitFor("Previous container stopped and deployment started", BUILD_IMAGE_TIMEOUT, () -> {
			assertTrue(con.getRunState() == RunState.INACTIVE);
			assertTrue(con2.getRunState() == RunState.RUNNING);
			// No diffs in project hence same image but another container started
			assertEquals(1, dep.getChildren().getValues().size());

			assertEquals(1, dep.getDesiredInstances());
			assertEquals(-1, img.getDesiredInstances());
			assertEquals(-1, con.getDesiredInstances());
			assertEquals(-1, con2.getDesiredInstances());
			assertEquals(1, dep.getActualInstances());
			assertEquals(1, img.getActualInstances());
			assertEquals(0, con.getActualInstances());
			assertEquals(1, con2.getActualInstances());

		});

		harness.assertInstancesLabel("1/1", "", dep);
		harness.assertInstancesLabel("1", "", img);
		harness.assertInstancesLabel("0", "", con);
		harness.assertInstancesLabel("1", "", con2);

		harness.selection.setElements(con);
		restartAction().run();

		ACondition.waitFor("Both containers running", BUILD_IMAGE_TIMEOUT, () -> {
			assertTrue(con.getRunState() == RunState.RUNNING);
			assertTrue(con2.getRunState() == RunState.RUNNING);
			// No diffs in project hence same image but another container started
			assertEquals(1, dep.getChildren().getValues().size());

			assertEquals(1, dep.getDesiredInstances());
			assertEquals(-1, img.getDesiredInstances());
			assertEquals(-1, con.getDesiredInstances());
			assertEquals(-1, con2.getDesiredInstances());
			assertEquals(2, dep.getActualInstances());
			assertEquals(2, img.getActualInstances());
			assertEquals(1, con.getActualInstances());
			assertEquals(1, con2.getActualInstances());

		});

		harness.assertInstancesLabel("2/1", "2/1", dep);
		harness.assertInstancesLabel("2", "2", img);
		harness.assertInstancesLabel("1", "", con);
		harness.assertInstancesLabel("1", "", con2);
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

		ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
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

	@Test
	public void noAutoStartForMismatchingSession() throws Exception {
		GenericRemoteBootDashModel<DockerClient, DockerTargetParams> model = createDockerTarget();
		Mockito.reset(ui());
		IProject project = projects.createBootWebProject("webby", bootVersionAtLeast("2.3.0"));

		dragAndDrop(project, model);
		{
			GenericRemoteAppElement dep = waitForDeployment(model, project);
			GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
			GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

			ACondition.waitFor("all started", BUILD_IMAGE_TIMEOUT, () -> {
				assertEquals(RunState.RUNNING, dep.getRunState());
				assertEquals(RunState.RUNNING, img.getRunState());
				assertEquals(RunState.RUNNING, con.getRunState());
			});

			verifyNoMoreInteractions(ui());
			Mockito.reset(ui());

			String sessionId = getSessionId(model);
			assertNotNull(sessionId);
			assertEquals(sessionId, getSessionId(dep));

			String containerId = con.getName();
			client().stopContainer(containerId, 2);
			ACondition.waitFor("Container stopped", 5_000, () -> {
				ContainerInfo info = client().inspectContainer(containerId);
				String status = info.state().status();
				System.out.println("status = "+status);
				assertEquals("exited", status);
			});
		}

		model.disconnect();

		ACondition.waitFor("Docker app dispaeared", 2_000, () -> {
			assertTrue(model.getElements().getValues().isEmpty());
		});

		CompletableFuture<Void> deploymentSynchronized = RefreshStateTracker.waitForOperation("Synchronizing deployment "+project.getName());
		model.connect(ConnectMode.INTERACTIVE);

		GenericRemoteAppElement dep = waitForDeployment(model, project);
		GenericRemoteAppElement img = waitForChild(dep, d -> d instanceof DockerImage);
		GenericRemoteAppElement con = waitForChild(img, d -> d instanceof DockerContainer);

		deploymentSynchronized.get(1, TimeUnit.SECONDS);

		assertEquals(RunState.INACTIVE, dep.getRunState());
		assertEquals(RunState.INACTIVE, img.getRunState());
		assertEquals(RunState.INACTIVE, con.getRunState());
	}

	@Test
	public void canceledDockerTargetCreation() throws Exception {
		DockerRunTargetType target = injections().getBean(DockerRunTargetType.class);
		AddRunTargetAction createTarget = getCreateTargetAction(target);
		assertNotNull(createTarget);
		assertTrue(createTarget.isEnabled());

		doAnswer(invocation -> {
			Model model = (Model) invocation.getArguments()[0];
			//model.performOk(); //no clicking ok, so that's like when dialog is 'canceled'.
			return null;
		}).when(ui()).selectDockerDaemonDialog(Matchers.any());

		createTarget.run();
		createTarget.waitFor(Duration.ofMillis(2000));

		assertTrue(harness.getRunTargetModels(target).isEmpty());
	}

	//////////////////////////////////////////////
	/// harness

	@After
	public void cleanup() {
		RefreshStateTracker.clearDebugObservers();
	}

	private void assertNoLaunchConfigs(String typeId) throws CoreException {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = lm.getLaunchConfigurationType(typeId);
		ILaunchConfiguration[] confs = lm.getLaunchConfigurations(type);
		assertTrue(confs == null || confs.length == 0);
	}

	private void assertNoActiveDevtoolsClientLaunch(GenericRemoteAppElement el) throws CoreException {
		List<ILaunch> launches = getDevtoolsClientLaunches(el);
		assertTrue(launches.isEmpty());
	}

	private ILaunch assertActiveDevtoolsClientLaunch(GenericRemoteAppElement el) throws CoreException {
		List<ILaunch> launches = getDevtoolsClientLaunches(el);
		assertEquals(1, launches.size());
		return launches.get(0);
	}

	private List<ILaunch> getDevtoolsClientLaunches(GenericRemoteAppElement el) throws CoreException {
		List<ILaunch> launches = new ArrayList<>();
		for (ILaunch l : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
			if (!l.isTerminated()) {
				ILaunchConfiguration conf = l.getLaunchConfiguration();
				if (conf.getType().getIdentifier().equals(BootDevtoolsClientLaunchConfigurationDelegate.TYPE_ID)) {
					String url = DevtoolsUtil.remoteUrl(el);
					assertTrue(url.startsWith("http"));
					if (url.equals(conf.getAttribute(BootDevtoolsClientLaunchConfigurationDelegate.REMOTE_URL, ""))) {
						launches.add(l);
					}
				}
			}
		}
		return launches;
	}

	private ILaunch assertActiveDebugLaunch(GenericRemoteAppElement el) throws CoreException {
		DockerContainer container = (DockerContainer)el.getAppData();
		String containerId = container.getName();
		List<ILaunch> launches = new ArrayList<>();
		for (ILaunch l : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
			if (!l.isTerminated()) {
				ILaunchConfiguration conf = l.getLaunchConfiguration();
				if (conf!=null && containerId.equals(conf.getAttribute(RemoteJavaLaunchUtil.APP_NAME, ""))) {
					launches.add(l);
				}
			}
		}
		assertEquals(1, launches.size());
		return launches.get(0);
	}

	private String getSessionId(GenericRemoteAppElement dep) {
		App data = dep.getAppData();
		assertTrue(data instanceof DockerApp);
		return ((DockerApp)data).deployment().getSessionId();
	}

	private String getSessionId(RemoteBootDashModel model) {
		DockerRunTarget target = (DockerRunTarget) model.getRunTarget();
		return target.getSessionId();
	}

	private GenericRemoteBootDashModel<DockerClient, DockerTargetParams> createDockerTarget() throws Exception {
		DockerRunTargetType target = injections().getBean(DockerRunTargetType.class);
		AddRunTargetAction createTarget = getCreateTargetAction(target);
		assertNotNull(createTarget);
		assertTrue(createTarget.isEnabled());

		doAnswer(invocation -> {
			Model model = (Model) invocation.getArguments()[0];
			model.performOk();
			return null;
		}).when(ui()).selectDockerDaemonDialog(Matchers.any());

		createTarget.run();
		createTarget.waitFor(Duration.ofMillis(5000));

		BootDashModel model;
			//		ACondition.waitFor("Run target model to appear", 2000, () -> {
			assertNotNull(model = harness.getRunTargetModel(target));
//		});
		return (GenericRemoteBootDashModel<DockerClient, DockerTargetParams>) model;
	}

	private void assertNoImage(String imageId) throws Exception {
		for (Image img : client().listImages(ListImagesParam.allImages())) {
			assertFalse(imageId.equals(img.id()));
		}
	}

	private GenericRemoteAppElement waitForChild(GenericRemoteAppElement dep, Predicate<App> selector) throws Exception {
		ACondition.waitFor("node to appear", 60_000, () -> {
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

	private GenericRemoteAppElement waitForDeployment(RemoteBootDashModel model,
			IProject project) throws Exception {
		ACondition.waitFor("project deployment node", 2_000, () -> {
			getDeployment(model, project);
		});
		GenericRemoteAppElement d = getDeployment(model, project);
		return d;
	}

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
		context.injections.defInstance(RunTargetType.class, RunTargetTypes.LOCAL);
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


	private DeployToRemoteTargetAction debugOnDockerAction() {
		ImmutableList<IAction> as = actions().getDebugOnTargetActions();
		for (IAction a : as) {
			if (a instanceof DeployToRemoteTargetAction) {
				DeployToRemoteTargetAction deployAction = (DeployToRemoteTargetAction) a;
				RemoteRunTarget target = ((DeployToRemoteTargetAction) a).getTarget();
				if (DockerRunTargetType.class == target.getType().getClass()) {
					return deployAction;
				}
			}
		}
		throw new NoSuchElementException("Debug On Docker Target Action not found");
	}

	private RunStateAction runstateAction(RunState state, String expectedLabel) {
		for (RunStateAction a : actions().getRunStateActions()) {
			if (a.getGoalState() == state) {
				assertEquals(expectedLabel, a.getText());
				return a;
			}
		}
		fail("Cannot find runstate action for "+state);
		return null;
	}

	private RunStateAction stopAction() {
		return runstateAction(RunState.INACTIVE, "Stop");
	}

	private RunStateAction restartAction() {
		return runstateAction(RunState.RUNNING, "(Re)start");
	}

	private void setTags(Taggable e, String... tags) {
		LinkedHashSet<String> tagSet = new LinkedHashSet<>();
		for (String tag : tags) {
			tagSet.add(tag);
		}
		e.setTags(tagSet);
	}

	private void assertTags(Taggable e, String... tags) {
		LinkedHashSet<String> tagSet = new LinkedHashSet<>();
		for (String tag : tags) {
			tagSet.add(tag);
		}
		assertEquals(tagSet, e.getTags());
	}
}
