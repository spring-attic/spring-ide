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

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunnableContext;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class CloudFoundryBootDashModelTest {

	/**
	 * Setting this to true will bump some timeouts to 'practical infinity' so
	 * the stuff doesn't bail out on running a the test while you are debugging it.
	 */
	private static final boolean DEBUG = false;

	private TestBootDashModelContext context;
//	private BootDashViewModelHarness harness;
	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private BootDashViewModelHarness harness;

	//// move to some kind of 'CF harness' class? //////////////
	private static final List<RunTarget> NO_TARGETS = ImmutableList.of();

	/**
	 * How long to wait for a deployed app to show up in the model? This should
	 * be relatively short.
	 */
	private static final long APP_IS_VISIBLE_TIMEOUT = DEBUG?TimeUnit.HOURS.toMillis(1):10_000;

	/**
	 * How long to wait for a deployed app to transition to running state.
	 */
	private static final long APP_DEPLOY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

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
		this.harness = new BootDashViewModelHarness(context, RunTargetTypes.LOCAL, cfTargetType);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@Test
	public void testCreateCfTarget() throws Exception {
		CloudFoundryRunTarget target =  createCfTarget(CfTestTargetParams.fromEnv());
		assertNotNull(target);
		assertNotNull(target.getTargetProperties().getPassword());
		assertEquals(1, harness.getRunTargetModels(cfTargetType).size());
		assertEquals(target, harness.getRunTargetModel(cfTargetType).getRunTarget());
	}

	@Test
	public void testDeployApp() throws Exception {
		final BootProjectDashElement project = harness.getElementFor(projects.createBootWebProject("to-deploy"));
		createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = getCfTargetModel();

		final String appName = randomAlphabetic(15);
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

	}

	///////////////////////////////////////////////////////////////////////////////////

	private CloudFoundryBootDashModel getCfTargetModel() {
		return (CloudFoundryBootDashModel) harness.getRunTargetModel(cfTargetType);
	}

	public CloudFoundryRunTarget createCfTarget(CfTestTargetParams params) throws Exception {
		CloudFoundryTargetWizardModel wizard = new CloudFoundryTargetWizardModel(cfTargetType, clientFactory, NO_TARGETS, context);
		wizard.setUrl(params.getApiUrl());
		wizard.setUsername(params.getUser());
		wizard.setPassword(params.getPassword());
		wizard.setSelfsigned(false);
		wizard.resolveSpaces(new MockRunnableContext());
		wizard.setSpace(getSpace(wizard, params.getOrg(), params.getSpace()));
		assertOk(wizard.getValidator());
		CloudFoundryRunTarget newTarget = wizard.finish();
		if (newTarget!=null) {
			harness.model.getRunTargets().add(newTarget);
		}
		return newTarget;
	}

	private CloudSpace getSpace(CloudFoundryTargetWizardModel wizard, String orgName, String spaceName) {
		for (CloudSpace space : wizard.getSpaces().getOrgSpaces(orgName)) {
			if (space.getName().equals(spaceName)) {
				return space;
			}
		}
		fail("Not found org/space = "+orgName+"/"+spaceName);
		return null;
	}

	@After
	public void tearDown() throws Exception {
		/*
		 * Remove any working sets created by the tests (BDEs filtering tests create working sets)
		 */
		IWorkingSetManager wsManager = PlatformUI.getWorkbench().getWorkingSetManager();
		for (IWorkingSet ws : wsManager.getAllWorkingSets()) {
			if (!ws.isAggregateWorkingSet()) {
				wsManager.removeWorkingSet(ws);
			}
		}
	}

	public static void assertOk(LiveExpression<ValidationResult> validator) {
		ValidationResult status = validator.getValue();
		if (!status.isOk()) {
			fail(status.toString());
		}
	}


}
