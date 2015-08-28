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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelStateSaver;

/**
 * @author Kris De Volder
 */
public class BootDashModelStateSaverTest extends Mocks {

	private TestBootDashModelContext context;
	private BootDashElementFactory factory;

	/**
	 * Path of the save file without save numbers, relative to the state location.
	 */
	private final IPath SAVE_FILE_NAME = new Path("preferredLaunches");

	private ISavedState emptySavedState() {
		ISavedState mock = mock(ISavedState.class);
		when(mock.lookup(any(IPath.class))).thenReturn(null);
		when(mock.getSaveNumber()).thenReturn(1);
		return mock;
	}

	private ISavedState mockSavedState(int saveNumber, File saveFile) {
		ISavedState mock = mock(ISavedState.class);
		when(mock.getSaveNumber()).thenReturn(saveNumber);
		when(mock.lookup(any(IPath.class))).thenReturn(new Path(saveFile.toString()));
		return mock;
	}

	@Before
	public void setup() throws Exception {
		this.context = spy(new TestBootDashModelContext(mock(IWorkspace.class), mock(ILaunchManager.class)));
		when(context.getWorkspace().getRoot()).thenReturn(mock(IWorkspaceRoot.class));
		this.factory = mock(BootDashElementFactory.class);
	}

	////////////////////////////////////////////////////////////////

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

		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mock(BootDashElement.class);
		when(factory.createOrGet(project)).thenReturn(dashElement);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		state.setPreferredConfig(dashElement, conf);

		assertEquals(conf, state.getPreferredConfig(dashElement));
	}

	@Test
	public void testSaveAndRestoreEmptyState() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
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

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mockSavedState(saveNumber, saveFilePath.toFile());
			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());
		}
	}

	@Test
	public void testSaveAndRestoreState() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
		File saveFile = saveFilePath.toFile();


		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
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

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(SAVE_FILE_NAME)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertFalse(state.isEmpty());

			assertEquals(conf, state.getPreferredConfig(dashElement));
		}
	}

	@Test
	public void testSaveWithDeletedProject() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
		File saveFile = saveFilePath.toFile();


		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
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

			when(project.exists()).thenReturn(false); //pretend project deleted prior to save

			state.prepareToSave(saveContext);
			state.saving(saveContext);
			state.doneSaving(saveContext);

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);


			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(SAVE_FILE_NAME)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());

			assertEquals(null, state.getPreferredConfig(dashElement));
		}
	}

	@Test
	public void testSaveWithDeletedConf() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
		File saveFile = saveFilePath.toFile();


		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
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

			when(conf.exists()).thenReturn(false); //pretend conf deleted prior to save

			state.prepareToSave(saveContext);
			state.saving(saveContext);
			state.doneSaving(saveContext);

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(SAVE_FILE_NAME)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());

			assertEquals(null, state.getPreferredConfig(dashElement));
		}
	}

	@Test
	public void testRestoreWithDeletedProject() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
		File saveFile = saveFilePath.toFile();

		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
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

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			when(project.exists()).thenReturn(false); //pretend project deleted prior to restore

			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(SAVE_FILE_NAME)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());

			assertEquals(null, state.getPreferredConfig(dashElement));
		}
	}

	@Test
	public void testRestoreWithDeletedConf() throws Exception {
		int saveNumber = 13;
		IPath saveFilePath = context.getStateLocation().append(SAVE_FILE_NAME+"-"+saveNumber);
		File saveFile = saveFilePath.toFile();


		IProject project = mockProject("foo", true);
		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
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

			verify(saveContext).map(SAVE_FILE_NAME, saveFilePath);
			verify(saveContext).needSaveNumber();

			assertTrue(saveFile.exists());
		}
		///////////////////////////////////////////////////////////////
		{
			when(conf.exists()).thenReturn(false); //pretend project deleted prior to restore

			BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
			ISavedState savedState = mock(ISavedState.class);
			when(savedState.getSaveNumber()).thenReturn(saveNumber);
			when(savedState.lookup(SAVE_FILE_NAME)).thenReturn(saveFilePath);

			assertTrue(state.isEmpty());

			state.restore(savedState); //Check this actually loaded the file somehow?

			assertTrue(state.isEmpty());

			assertEquals(null, state.getPreferredConfig(dashElement));
		}
	}


	@Test
	public void testRestoreNoPriorSave() throws Exception {
		IProject project = mockProject("foo", true);

		BootDashElement dashElement = mockElement(project);
		when(factory.createOrGet(project)).thenReturn(dashElement);
		when(context.getWorkspace().getRoot().getProject("foo"))
			.thenReturn(project);

		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		when(conf.getMemento()).thenReturn("fooMemento");
		when(context.getLaunchManager().getLaunchConfiguration("fooMemento"))
			.thenReturn(conf);
		when(conf.exists()).thenReturn(true);

		BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
		ISavedState savedState = null;
		assertTrue(state.isEmpty());

		state.restore(savedState);
		assertTrue(state.isEmpty());

		assertEquals(null, state.getPreferredConfig(dashElement));
	}

	@Test
	public void testRestoreWithCorruptedSaveData() throws Exception {
		BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
		IPath corruptFilePath = context.getStateLocation().append("test-corrupt");
		File corruptFile = corruptFilePath.toFile();
		FileUtils.write(corruptFile, "JUNK");

		int saveNumber = 13;
		ISavedState savedState = mockSavedState(saveNumber, corruptFile);
		assertTrue(state.isEmpty());

		state.restore(savedState);
		assertTrue(state.isEmpty());

		verify(context).log(exceptionWith("java.io.StreamCorruptedException"));
	}


	@Test
	public void testDoneSaving() throws Exception {
		int previousSaveNumber = 12;
		BootDashModelStateSaver state = new BootDashModelStateSaver(context, factory);
		File previousSaveFile = context.getStateLocation().append(SAVE_FILE_NAME+"-"+previousSaveNumber).toFile();
		FileUtils.write(previousSaveFile, "This data really doesn't matter");

		assertTrue(previousSaveFile.exists());

		ISaveContext saveContext = mock(ISaveContext.class);
		when(saveContext.getPreviousSaveNumber()).thenReturn(previousSaveNumber);

		state.doneSaving(saveContext);

		assertFalse(previousSaveFile.exists());
	}


	////////////////////////////////////////////////////////////////

	private Exception exceptionWith(final String snippet) {
		Matcher<Exception> matcher = new BaseMatcher<Exception>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("Exception with text fragment '"+snippet+"'");
			}

			@Override
			public boolean matches(Object item) {
				if (item instanceof Throwable) {
					return stacktrace((Throwable)item).contains(snippet);
				}
				return false;
			}

			private String stacktrace(Throwable item) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream pout = new PrintStream(out);
				try {
					item.printStackTrace(pout);
				} finally {
					pout.close();
				}
				return new String(out.toByteArray());
			}

			@Override
			public void describeMismatch(Object item, Description mismatchDescription) {
				if (item instanceof Throwable) {
					mismatchDescription.appendText("Expecting snippet: "+snippet);
					mismatchDescription.appendText("\n"+stacktrace((Throwable)item));
				} else {
					mismatchDescription.appendText("Expecting a throwable but got "+item);
				}
			}
		};
		return argThat(matcher);
	}

	private BootDashElement mockElement(IProject project) {
		BootDashElement el = mock(BootDashElement.class);
		when(el.getProject()).thenReturn(project);
		return el;
	}

	@After
	public void teardown() throws Exception {
		context.teardownn();
		factory.dispose();
	}


}
