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
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.test.requestmappings.ActuatorClientTest;


@RunWith(Suite.class)
@SuiteClasses({
	BootDashViewModelTest.class,
	JarNameGeneratorTest.class,
	BootJarPackagingTest.class,
	BootDashModelStateSaverTest.class,
	BootDashModelTest.class,
	BootProjectDashElementTest.class,
	BootDashElementTagsTests.class,
	JLRMethodParserTest.class,
	ActuatorClientTest.class
})
public class AllBootDashTests {

}
