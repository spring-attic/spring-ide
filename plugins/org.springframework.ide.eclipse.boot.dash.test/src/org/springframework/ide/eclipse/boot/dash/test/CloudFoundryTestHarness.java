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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunnableContext;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class CloudFoundryTestHarness extends BootDashViewModelHarness {

	/**
	 * How long to wait for deleted app to disapear from the model.
	 */
	public static final long APP_DELETE_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	/**
	 * How long to wait for a deployed app to show up in the model? This should
	 * be relatively short.
	 */
	public static final long APP_IS_VISIBLE_TIMEOUT = 10_000;

	/**
	 * How long to wait for a deployed app to transition to running state.
	 */
	public static final long APP_DEPLOY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	/**
	 * How long to wait for runtarget to become 'connected'.
	 */
	public static final long CONNECT_TARGET_TIMEOUT = 10_000;

	public static CloudFoundryTestHarness create(BootDashModelContext context) throws Exception {
		CloudFoundryClientFactory clientFactory = new CloudFoundryClientFactory();
		CloudFoundryRunTargetType cfTargetType = new CloudFoundryRunTargetType(context, clientFactory);
		return new CloudFoundryTestHarness(context, clientFactory, cfTargetType);
	}

	private Set<String> ownedAppNames  = new HashSet<>();

	public CloudFoundryBootDashModel getCfTargetModel() {
		return (CloudFoundryBootDashModel) getRunTargetModel(cfTargetType);
	}

	public CloudFoundryBootDashModel createCfTarget(CfTestTargetParams params) throws Exception {
		CloudFoundryTargetWizardModel wizard = new CloudFoundryTargetWizardModel(cfTargetType, clientFactory, NO_TARGETS, context);
		wizard.setUrl(params.getApiUrl());
		wizard.setUsername(params.getUser());
		wizard.setPassword(params.getPassword());
		wizard.setSelfsigned(false);
		wizard.resolveSpaces(new MockRunnableContext());
		wizard.setSpace(getSpace(wizard, params.getOrg(), params.getSpace()));
		assertOk(wizard.getValidator());
		final CloudFoundryRunTarget newTarget = wizard.finish();
		if (newTarget!=null) {
			model.getRunTargets().add(newTarget);
		}
		final CloudFoundryBootDashModel targetModel = getCfModelFor(newTarget);
		//The created targetModel automatically connected, but this happens asynchly.
		new ACondition("Wait for connected state", CONNECT_TARGET_TIMEOUT) {
			public boolean test() throws Exception {
				return targetModel.isConnected();
			}
		};
		return targetModel;
	}

	public CloudFoundryBootDashModel getCfModelFor(CloudFoundryRunTarget cfTarget) {
		return (CloudFoundryBootDashModel) model.getSectionByTargetId(cfTarget.getId());
	}

	public String randomAppName() {
		String name = randomAlphabetic(15);
		ownedAppNames.add(name);
		return name;
	}

	private static final List<RunTarget> NO_TARGETS = ImmutableList.of();

	private CloudFoundryClientFactory clientFactory;
	private CloudFoundryRunTargetType cfTargetType;

	private CloudFoundryTestHarness(BootDashModelContext context, CloudFoundryClientFactory clientFactory, CloudFoundryRunTargetType cfTargetType) throws Exception {
		super(context, RunTargetTypes.LOCAL, cfTargetType);
		this.clientFactory = clientFactory;
		this.cfTargetType = cfTargetType;
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

	public void dispose() {
		deleteOwnedApps();
		super.dispose();
	}

	protected void deleteOwnedApps() {
		if (!ownedAppNames.isEmpty()) {
			CloudFoundryBootDashModel cfModel = getCfTargetModel();
			//TODO: A potential issue with this cleanup code is that it won't work if the test disconnected
			// the model, but it is good enough for now, let's just make sure it fails loudly when its not
			// good enough anymore.
			assertTrue("Cleanup not (yet) supported when model is disconnected", cfModel.isConnected());
			Set<BootDashElement> toDelete = new HashSet<>();
			for (String appName : ownedAppNames) {
				CloudDashElement app = cfModel.getElement(appName);
				if (app!=null) {
					toDelete.add(app);
				}
			}
			UserInteractions ui = mock(UserInteractions.class);
			when(ui.confirmOperation(anyString(), anyString())).thenReturn(true);
			cfModel.delete(toDelete, ui);
		}
	}

}
