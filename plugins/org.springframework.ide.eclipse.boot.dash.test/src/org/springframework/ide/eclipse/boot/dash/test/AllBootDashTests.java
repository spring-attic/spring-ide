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
import org.springframework.ide.eclipse.boot.dash.test.yaml.ManifestCompareMergeTests;

@RunWith(Suite.class)
@SuiteClasses({
	//Tests suites are put in order roughly based on
	// how long it takes to run them. Faster ones at the top.

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
	CloudFoundryBootDashModelMockingTest.class,

	//Medium length (less than 30 seconds):
	JarNameGeneratorTest.class,
	BootJarPackagingTest.class,
	BootDashViewModelTest.class,

	//Long tests (more than 30 seconds):
	BootDashActionTests.class,
	BootDashModelTest.class,
	CloudFoundryBootDashModelIntegrationTest.class,
})
public class AllBootDashTests {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.test";

}
