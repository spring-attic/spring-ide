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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.eclipse.boot.dash.test.requestmappings.ActuatorClientTest;

@RunWith(Suite.class)
@SuiteClasses({
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
	CloudFoundryBootDashModelTest.class,
	//Test for 'utilities' which may eventually be moved if we reuse them elsewhere
	// (the test should also move then!)
	OrderBasedComparatorTest.class
})
public class AllBootDashTests {

}
