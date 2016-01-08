/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DEPLOY_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class CloudFoundryBootDashModelTest {

	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private CloudFoundryTestHarness harness;

	private CloudFoundryClientFactory clientFactory;
	private CloudFoundryRunTargetType cfTargetType;

	////////////////////////////////////////////////////////////

	@Rule
	public AutobuildingEnablement disableAutoBuild = new AutobuildingEnablement(false);

	@Rule
	public TestBracketter testBracketter = new TestBracketter();

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.clientFactory = new CloudFoundryClientFactory();
		this.cfTargetType = new CloudFoundryRunTargetType(context, clientFactory);
		this.harness = CloudFoundryTestHarness.create(context);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@Test
	public void testCreateCfTarget() throws Exception {
		CloudFoundryBootDashModel target =  harness.createCfTarget(CfTestTargetParams.fromEnv());
		assertNotNull(target);
		assertNotNull(target.getRunTarget().getTargetProperties().getPassword());
		assertEquals(1, harness.getRunTargetModels(cfTargetType).size());
	}

	@Test
	public void testDeployAppAndDelete() throws Exception {
		harness.createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		final BootProjectDashElement project = harness.getElementFor(projects.createBootWebProject("to-deploy"));
		final String appName = harness.randomAppName();

		when(ui.promptApplicationDeploymentProperties(eq(project.getProject()), anyListOf(CloudDomain.class)))
			.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
				@Override
				public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					@SuppressWarnings("unchecked")
					List<CloudDomain> domains = (List<CloudDomain>) args[1];
					CloudApplicationDeploymentProperties deploymentProperties = new CloudApplicationDeploymentProperties();
					deploymentProperties.setProject(project.getProject());
					deploymentProperties.setAppName(appName);
					String url = appName+"."+domains.get(0).getName();
					deploymentProperties.setUrls(ImmutableList.of(url));
					return deploymentProperties;
				}
			});

		model.add(ImmutableList.<Object>of(project), ui);

		//The resulting deploy is asynchronous
		new ACondition("wait for app '"+ appName +"'to appear", APP_IS_VISIBLE_TIMEOUT) {
			public boolean test() throws Exception {
				assertNotNull(model.getElement(appName));
				return true;
			}
		};

		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				CloudDashElement element = model.getElement(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};

		//Try to delete the app...
		reset(ui);
		when(ui.confirmOperation(eq("Deleting Elements"), anyString())).thenReturn(true);

		CloudDashElement app = model.getElement(appName);
		app.getCloudModel().delete(ImmutableList.<BootDashElement>of(app), ui);

		new ACondition("wait for app to be deleted", APP_DELETE_TIMEOUT) {

			@Override
			public boolean test() throws Exception {
				assertNull(model.getElement(appName));
				return true;
			}
		};
	}

	@Test
	public void testPreexistingApplicationInModel() throws Exception {
		// Create external client and deploy app "externally"
		CfTestTargetParams params = CfTestTargetParams.fromEnv();
		CloudFoundryOperations externalClient = clientFactory.getClient(
				new CloudCredentials(params.getUser(), params.getPassword()), new URL(params.getApiUrl()),
				params.getOrg(), params.getSpace(), params.isSelfsigned());
		List<CloudDomain> domains = externalClient.getDomains();

		List<String> services = new ArrayList<String>();
		int memory = 1024;
		final String preexistingAppName = harness.randomAppName();
		externalClient.createApplication(preexistingAppName, new Staging(), memory,
				ImmutableList.of(preexistingAppName + "." + domains.get(0).getName()), services);

		// Create the boot dash target and model
		harness.createCfTarget(CfTestTargetParams.fromEnv());

		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		final BootProjectDashElement project = harness
				.getElementFor(projects.createBootWebProject("testPreexistingApplicationInModel"));
		final String newAppName = harness.randomAppName();

		// Create a new one too
		when(ui.promptApplicationDeploymentProperties(eq(project.getProject()), anyListOf(CloudDomain.class)))
				.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
					@Override
					public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						@SuppressWarnings("unchecked")
						List<CloudDomain> domains = (List<CloudDomain>) args[1];
						CloudApplicationDeploymentProperties deploymentProperties = new CloudApplicationDeploymentProperties();
						deploymentProperties.setProject(project.getProject());
						deploymentProperties.setAppName(newAppName);
						String url = newAppName + "." + domains.get(0).getName();
						deploymentProperties.setUrls(ImmutableList.of(url));
						return deploymentProperties;
					}
				});

		model.add(ImmutableList.<Object> of(project), ui);

		// The resulting deploy is asynchronous
		new ACondition("wait for apps '" + newAppName + "' and '" + preexistingAppName + "' to appear",
				APP_IS_VISIBLE_TIMEOUT) {
			public boolean test() throws Exception {
				assertNotNull(model.getElement(newAppName));
				assertNotNull(model.getElement(preexistingAppName));

				// check project mapping
				assertEquals("Expected new element in model to have workspace project mapping",
						model.getElement(newAppName).getProject().equals(project));

				// No project mapping for the "external" app
				assertNull(model.getElement(preexistingAppName).getProject());

				// check the actual CloudApplication
				CloudApplication actualNewApp = model.getElement(newAppName).getCloudModel().getAppCache()
						.getApp(newAppName);
				assertEquals("No CloudApplication mapping found", actualNewApp.getName(), newAppName);

				CloudApplication actualPreexistingApp = model.getElement(preexistingAppName).getCloudModel()
						.getAppCache().getApp(preexistingAppName);
				assertEquals("No CloudApplication mapping found", actualPreexistingApp.getName(), preexistingAppName);

				return true;
			}
		};

	}

	///////////////////////////////////////////////////////////////////////////////////

	@After
	public void tearDown() throws Exception {
		harness.dispose();
	}


}
