/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelStateSaver;

/**
 * @author Kris De Volder
 */
public class BootDashModelStateSaverTest extends BootDashTestHarness {

	private TestBootDashModelContext context;
	private BootDashElementFactory factory;

	private ISavedState emptySavedState() {
		ISavedState mock = mock(ISavedState.class);
		when(mock.lookup(any(IPath.class))).thenReturn(null);
		when(mock.getSaveNumber()).thenReturn(1);
		return mock;
	}

	@Before
	public void setup() throws Exception {
		this.context = new TestBootDashModelContext();
		this.factory = mock(BootDashElementFactory.class);
	}

	////////////////////////////////////////////////////////////////

	@Test
	public void testFail() throws Exception {
		fail("This test should fail");
	}

	@Test
	public void testRestoreFromEmptyStateLocation() throws Exception {
		BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
		state.restore(emptySavedState());

		assertTrue(state.isEmpty());
	}

	@Test
	public void testGetAndSetPreferredConfig() throws Exception {
		BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
		assertTrue(state.isEmpty());

		IProject project = mockProject("foo");
		BootDashElement dashElement = mock(BootDashElement.class);
		when(factory.create(project)).thenReturn(dashElement);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		state.setPreferredConfig(dashElement, conf);

		assertEquals(conf, state.getPreferredConfig(dashElement));
	}

	@Test
	public void testSaveAndRestoreEmptyState() throws Exception {
		int saveNumber = 13;
		Path UNMAPPED_PATH = new Path("preferredLaunches");
		IPath saveFilePath = context.getStateLocation().append("preferredLaunches-"+saveNumber);
		File saveFile = saveFilePath.toFile();
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			assertTrue(state.isEmpty());

			ISaveContext saveContext = mock(ISaveContext.class);
			when(saveContext.getSaveNumber()).thenReturn(saveNumber);

			assertFalse(saveFile.exists());

			state.prepareToSave(saveContext);
			state.saving(saveContext);
			state.doneSaving(saveContext);

			verify(saveContext).map(UNMAPPED_PATH, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(UNMAPPED_PATH)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());
		}
	}

	@Test
	public void testSaveAndRestoreState() throws Exception {
		int saveNumber = 13;
		Path UNMAPPED_PATH = new Path("preferredLaunches");
		IPath saveFilePath = context.getStateLocation().append("preferredLaunches-"+saveNumber);
		File saveFile = saveFilePath.toFile();


		IProject project = mockProject("foo");
		BootDashElement dashElement = mockElement(project);
		when(factory.create(project)).thenReturn(dashElement);
		when(context.getWorkspace().getRoot().getProject("foo"))
			.thenReturn(project);

		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		when(conf.getMemento()).thenReturn("fooMemento");
		when(context.getLaunchManager().getLaunchConfiguration("fooMemento"))
			.thenReturn(conf);
		when(conf.exists()).thenReturn(true);

		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			assertTrue(state.isEmpty());

			ISaveContext saveContext = mock(ISaveContext.class);
			when(saveContext.getSaveNumber()).thenReturn(saveNumber);

			assertFalse(saveFile.exists());

			state.setPreferredConfig(dashElement, conf);

			state.prepareToSave(saveContext);
			state.saving(saveContext);
			state.doneSaving(saveContext);

			verify(saveContext).map(UNMAPPED_PATH, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(UNMAPPED_PATH)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertFalse(state.isEmpty());

			assertEquals(conf, state.getPreferredConfig(dashElement));
		}
	}

	////////////////////////////////////////////////////////////////

	private BootDashElement mockElement(IProject project) {
		BootDashElement el = mock(BootDashElement.class);
		when(el.getProject()).thenReturn(project);
		return el;
	}

	private IProject mockProject(String name) {
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(name);
		when(project.exists()).thenReturn(true);
		return project;
	}

	@After
	public void teardown() throws Exception {
		context.teardownn();
		factory.dispose();
	}


}
