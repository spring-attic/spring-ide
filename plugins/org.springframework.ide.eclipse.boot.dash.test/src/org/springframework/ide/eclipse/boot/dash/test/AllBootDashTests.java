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
import org.springframework.ide.eclipse.boot.dash.test.yaml.DeploymentProperties2Yaml;
import org.springframework.ide.eclipse.boot.dash.test.yaml.ManifestCompareMergeTests;
import org.springframework.ide.eclipse.boot.dash.test.yaml.Yaml2DeploymentProperties;

@RunWith(Suite.class)
@SuiteClasses({
	ManifestYmlSchemaTest.class,
	ManifestYamlEditorTest.class,
	BootDashViewModelTest.class,
	JarNameGeneratorTest.class,
	BootJarPackagingTest.class,
	BootDashModelTest.class,
	BootDashActionTests.class,
	AbstractLaunchConfigurationsDashElementTest.class,
	BootDashElementTagsTests.class,
	JLRMethodParserTest.class,
	ActuatorClientTest.class,
	ToggleFiltersModelTest.class,
	CloudFoundryBootDashModelIntegrationTest.class,
	//Test for 'utilities' which may eventually be moved if we reuse them elsewhere
	// (the test should also move then!)
	OrderBasedComparatorTest.class,
	DeploymentProperties2Yaml.class,
	Yaml2DeploymentProperties.class,
	ManifestCompareMergeTests.class
})
public class AllBootDashTests {
	
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.test";

}
