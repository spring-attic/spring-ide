/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.AbstractCollection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
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
	 * How long to wait on retrieving request mappings from a CF app.
	 */
	public static final long FETCH_REQUEST_MAPPINGS_TIMEOUT = 5_000;
	/**
	 * How long to wait for runtarget to become 'connected'.
	 */
	public static final long CONNECT_TARGET_TIMEOUT = 10_000;

	public static CloudFoundryTestHarness create(BootDashModelContext context) throws Exception {
		CloudFoundryClientFactory clientFactory = CloudFoundryClientFactory.DEFAULT;
		return create(context, clientFactory);
	}

	protected static CloudFoundryTestHarness create(BootDashModelContext context,
			CloudFoundryClientFactory clientFactory) throws Exception {
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
	public final CloudFoundryRunTargetType cfTargetType;

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

	public CloudFoundryOperations createExternalClient(CfTestTargetParams params) throws Exception {
		return clientFactory.getClient(new CloudCredentials(params.getUser(), params.getPassword()),
				new URL(params.getApiUrl()), params.getOrg(), params.getSpace(), params.isSelfsigned());
	}

	protected void deleteOwnedApps() {
		if (!ownedAppNames.isEmpty()) {

			try {
				CloudFoundryOperations externalClient = createExternalClient(CfTestTargetParams.fromEnv());
				for (String appName : ownedAppNames) {
					try {
						externalClient.deleteApplication(appName);
					} catch (Exception e) {
						// May get 404 or other 400 errors if it is alrready
						// gone so don't prevent other owned apps from being
						// deleted
					}
				}

			} catch (Exception e) {
				fail("failed to cleanup owned apps: " + e.getMessage());
			}
		}
	}

	public void answerDeploymentPrompt(UserInteractions ui, final String appName, final String hostName) {
		when(ui.promptApplicationDeploymentProperties(anyListOf(CloudDomain.class), any(IProject.class), any(IFile.class), any(String.class), any(boolean.class), any(boolean.class)))
		.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
			@Override
			public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				@SuppressWarnings("unchecked")
				List<CloudDomain> domains = (List<CloudDomain>) args[0];
				IProject project = (IProject) args[1];
				CloudApplicationDeploymentProperties deploymentProperties = new CloudApplicationDeploymentProperties();
				deploymentProperties.setProject(project.getProject());
				deploymentProperties.setAppName(appName);
				String url = hostName + "." + domains.get(0).getName();
				deploymentProperties.setUris(ImmutableList.of(url));
				return deploymentProperties;
			}
		});
	}

	public List<BootDashModel> getCfRunTargetModels() {
		return getRunTargetModels(cfTargetType);
	}

}
