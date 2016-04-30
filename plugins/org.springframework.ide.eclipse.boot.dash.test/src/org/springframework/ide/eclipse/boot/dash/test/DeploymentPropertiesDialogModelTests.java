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
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameAnnotationModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameReconciler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFDomain;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
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
	
	private static final String UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL = "Unknown deployment manifest type. Must be either 'File' or 'Manual'.";
	private static final String NO_SUPPORT_TO_DETERMINE_APP_NAMES = "Support for determining application names is unavailable";
	private static final String MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME = "Manifest does not contain deployment properties for application with name ''{0}''.";
	private static final String MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED = "Manifest does not have any application defined.";
	private static final String ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY = "Enter deployment manifest YAML manually.";
	private static final String CURRENT_GENERATED_DEPLOYMENT_MANIFEST = "Current generated deployment manifest.";
	private static final String CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM = "Choose an existing deployment manifest YAML file from the local file system.";
	private static final String DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED = "Deployment manifest file not selected.";
	
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
	private DeploymentPropertiesDialogModel model;
	private AppNameReconciler reconciler;
	
	////////////////////////////////////////////////////////////

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@After
	public void tearDown() throws Exception {
		if (model != null) {
			model.dispose();
			model = null;
		}
		if (reconciler != null) {
			reconciler = null;
		}
		waitForJobsToComplete();
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
	
	private void createDialogModel(IProject project, CFApplication deployedApp) {
		model = new DeploymentPropertiesDialogModel(ui, createCloudDataMap(), project, deployedApp);
		reconciler = addAppNameReconcilingFeature(model);
	}
	
	private static CFApplication createCfApp(String name, int memory) {
		CFApplication cfApp = mock(CFApplication.class);
		Mockito.when(cfApp.getName()).thenReturn(name);
		Mockito.when(cfApp.getMemory()).thenReturn(memory);
		Mockito.when(cfApp.getBuildpackUrl()).thenReturn(DEFAULT_BUILDPACK);
		Mockito.when(cfApp.getCommand()).thenReturn(null);
		Mockito.when(cfApp.getDiskQuota()).thenReturn(DeploymentProperties.DEFAULT_MEMORY);
		Mockito.when(cfApp.getEnvAsMap()).thenReturn(Collections.emptyMap());
		Mockito.when(cfApp.getGuid()).thenReturn(UUID.randomUUID());
		Mockito.when(cfApp.getInstances()).thenReturn(DeploymentProperties.DEFAULT_INSTANCES);
		Mockito.when(cfApp.getRunningInstances()).thenReturn(DeploymentProperties.DEFAULT_INSTANCES);
		Mockito.when(cfApp.getServices()).thenReturn(Collections.emptyList());
		Mockito.when(cfApp.getStack()).thenReturn(null);
		Mockito.when(cfApp.getState()).thenReturn(CFAppState.STARTED);
		Mockito.when(cfApp.getTimeout()).thenReturn(null);
		Mockito.when(cfApp.getUris()).thenReturn(Arrays.asList(new String[] {"myapp." + SPRING_CLOUD_DOMAINS.get(0)}));
		return cfApp;
	}
	
	@Test public void testNoTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);
		
		assertFalse(model.isManualManifestType());
		assertFalse(model.isFileManifestType());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertTrue(deploymentProperties == null);
	}

	@Test public void testManualTypeSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		assertNotNull(model.getManualAnnotationModel());
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(project.getName(), deploymentProperties.getAppName());
	}

	@Test public void testManualTypeForDeployedApp() throws Exception {
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp("my-test-app", 512);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertTrue(model.isManualManifestReadOnly());
		assertNotNull(model.getManualAnnotationModel());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deploymentProperties.getAppName(), deployedApp.getName());
		assertEquals(deployedApp.getMemory(), deploymentProperties.getMemory());
	}

	@Test(expected = IllegalStateException.class)
	public void testManualTypeManifestTextWhenAppDeployed() throws Exception {
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp("my-test-app", 512);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertTrue(model.isManualManifestReadOnly());
		
		model.setManualManifest("some text");
	}

	@Test public void testManualTypeSetManifestText() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.MANUAL);
		
		assertTrue(model.isManualManifestType());
		assertFalse(model.isManualManifestReadOnly());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(project.getName(), deploymentProperties.getAppName());
		
		String newText = "Some text";
		model.setManualManifest(newText);
		assertEquals(newText, model.getManualDocument().get());
		reconciler.reconcile(model.getManualDocument(), model.getManualAppNameAnnotationModel().getValue(), new NullProgressMonitor());

		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
	}

	@Test public void testNoAppNameAnnotationModel() throws Exception {
		IProject project = projects.createProject("p1");
		IFile file = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n"
		);
		model = new DeploymentPropertiesDialogModel(ui, createCloudDataMap(), project, null);
		model.setSelectedManifest(file);
		model.type.setValue(ManifestType.MANUAL);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(NO_SUPPORT_TO_DETERMINE_APP_NAMES, validationResult.msg);

		model.type.setValue(ManifestType.FILE);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(NO_SUPPORT_TO_DETERMINE_APP_NAMES, validationResult.msg);
	}

	@Test public void testFileManifestFileNotSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.FILE);
		assertEquals(null, model.getFileAnnotationModel());
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
	}

	@Test public void testFileManifestNonYamlFileSelected() throws Exception {
		IProject project = projects.createProject("p1");
		IFile file = createFile(project, "manifest.yml", "Some text content!");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.FILE);
		model.setSelectedManifest(file);
		
		assertNotNull(model.getFileAnnotationModel());

		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
	}
	
	@Test public void testFileManifestFolderSelected() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.FILE);
		model.setSelectedManifest(project);
		
		assertEquals(null, model.getFileAnnotationModel());

		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
	}
	
	@Test public void testFileManifestFileSelected() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile file = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.type.setValue(ManifestType.FILE);
		
		assertNotNull(model.getFileAnnotationModel());

		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);

		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
	}

	@Test public void testSwitchingManifestTypeAndFiles() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile validFile = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		IFile invalidFile = createFile(project, "text.yml", "Some text");				
		
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.MANUAL);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		
		model.type.setValue(ManifestType.FILE);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		
		model.setSelectedManifest(validFile);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		
		model.type.setValue(ManifestType.MANUAL);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		
		model.type.setValue(ManifestType.FILE);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		
		model.setSelectedManifest(project);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		
		model.setSelectedManifest(invalidFile);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED, validationResult.msg);
	}
	
	@Test public void testValidSingleAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);
		
		IFile validFileSingleName = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n"
		);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.FILE);
		
		model.setSelectedManifest(validFileSingleName);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);		
	}

	@Test public void testInvalidSingleAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);
		
		IFile invalidFileSingleName = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.FILE);
		
		model.setSelectedManifest(invalidFileSingleName);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);		
	}

	@Test public void testValidMultiAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);;
		
		IFile validFileMultiName = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.FILE);
		
		model.setSelectedManifest(validFileMultiName);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(deployedApp.getName(), deploymentProperties.getAppName());
	}

	@Test public void testInvalidMultiAppFileSelectedForDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);
		
		IFile invalidFileMultiName = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);
		
		createDialogModel(project, deployedApp);		
		model.type.setValue(ManifestType.FILE);
		
		model.setSelectedManifest(invalidFileMultiName);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertEquals(null, deploymentProperties);
	}
	
	@Test public void testSwitchingWithDeployedApp() throws Exception {
		String appName = "my-test-app";
		IProject project = projects.createProject("p1");
		CFApplication deployedApp = createCfApp(appName, 512);
		
		IFile validFileMultiName = createFile(project, "valid-manifest.yml", 				
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: " + appName + "\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);
		
		IFile invalidFileMultiName = createFile(project, "invalid-manifest.yml", 				
				"applications:\n" +
				"- name: " + project.getName() + "\n" +
				"  memory: 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n"
		);
		
		createDialogModel(project, deployedApp);		
		
		model.type.setValue(ManifestType.FILE);
		model.setSelectedManifest(validFileMultiName);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM, validationResult.msg);

		model.setSelectedManifest(invalidFileMultiName);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
		
		model.setSelectedManifest(project);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED, validationResult.msg);
		
		model.type.setValue(ManifestType.MANUAL);
		assertTrue(model.isManualManifestReadOnly());		
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);

		model.setSelectedManifest(invalidFileMultiName);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(CURRENT_GENERATED_DEPLOYMENT_MANIFEST, validationResult.msg);

		model.type.setValue(ManifestType.FILE);
		validationResult = model.getValidator().getValue();
		assertEquals(IStatus.ERROR, validationResult.status);
		assertEquals(MessageFormat.format(MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME, appName), validationResult.msg);
	}
	
	@Test(expected = OperationCanceledException.class)
	public void testCancel() throws Exception {
		IProject project = projects.createProject("p1");
		createDialogModel(project, null);		
		model.type.setValue(ManifestType.MANUAL);
		
		ValidationResult validationResult = model.getValidator().getValue();
		assertEquals(IStatus.INFO, validationResult.status);
		assertEquals(ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY, validationResult.msg);
		
		model.cancelPressed();
		
		assertTrue(model.isCanceled());
		
		model.getDeploymentProperties();
	}
	
	@Test public void testManifestFileLabel() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		IFile file = createFile(project, "manifest.yml", 				
				"applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n"
		);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		assertEquals(file.getFullPath().toOSString(), model.getFileLabel().getValue());
		
		model.getFileDocument().getValue().set("some text");

		assertEquals(file.getFullPath().toOSString() + "*", model.getFileLabel().getValue());
	}
	
	@Test public void testDiscardCancelWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		model.getFileDocument().getValue().set("some text");
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(1);
		
		model.cancelPressed();
		
		assertTrue(model.isCanceled());		
		assertEquals(text, IOUtil.toString(file.getContents()));
	}

	@Test public void testSaveCancelWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: 512M\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		String newText = "some text";
		model.getFileDocument().getValue().set(newText);
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(0);
		
		model.cancelPressed();
		
		assertTrue(model.isCanceled());		
		assertEquals(newText, IOUtil.toString(file.getContents()));
	}

	@Test public void testDiscardOkWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: " + memory + "\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		model.getFileDocument().getValue().set("some text");
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(1);
		
		model.okPressed();
		
		assertFalse(model.isCanceled());		
		assertEquals(text, IOUtil.toString(file.getContents()));
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
		assertEquals(memory, deploymentProperties.getMemory());
	}

	@Test public void testSaveOkWithDirtyManifestFile() throws Exception {
		IProject project = projects.createProject("p1");
		String appName = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: " + memory + "\n";
		String newAppName = "new-app";
		int newMemory = 768;
		String newText = "applications:\n" +
				"- name: " + newAppName + "\n" +
				"  memory: " + newMemory + "\n";

		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		model.getFileDocument().getValue().set(newText);
		// Reconcile to pick the new app name (Simulate what happens in the editor)
		reconciler.reconcile(model.getFileDocument().getValue(), model.getFileAppNameAnnotationModel().getValue(), new NullProgressMonitor());
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(0);
		
		model.okPressed();
		
		assertFalse(model.isCanceled());		
		assertEquals(newText, IOUtil.toString(file.getContents()));
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(newAppName, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());
	}

	@Test public void testDiscardOnDirtyManifestFileSwitch() throws Exception {
		IProject project = projects.createProject("p1");
		String appNameFromFile = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appNameFromFile + "\n" +
				"  memory: " + memory + "\n";
		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		model.getFileDocument().getValue().set("some text");
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(1);
		
		model.setSelectedManifest(project);
		
		assertEquals(text, IOUtil.toString(file.getContents()));

		model.setSelectedManifest(file);
		
		assertEquals(text, model.getFileDocument().getValue().get());
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(appNameFromFile, deploymentProperties.getAppName());
		assertEquals(memory, deploymentProperties.getMemory());
	}
	
	@Test public void testSaveOnDirtyManifestFileSwitch() throws Exception {
		IProject project = projects.createProject("p1");
		String appName = "app-name-from-file";
		int memory = 512;
		String text = "applications:\n" +
				"- name: " + appName + "\n" +
				"  memory: " + memory + "\n";
		String newAppName = "new-app";
		int newMemory = 768;
		String newText = "applications:\n" +
				"- name: " + newAppName + "\n" +
				"  memory: " + newMemory + "\n";

		IFile file = createFile(project, "manifest.yml", text);
		createDialogModel(project, null);		
		model.setSelectedManifest(file);
		model.setManifestType(ManifestType.FILE);
		
		model.getFileDocument().getValue().set(newText);
		// Reconcile to pick the new app name (Simulate what happens in the editor)
		reconciler.reconcile(model.getFileDocument().getValue(), model.getFileAppNameAnnotationModel().getValue(), new NullProgressMonitor());
		
		Mockito.when(ui.confirmOperation(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenReturn(0);
		
		model.setSelectedManifest(project);
		
		assertEquals(newText, IOUtil.toString(file.getContents()));
		
		model.setSelectedManifest(file);
		
		assertEquals(newText, model.getFileDocument().getValue().get());
		
		CloudApplicationDeploymentProperties deploymentProperties = model.getDeploymentProperties();
		assertNotNull(deploymentProperties);
		assertEquals(newAppName, deploymentProperties.getAppName());
		assertEquals(newMemory, deploymentProperties.getMemory());
	}
}
