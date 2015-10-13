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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BootDashViewModelTest {


	@Before
	public void setup() throws Exception {
		StsTestUtil.cleanUpProjects();
	}

	@Test
	public void testCreate() throws Exception {
		BootDashViewModelHarness harness = new BootDashViewModelHarness(RunTargetTypes.LOCAL);
		BootDashModel localModel = harness.getRunTargetModel(RunTargetTypes.LOCAL);
		assertNotNull(localModel);
	}

}
