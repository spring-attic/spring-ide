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

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel;
import org.springframework.ide.eclipse.boot.dash.model.ToggleFiltersModel.FilterChoice;

import static org.mockito.Mockito.*;

import org.eclipse.core.resources.IProject;

@SuppressWarnings("unchecked")
public class ToggleFiltersModelTest {

	private static final String HIDE_NON_WORKSPACE_ELEMENTS = "Hide non-workspace elements";


	@Test
	public void testAvailableFilters() throws Exception {
		ToggleFiltersModel model = new ToggleFiltersModel();
		assertThat(model.getAvailableFilters(),
			arrayContaining(
					hasToString("FilterChoice("+HIDE_NON_WORKSPACE_ELEMENTS+")")
			)
		);
	}

	@Test
	public void testHideNonWorkspaceElementsToleratesNull() throws Exception {
		//Case: element == null
		//  Basically this shouldn't happen, but if it does, the filter does
		//  something sensible.

		Filter<BootDashElement> f = getFilter(HIDE_NON_WORKSPACE_ELEMENTS);

		assertEquals(false, f.accept(null));
	}

	@Test
	public void testHideNonWorkspaceElementsNullProject() throws Exception {
		//Case: project == null means not associated with ws project

		Filter<BootDashElement> f = getFilter(HIDE_NON_WORKSPACE_ELEMENTS);

		BootDashElement e = mock(BootDashElement.class);
		when(e.getProject()).thenReturn(null); // unnessary, but just for clarity
		assertEquals(false, f.accept(e));
	}

	@Test
	public void testHideNonWorkspaceElementsProjectNoExist() throws Exception {
		//Case 2" project that doesn't exist.
		// => may have been associated with a project, but project no longer in workspace

		Filter<BootDashElement> f = getFilter(HIDE_NON_WORKSPACE_ELEMENTS);
		BootDashElement e = mock(BootDashElement.class);
		IProject p = mock(IProject.class);

		when(e.getProject()).thenReturn(p);
		when(p.exists()).thenReturn(false);

		assertEquals(false, f.accept(e));
	}

	@Test
	public void testHideNonWorkspaceElementsProjectExist() throws Exception {
		//Case 2" project that doesn't exist.
		// => may have been associated with a project, but project no longer in workspace

		Filter<BootDashElement> f = getFilter(HIDE_NON_WORKSPACE_ELEMENTS);
		BootDashElement e = mock(BootDashElement.class);
		IProject p = mock(IProject.class);

		when(e.getProject()).thenReturn(p);
		when(p.exists()).thenReturn(true);

		assertEquals(true, f.accept(e));
	}

	private Filter<BootDashElement> getFilter(String withLabel) {
		ToggleFiltersModel model = new ToggleFiltersModel();
		FilterChoice selectFilter = getFilter(model, withLabel);
		model.getSelectedFilters().add(selectFilter);
		Filter<BootDashElement> effectiveFilter = model.getFilter().getValue();
		return effectiveFilter;
	}

	private FilterChoice getFilter(ToggleFiltersModel model, String withLabel) {
		for (FilterChoice choice : model.getAvailableFilters()) {
			if (choice.getLabel().equals(withLabel)) {
				return choice;
			}
		}
		fail("No available filter has this label '"+withLabel+"'");
		return null;
	}

}
