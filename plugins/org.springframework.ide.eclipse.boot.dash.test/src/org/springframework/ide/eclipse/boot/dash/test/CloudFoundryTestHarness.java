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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunnableContext;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.frameworks.test.util.Asserter1;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class CloudFoundryTestHarness extends BootDashViewModelHarness {

	@FunctionalInterface
	public interface DeploymentAnswerer {

		void apply(DeploymentPropertiesDialogModel model) throws Exception;

	}

	@FunctionalInterface
	public interface PasswordAnswerer {
		void apply(PasswordDialogModel model) throws Exception;
	}

	/**
	 * How long to wait for deleted app to disapear from the model.
	 */
	public static final long APP_DELETE_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	/**
	 * How long to wait for deleted app to disapear from the model.
	 */
	public static final long SERVICE_DELETE_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	/**
	 * How long to wait for a deployed app to show up in the model? This should
	 * be relatively short.
	 */
	public static final long APP_IS_VISIBLE_TIMEOUT = 20_000;

	/**
	 * How long to wait for a deployed app to transition to running state.
	 */
	public static final long APP_DEPLOY_TIMEOUT = TimeUnit.MINUTES.toMillis(6);

	/**
	 * How long to wait on retrieving request mappings from a CF app.
	 */
	public static final long FETCH_REQUEST_MAPPINGS_TIMEOUT = 5_000;
	/**
	 * How long to wait for runtarget to become 'connected'.
	 */
	public static final long CONNECT_TARGET_TIMEOUT = 30_000;

	public static CloudFoundryTestHarness create(BootDashModelContext context) throws Exception {
		CloudFoundryClientFactory clientFactory = DefaultCloudFoundryClientFactoryV2.INSTANCE;
		return create(context, clientFactory);
	}

	protected static CloudFoundryTestHarness create(
			BootDashModelContext context,
			CloudFoundryClientFactory clientFactory
	) throws Exception {
		CloudFoundryRunTargetType cfTargetType = new CloudFoundryRunTargetType(context, clientFactory);
		return new CloudFoundryTestHarness(context, clientFactory, cfTargetType);
	}

	public CloudFoundryBootDashModel getCfTargetModel() {
		return (CloudFoundryBootDashModel) getRunTargetModel(cfTargetType);
	}

	public CloudFoundryBootDashModel createCfTarget(
			CFClientParams params,
			StoreCredentialsMode storePassword
	) throws Exception {
		return createCfTarget(params, storePassword, (wizard) -> assertOk(wizard.getValidator()));
	}

	public CloudFoundryBootDashModel createCfTarget(
			CFClientParams params,
			StoreCredentialsMode storePassword,
			Asserter1<CloudFoundryTargetWizardModel> wizardAsserter
	) throws Exception {
		CloudFoundryTargetWizardModel wizard = new CloudFoundryTargetWizardModel(cfTargetType, clientFactory, NO_TARGETS, context);

		wizard.setUrl(params.getApiUrl());
		wizard.setUsername(params.getUsername());
		wizard.setStoreCredentials(storePassword);
		wizard.setMethod(params.getCredentials().getType().toLoginMethod());
		wizard.setPassword(params.getCredentials().getSecret());
		wizard.setSelfsigned(params.isSelfsigned());
		wizard.skipSslValidation(params.skipSslValidation());
		wizard.resolveSpaces(new MockRunnableContext());
		assertNotNull(wizard.getRefreshToken());
		wizard.setSpace(getSpace(wizard, params.getOrgName(), params.getSpaceName()));
		wizardAsserter.execute(wizard);
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

	public CloudFoundryBootDashModel createCfTarget(CFClientParams params) throws Exception {
		return createCfTarget(params, StoreCredentialsMode.STORE_PASSWORD);
	}

	public CloudFoundryBootDashModel getCfModelFor(CloudFoundryRunTarget cfTarget) {
		return (CloudFoundryBootDashModel) model.getSectionByTargetId(cfTarget.getId());
	}

	private static final ImmutableSet<RunTarget> NO_TARGETS = ImmutableSet.of();

	private CloudFoundryClientFactory clientFactory;
	public final CloudFoundryRunTargetType cfTargetType;

	private CloudFoundryTestHarness(BootDashModelContext context, CloudFoundryClientFactory clientFactory, CloudFoundryRunTargetType cfTargetType) throws Exception {
		super(context, RunTargetTypes.LOCAL, cfTargetType);
		Assert.isNotNull(clientFactory, "clientFactory");
		this.clientFactory = clientFactory;
		this.cfTargetType = cfTargetType;
	}

	private CFSpace getSpace(CloudFoundryTargetWizardModel wizard, String orgName, String spaceName) {
		for (CFSpace space : wizard.getSpaces().getOrgSpaces(orgName)) {
			if (space.getName().equals(spaceName)) {
				return space;
			}
		}
		fail("Not found org/space = "+orgName+"/"+spaceName);
		return null;
	}

	public void answerDeploymentPrompt(UserInteractions ui, final String appName, final String hostName) throws Exception {
		when(ui.promptApplicationDeploymentProperties(any(DeploymentPropertiesDialogModel.class)))
		.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
			@Override
			public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
				DeploymentPropertiesDialogModel dialog = (DeploymentPropertiesDialogModel) invocation.getArguments()[0];
				dialog.setManifestType(ManifestType.MANUAL);
				dialog.setManualManifest(
						"applications:\n" +
						"- name: "+appName+"\n" +
						"  host: "+hostName+"\n"
				);
				dialog.okPressed();
				return dialog.getDeploymentProperties();
			}
		});
	}

	public void answerDeploymentPrompt(UserInteractions ui, final String appName, final String hostName, final List<String> bindServices) throws Exception {
		//TODO: replace this method with something more 'generic' that accepts a function which is passed the deploymentProperties
		// so that it can add additional infos to it.
		answerDeploymentPrompt(ui, (dialog) -> {
			dialog.setManifestType(ManifestType.MANUAL);
			dialog.setManualManifest(
					"applications:\n" +
					"- name: "+appName+"\n" +
					"  host: "+hostName+"\n" +
					createServicesBlock(bindServices)
			);
			dialog.okPressed();
		});
	}

	private String createServicesBlock(List<String> bindServices) {
		if (bindServices==null || bindServices.isEmpty()) {
			return "";
		}
		StringBuilder buf = new StringBuilder("  services:\n");
		for (String s : bindServices) {
			buf.append("  - "+s+"\n");
		}
		return buf.toString();
	}

	public void answerDeploymentPrompt(UserInteractions ui, final String appName, final String hostName, final Map<String,String> env) throws Exception {
		when(ui.promptApplicationDeploymentProperties(any(DeploymentPropertiesDialogModel.class)))
		.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
			@Override
			public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
				DeploymentPropertiesDialogModel dialog = (DeploymentPropertiesDialogModel) invocation.getArguments()[0];
				dialog.setManifestType(ManifestType.MANUAL);
				dialog.setManualManifest(
						"applications:\n" +
						"- name: "+appName+"\n" +
						"  host: "+hostName+"\n" +
						createEnvBlock(env)
				);
				dialog.okPressed();
				return dialog.getDeploymentProperties();
			}

			private String createEnvBlock(Map<String, String> env) {
				if (env==null || env.isEmpty()) {
					return "";
				}
				StringBuilder buf = new StringBuilder("  env:\n");
				for (Entry<String, String> e : env.entrySet()) {
					buf.append("    "+e.getKey()+": "+e.getValue());
				}
				return buf.toString();
			}
		});
	}

	/**
	 * Does the same thing as what would happen if a user answered the deployment props dialog by selecting an
	 * existing manifest file.
	 */
	public void answerDeploymentPrompt(UserInteractions ui, IFile manifestToSelect) throws Exception {
		when(ui.promptApplicationDeploymentProperties(any(DeploymentPropertiesDialogModel.class)))
		.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
			@Override
			public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
				DeploymentPropertiesDialogModel dialog = (DeploymentPropertiesDialogModel) invocation.getArguments()[0];
				dialog.setManifestType(ManifestType.FILE);
				dialog.setSelectedManifest(manifestToSelect);
				dialog.okPressed();
				return dialog.getDeploymentProperties();
			}
		});
	}

	public void answerDeploymentPrompt(UserInteractions ui, DeploymentAnswerer answerer) throws Exception {
		when(ui.promptApplicationDeploymentProperties(any(DeploymentPropertiesDialogModel.class)))
		.thenAnswer(new Answer<CloudApplicationDeploymentProperties>() {
			@Override
			public CloudApplicationDeploymentProperties answer(InvocationOnMock invocation) throws Throwable {
				DeploymentPropertiesDialogModel dialog = (DeploymentPropertiesDialogModel) invocation.getArguments()[0];
				System.out.println(">>>>>>> Opening DeploymentPropertiesDialog");
				System.out.println("TYPE: " + dialog.type.getValue());
				System.out.println("MANUAL MANIFEST: \n" + dialog.getManualDocument().get());
				System.out.println("FILE MANIFEST: "+dialog.getFileLabel().getValue() +"\n");
				System.out.println(getFileContent(dialog));
				answerer.apply(dialog);

				System.out.println(">>>>>>> Closed DeploymentPropertiesDialog: "+(dialog.isCanceled()?"CANCEL":"OK"));
				System.out.println("TYPE: " + dialog.type.getValue());
				System.out.println("MANUAL MANIFEST: \n" + dialog.getManualDocument().get());
				System.out.println("FILE MANIFEST: "+dialog.getFileLabel().getValue() +"\n");
				System.out.println(getFileContent(dialog));

				return dialog.getDeploymentProperties();
			}

		});
	}

	public String privateStoreKey(CloudFoundryBootDashModel target) {
		return secureStoreKey(target)+":token";
	}

	public String secureStoreKey(CloudFoundryBootDashModel target) {
		return target.getRunTarget().getType().getName()+":"+target.getRunTarget().getId();
	}

	public void answerPasswordPrompt(UserInteractions ui, PasswordAnswerer answerer) {
		doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				PasswordDialogModel dialog = (PasswordDialogModel) invocation.getArguments()[0];
				answerer.apply(dialog);
				return dialog.isOk();
			}
		}).when(ui).openPasswordDialog(any(PasswordDialogModel.class));
	}

	private String getFileContent(DeploymentPropertiesDialogModel dialog) {
		IDocument doc = dialog.getFileDocument().getValue();
		if (doc!=null) {
			return doc.get();
		}
		return null;
	}

	public List<BootDashModel> getCfRunTargetModels() {
		return getRunTargetModels(cfTargetType);
	}

	public CloudFoundryRunTargetType getCfTargetType() {
		for (RunTargetType type : model.getRunTargetTypes()) {
			if (type instanceof CloudFoundryRunTargetType) {
				return (CloudFoundryRunTargetType) type;
			}
		}
		return null;
	}

	/**
	 * Raw fetch of environment variables (makes a request through the CF client, rather then get the cached data
	 * from the model).
	 */
	public Map<String, String> fetchEnvironment(CloudFoundryBootDashModel model, String appName) throws Exception {
		return model.getRunTarget().getClient().getApplication(appName).getEnvAsMap();
	}

	public LocalBootDashModel getLocalModel() {
		return (LocalBootDashModel) getRunTargetModel(RunTargetTypes.LOCAL);
	}

	public void answerManifestDiffDialog(UserInteractions ui, Function<ManifestDiffDialogModel, ManifestDiffDialogModel.Result> answerer) throws Exception {
		when(ui.openManifestDiffDialog(any()))
		.thenAnswer(new Answer<ManifestDiffDialogModel.Result>() {
			@Override
			public ManifestDiffDialogModel.Result answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				ManifestDiffDialogModel dialog = (ManifestDiffDialogModel) args[0];
				return answerer.apply(dialog);
			}
		});
	}

	public SecuredCredentialsStore getCredentialsStore() {
		return context.getSecuredCredentialsStore();
	}

	public IPropertyStore getPrivateStore() {
		return context.getPrivatePropertyStore();
	}

}
