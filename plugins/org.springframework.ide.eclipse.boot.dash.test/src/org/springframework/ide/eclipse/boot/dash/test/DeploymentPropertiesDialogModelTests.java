/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.ide.eclipse.boot.dash.test.BootDashModelTest.waitForJobsToComplete;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameAnnotationModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameReconciler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFApplication;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFDomain;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFSpace;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;
import org.yaml.snakeyaml.Yaml;

/**
 * Tests for {@link DeploymentPropertiesDialogModel}
 * 
 * @author Alex Boyko
 *
 */
public class DeploymentPropertiesDialogModelTests {
	
	public static final String DEFAULT_BUILDPACK = "java_buildpack_offline";

	public static final List<CFCloudDomain> SPRING_CLOUD_DOMAINS = Arrays.<CFCloudDomain>asList(new MockCFDomain("springsource.org"));

	public static Map<String, Object> createCloudDataMap() {
		Map<String, Object> cloudData = new HashMap<>();
		cloudData.put(ApplicationManifestHandler.DOMAINS_PROP, SPRING_CLOUD_DOMAINS);
		cloudData.put(ApplicationManifestHandler.BUILDPACK_PROP, DEFAULT_BUILDPACK);
		return cloudData;
	}

	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private MockCloudFoundryClientFactory clientFactory;
	
	////////////////////////////////////////////////////////////

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.clientFactory = new MockCloudFoundryClientFactory();
		this.projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@After
	public void tearDown() throws Exception {
		waitForJobsToComplete();
		clientFactory.assertOnlyImplementedStubsCalled();
	}
	
	private AppNameReconciler addAppNameReconcilingFeature(final DeploymentPropertiesDialogModel model) {
		final AppNameReconciler appNameReconciler = new AppNameReconciler(new YamlASTProvider(new Yaml()), model.getDeployedAppName());
		
		// File manifest
		model.getFileDocument().addListener(new ValueListener<IDocument>() {
			@Override
			public void gotValue(LiveExpression<IDocument> exp, IDocument value) {
				if (value == null) {
					model.setFileAppNameAnnotationModel(null);
				} else {
					AppNameAnnotationModel appNameAnnotationModel = new AppNameAnnotationModel();
					model.setFileAppNameAnnotationModel(appNameAnnotationModel);
					appNameReconciler.reconcile(value, appNameAnnotationModel, new NullProgressMonitor());
				}
			}
		});
		
		// Manual manifest
		AppNameAnnotationModel appNameAnnotationModel = new AppNameAnnotationModel();
		model.setManualAppNameAnnotationModel(appNameAnnotationModel);
		appNameReconciler.reconcile(model.getManualDocument(), appNameAnnotationModel, new NullProgressMonitor());
		
		return appNameReconciler;
	}
	
	@Test public void testNoTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		DeploymentPropertiesDialogModel model = new DeploymentPropertiesDialogModel(ui, createCloudDataMap(), project, null);
		addAppNameReconcilingFeature(model);
		
		assertFalse(model.isManualManifestType());
		assertFalse(model.isFileManifestType());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertTrue(validationResult.status == IStatus.ERROR);
		assertEquals(DeploymentPropertiesDialogModel.UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertTrue(deploymentProperties == null);
	}

	@Test public void testManualTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		DeploymentPropertiesDialogModel model = new DeploymentPropertiesDialogModel(ui, createCloudDataMap(), project, null);
		addAppNameReconcilingFeature(model);
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertTrue(validationResult.status == IStatus.INFO);
		assertEquals(DeploymentPropertiesDialogModel.ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deploymentProperties.getAppName(), project.getName());
	}

	@Test public void testManualTypeForDeployedApp() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		MockCFApplication cfApp = space.defApp("my-test-app");
		cfApp.setMemory(512);
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = cfApp.getDetailedInfo();
		
		DeploymentPropertiesDialogModel model = new DeploymentPropertiesDialogModel(ui, createCloudDataMap(), project, deployedApp);
		addAppNameReconcilingFeature(model);
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertTrue(model.isManualManifestReadOnly());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertTrue(validationResult.status == IStatus.INFO);
		assertEquals(DeploymentPropertiesDialogModel.CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deploymentProperties.getAppName(), deployedApp.getName());
		assertEquals(cfApp.getMemory(), deploymentProperties.getMemory());
	}
}
