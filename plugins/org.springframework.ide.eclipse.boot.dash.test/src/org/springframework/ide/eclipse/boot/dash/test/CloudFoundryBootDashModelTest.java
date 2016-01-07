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

import static org.mockito.Mockito.mock;

import java.util.List;

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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunnableContext;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;

public class CloudFoundryBootDashModelTest {

	private TestBootDashModelContext context;
	private BootDashViewModelHarness harness;
	private BootProjectTestHarness projects;
	private UserInteractions ui;

	//// move to some kind of 'CF harness' class? //////////////
	private static final List<RunTarget> NO_TARGETS = ImmutableList.of();
	private CloudFoundryClientFactory clientFactory;
	private RunTargetType targetType;

	////////////////////////////////////////////////////////////


	@Rule
	public AutobuildingEnablement disableAutoBuild = new AutobuildingEnablement(false);

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.clientFactory = new CloudFoundryClientFactory();
		this.targetType = new CloudFoundryRunTargetType(clientFactory);
		this.harness = new BootDashViewModelHarness(context, targetType);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@Test
	public void testCreateCfTarget() throws Exception {
		//TODO: create this test for real
		CloudFoundryRunTarget target =  createCfTarget(CfTestTargetParams.fromEnv());

	}

	///////////////////////////////////////////////////////////////////////////////////

	private CloudFoundryRunTarget createCfTarget(CfTestTargetParams params) throws Exception {
		CloudFoundryTargetWizardModel wizard = new CloudFoundryTargetWizardModel(targetType, clientFactory, NO_TARGETS, context);
		wizard.setUrl(params.getApiUrl());
		wizard.setUsername(params.getUser());
		wizard.setPassword(params.getPassword());
		wizard.setSelfsigned(false);
		wizard.resolveSpaces(new MockRunnableContext());

//		CloudSpace space = getSpace(wizard, ); //TODO



//		wizard.setSpace(space);
		return null;
	}

	private void resolveSpaces(CloudFoundryTargetWizardModel wizard) {
//		wizard.resolveSpaces(context)
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

		this.harness.dispose();
	}

}
