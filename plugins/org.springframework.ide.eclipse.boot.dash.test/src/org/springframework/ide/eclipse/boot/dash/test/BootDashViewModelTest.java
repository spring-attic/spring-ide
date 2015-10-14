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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.LocalRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargets;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel.FilterChoice;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockBootDashModel;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunTarget;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockRunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
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

	@Test
	public void testElementStateListenerAddedAfterModel() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);
		//We need a more fleshed-out BootDashModel mock for this test, so not using mockito here:
		BootDashModel bdm = new MockBootDashModel(target, harness.context);

		when(target.getId()).thenReturn("target_id");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bdm);

		//////Add target///////

		harness.model.getRunTargets().add(target);

		//Make sure the model got added as expected
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		BootDashElement element = mock(BootDashElement.class);
		bdm.getElements().add(element);

		/////Add listener////////

		ElementStateListener listener = mock(ElementStateListener.class);
		harness.model.addElementStateListener(listener);

		////Fire event///////////

		bdm.notifyElementChanged(element);

		/////Verify listener

		verify(listener).stateChanged(element);

		////////////////////////////////////////////////////////////////////////////

		reset(listener);

		harness.model.removeElementStateListener(listener);
		bdm.notifyElementChanged(element);

		verifyZeroInteractions(listener);

	}

	@Test
	public void testElementStateListenerAddedBeforeModel() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);
		//We need a more fleshed-out BootDashModel mock for this test, so not using mockito here:
		BootDashModel bdm = new MockBootDashModel(target, harness.context);

		when(target.getId()).thenReturn("target_id");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bdm);

		/////Add listener////////

		ElementStateListener listener = mock(ElementStateListener.class);
		harness.model.addElementStateListener(listener);

		//////Add target///////

		harness.model.getRunTargets().add(target);

		//Make sure the model got added as expected
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		BootDashElement element = mock(BootDashElement.class);
		bdm.getElements().add(element);

		////Fire event///////////

		bdm.notifyElementChanged(element);

		/////Verify listener

		verify(listener).stateChanged(element);
	}

	@Test
	public void testRemoveTargetToleratesNull() throws Exception {
		UserInteractions ui = mock(UserInteractions.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL
		);
		harness.model.removeTarget(null, ui);

		verifyZeroInteractions(ui);
	}

	@Test
	public void testRemoveTargetCanceled() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);
		BootDashModel bdm = new MockBootDashModel(target, harness.context);

		when(target.getId()).thenReturn("target_id");
		when(target.getName()).thenReturn("target_name");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bdm);

		//////Add target///////
		harness.model.getRunTargets().add(target);

		//Make sure the model got added as expected
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		/////Remove target operation CANCELED ////

		UserInteractions ui = mock(UserInteractions.class);
		when(ui.confirmOperation(
			contains("Deleting run target: target_name"),
			contains("Are you sure")
		)).thenReturn(false);

		harness.model.removeTarget(target, ui);

		//Since user canceled, the target should NOT actually have been removed
		assertTrue(harness.model.getRunTargets().contains(target));

//		verify(ui).confirmOperation(anyString(), anyString());
//		verifyNoMoreInteractions(ui);
	}

	@Test
	public void testRemoveTargetConfirmed() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);
		BootDashModel bdm = new MockBootDashModel(target, harness.context);

		when(target.getId()).thenReturn("target_id");
		when(target.getName()).thenReturn("target_name");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bdm);

		//////Add target///////
		harness.model.getRunTargets().add(target);

		//Make sure the model got added as expected
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		/////Remove target operation CANCELED ////

		UserInteractions ui = mock(UserInteractions.class);
		when(ui.confirmOperation(
			contains("Deleting run target: target_name"),
			contains("Are you sure")
		)).thenReturn(true);

		harness.model.removeTarget(target, ui);

		//Since user confirmed, the target should have been removed
		assertFalse(harness.model.getRunTargets().contains(target));

//		verify(ui).confirmOperation(anyString(), anyString());
//		verifyNoMoreInteractions(ui);
	}


	@Test
	public void testRemoveTargetToleratesRemovingNonContainedElement() throws Exception {
		RunTargetType targetType = mock(RunTargetType.class);
		RunTarget target = mock(RunTarget.class);
		RunTarget otherTarget = mock(RunTarget.class);

		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);
		BootDashModel bdm = new MockBootDashModel(target, harness.context);

		when(target.getId()).thenReturn("target_id");
		when(target.getName()).thenReturn("target_name");
		when(target.getType()).thenReturn(targetType);
		when(target.createElementsTabelModel(harness.context)).thenReturn(bdm);

		when(otherTarget.getId()).thenReturn("other_id");

		//////Add target///////
		harness.model.getRunTargets().add(target);

		//Make sure the model got added as expected
		List<BootDashModel> models = harness.getRunTargetModels(targetType);
		assertEquals(1, models.size());

		/////Remove target operation ////

		UserInteractions ui = mock(UserInteractions.class);

		List<RunTarget> targetsBefore = harness.getRunTargets();
		int numTargetsBefore = targetsBefore.size();

		harness.model.removeTarget(otherTarget, ui);
		List<RunTarget> targetsAfter = harness.getRunTargets();

		//Since target is not in the model, nothing should happen.
		assertEquals(targetsBefore, targetsAfter);
		assertEquals(numTargetsBefore, targetsAfter.size());
		verifyZeroInteractions(ui);

//		verify(ui).confirmOperation(anyString(), anyString());
//		verifyNoMoreInteractions(ui);
	}

	@Test
	public void testFilterBox() throws Exception {
		//Note: this test check the relationship between
		// filterBox and filter. It does so indirectly by observing
		// that changing the filterText makes the filter behave as expected.
		//However, it is not (intended to be) an in-depth test of the way the
		// filter matches elements. So it only does some basic test cases.
		//There are more in-depth tests for the filters elsewhere.

		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL
		);

		LiveVariable<String> filterText = harness.model.getFilterBox().getText();
		LiveExpression<Filter<BootDashElement>> filter = harness.model.getFilter();

		assertEquals("", filterText.getValue());
		assertFilterAccepts(true, filter, "a-tag");

		filterText.setValue("foo");
		assertFilterAccepts(false, filter, "a-tag");
		assertFilterAccepts(true, filter, "foo");
	}

	@Test
	public void testToggleFilters() throws Exception {
		//Note: this test check the relationship between
		// filterBox and filter. It does so indirectly by observing
		// that changing the filterText makes the filter behave as expected.
		//However, it is not (intended to be) an in-depth test of the way the
		// filter matches elements. So it only does some basic test cases.
		//There are more in-depth tests for the filters elsewhere.

		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL
		);

		LiveSet<FilterChoice> toggleFilters = harness.model.getToggleFilters().getSelectedFilters();
		LiveExpression<Filter<BootDashElement>> filter = harness.model.getFilter();

		assertTrue(toggleFilters.getValue().isEmpty());
		assertFilterAccepts(true, filter, "a-tag");

		FilterChoice toggleFilter = new FilterChoice("Foo filter", new Filter<BootDashElement>() {
			public boolean accept(BootDashElement t) {
				return t.getTags().contains("foo");
			}
		});
		toggleFilters.add(toggleFilter);
		assertFilterAccepts(false, filter, "a-tag");
		assertFilterAccepts(true, filter, "foo");
	}

	private void assertFilterAccepts(boolean expectedAccept, LiveExpression<Filter<BootDashElement>> filter, String... tags) {
		BootDashElement element = mock(BootDashElement.class);
		when(element.getTags()).thenReturn(new LinkedHashSet<String>(Arrays.asList(tags)));
		assertEquals(expectedAccept, filter.getValue().accept(element));
	}

	@Test
	public void testGetSectionByTargetId() throws Exception {
		BootDashViewModel view = mock(BootDashViewModel.class);
		LiveSet<BootDashModel> sections = new LiveSet<BootDashModel>();
		when(view.getSectionModels()).thenReturn(sections);
		when(view.getSectionByTargetId(anyString())).thenCallRealMethod();

		assertNull(view.getSectionByTargetId("some-id"));

		BootDashModel bdm = mockBDMWithTargetId("some-id");
		BootDashModel other_bdm = mockBDMWithTargetId("other-id");

		sections.add(bdm);
		sections.add(other_bdm);

		assertEquals(bdm,       view.getSectionByTargetId("some-id"));
		assertEquals(other_bdm, view.getSectionByTargetId("other-id"));
		assertEquals(null,      view.getSectionByTargetId("not-found-id"));
	}

	private BootDashModel mockBDMWithTargetId(String id) {
		BootDashModel bdm = mock(BootDashModel.class);
		RunTarget target = mock(RunTarget.class);
		when(bdm.getRunTarget()).thenReturn(target);
		when(target.getId()).thenReturn(id);
		return bdm;
	}


	@Test
	public void testRestoreSingleRunTarget() throws Exception {
		RunTargetType targetType = new MockRunTargetType("MOCK");
		String targetId = "foo";

		harness = new BootDashViewModelHarness(
				RunTargetTypes.LOCAL,
				targetType
		);

		TargetProperties props = new TargetProperties(targetType, targetId);
		props.put("describe", "This is foo");
		RunTarget savedTarget = targetType.createRunTarget(props);
		harness.model.getRunTargets().add(savedTarget);
		BootDashModelContext oldContext = harness.context;

		harness.dispose();

		harness = new BootDashViewModelHarness(oldContext,
				RunTargetTypes.LOCAL,
				targetType
		);

		MockRunTarget restoredTarget = (MockRunTarget)harness.getRunTarget(targetType);

		//Not a stric requirement, but it would be a little strange of the restored
		// target was the exact same object as the saved target (the test may be broken
		// or some state in the model is not cleaned up when it is disposed)
		assertTrue(restoredTarget !=  savedTarget);

		assertEquals(savedTarget, restoredTarget);
		assertEquals("This is foo", restoredTarget.get("describe"));
	}
}
