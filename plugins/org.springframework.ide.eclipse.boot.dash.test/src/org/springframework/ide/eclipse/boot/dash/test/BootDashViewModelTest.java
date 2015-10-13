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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargets;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BootDashViewModelTest {

	private BootDashViewModelHarness harness = null;

	@Before
	public void setup() throws Exception {
		StsTestUtil.cleanUpProjects();
	}

	@After
	public void teardown() throws Exception {
		if (harness!=null) {
			harness.dispose();
		}
	}

	@Test
	public void testCreate() throws Exception {
		harness = new BootDashViewModelHarness(RunTargetTypes.LOCAL);
		BootDashModel localModel = harness.getRunTargetModel(RunTargetTypes.LOCAL);
		assertNotNull(localModel);

		assertElements(harness.model.getRunTargets().getValues(),
				RunTargets.LOCAL
		);
	}

	@Test
	public void testGetTargetTypes() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);

		assertElements(harness.model.getRunTargetTypes(),
				RunTargetTypes.LOCAL,
				targetType
		);

	}

	@Test
	public void testAddAndRemoveRunTarget() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		BootDashModel bootDashModel = mock(BootDashModel.class);

		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);

		assertEquals(0, harness.getRunTargetModels(targetType).size());

		when(target.getId()).thenReturn("target_id");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bootDashModel);

		when(targetType.canInstantiate()).thenReturn(true);
		when(targetType.createRunTarget(any(TargetProperties.class))).thenReturn(target);

		when(bootDashModel.getRunTarget()).thenReturn(target);

		LiveSet<RunTarget> runTargets = harness.model.getRunTargets();

		//Adding...
		runTargets.add(target);
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		BootDashModel targetModel = models.get(0);
		assertEquals(target, targetModel.getRunTarget());

		//Removing...
		runTargets.remove(target);

		models = harness.getRunTargetModels(targetType);
		assertEquals(0, models.size());

		assertEquals(1, harness.getRunTargetModels(RunTargetTypes.LOCAL).size());
	}

//	@Test
//	public void testElementStateListener() throws Exception {
//		targetType =
//		harness = new BootDashViewModelHarness(
//				RunTargetTypes.LOCAL,
//				new MockRunTargetType("MOCK")
//		);
//	}

}
