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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.eclipse.boot.dash.test.requestmappings.ActuatorClientTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.AppNameReconcilerTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.CFRouteTests;
import org.springframework.ide.eclipse.boot.dash.test.yaml.DeploymentProperties2YamlTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.ManifestCompareMergeTests;
import org.springframework.ide.eclipse.boot.dash.test.yaml.Yaml2DeploymentPropertiesTest;

@RunWith(Suite.class)
@SuiteClasses({
	//Tests suites are put in order roughly based on
	// how long it takes to run them. Faster ones at the top.

	//New: (move down the chain later based on runtime)
	PropertyFileStoreTest.class,

	// Manifest YAML/Deployment Properties tests (less than 2 seconds per suite)
	DeploymentProperties2YamlTest.class,
	Yaml2DeploymentPropertiesTest.class,
	AppNameReconcilerTest.class,
	RouteBuilderTest.class,
	CFRouteTests.class,

	//Really short (less than 2 seconds per suite):
	JLRMethodParserTest.class,
	OrderBasedComparatorTest.class,
	ManifestCompareMergeTests.class,
	ManifestYmlSchemaTest.class,
	ManifestYamlEditorTest.class,
	AbstractLaunchConfigurationsDashElementTest.class,
	BootDashElementTagsTests.class,
	ActuatorClientTest.class,
	ToggleFiltersModelTest.class,

	//Medium length (less than 30 seconds):
	JarNameGeneratorTest.class,
	BootJarPackagingTest.class,
	BootDashViewModelTest.class,
	DeploymentPropertiesDialogModelTests.class,

	//Long tests (more than 30 seconds):
	CloudFoundryBootDashModelMockingTest.class,
	BootDashActionTests.class,
	BootDashModelTest.class,
	CloudFoundryClientTest.class,
	CloudFoundryBootDashModelIntegrationTest.class,
})
public class AllBootDashTests {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.test";

}
