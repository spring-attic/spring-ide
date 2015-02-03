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
package org.springframework.ide.eclipse.boot.launch.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.BootLaunchUIModel;
import org.springframework.ide.eclipse.boot.launch.SelectProjectLaunchTabModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * @author Kris De Volder
 */
public class BootLaunchUIModelTest extends BootLaunchTestCase {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testProjectValidator() throws Exception {
		createPredefinedProject("empty-boot-project");
		createGeneralProject("general");
		BootLaunchUIModel model = new BootLaunchUIModel();
		assertError("No project selected", model.project.validator);

		model.project.selection.setValue(getProject("non-existant"));
		assertError("does not exist", model.project.validator);

		model.project.selection.setValue(getProject("general"));
		assertError("does not look like a Boot project", model.project.validator);

		model.project.selection.setValue(getProject("empty-boot-project"));
		assertOk(model.project.validator);

		getProject("empty-boot-project").close(new NullProgressMonitor());
		model.project.validator.refresh(); //manual refresh is needed
											// no auto refresh when closing project. This is normal
											// and it is okay since user can't open/close projects
											// while using launch config dialog.
		assertError("is closed", model.project.validator);
	}

	public void testProjectInitializeFrom() throws Exception {
		IProject fooProject = getProject("foo");

		BootLaunchUIModel model = new BootLaunchUIModel();
		SelectProjectLaunchTabModel project = model.project;
		LiveVariable<Boolean> dirtyState = model.project.getDirtyState();

		dirtyState.setValue(false);
		project.selection.setValue(fooProject);
		assertTrue(dirtyState.getValue());

		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		project.initializeFrom(wc);
		assertEquals(null, project.selection.getValue());
		assertFalse(dirtyState.getValue());

		BootLaunchConfigurationDelegate.setProject(wc, fooProject);
		project.initializeFrom(wc);
		assertEquals(fooProject, project.selection.getValue());
	}

	public void testProjectPerformApply() throws Exception {
		IProject fooProject = getProject("foo");

		BootLaunchUIModel model = new BootLaunchUIModel();
		SelectProjectLaunchTabModel project = model.project;
		LiveVariable<Boolean> dirtyState = model.project.getDirtyState();

		dirtyState.setValue(false);
		project.selection.setValue(fooProject);
		assertTrue(dirtyState.getValue());

		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		assertEquals(null, BootLaunchConfigurationDelegate.getProject(wc));

		project.performApply(wc);

		assertFalse(dirtyState.getValue());
		assertEquals(fooProject, BootLaunchConfigurationDelegate.getProject(wc));
	}

	public void testProjectSetDefaults() throws Exception {
		IProject fooProject = getProject("foo");

		BootLaunchUIModel model = new BootLaunchUIModel();
		SelectProjectLaunchTabModel project = model.project;

		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		BootLaunchConfigurationDelegate.setProject(wc, fooProject);
		assertEquals(fooProject, BootLaunchConfigurationDelegate.getProject(wc));
		project.setDefaults(wc);
		assertEquals(null, BootLaunchConfigurationDelegate.getProject(wc));
	}

	protected ILaunchConfigurationWorkingCopy createWorkingCopy()
			throws CoreException {
		String name = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName("test");
		ILaunchConfigurationWorkingCopy wc = DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(BootLaunchConfigurationDelegate.LAUNCH_CONFIG_TYPE_ID)
			.newInstance(null, name);
		return wc;
	}

	public void testMainTypeValidator() throws Exception {
		BootLaunchUIModel model = new BootLaunchUIModel();
		assertEquals("", model.mainTypeName.selection.getValue());
		assertError("No Main type selected", model.mainTypeName.validator);
		model.mainTypeName.selection.setValue("something");
		assertOk(model.mainTypeName.validator);
	}

	private void assertOk(LiveExpression<ValidationResult> validator) {
		assertTrue("Should be 'OK'", validator.getValue().isOk());
	}

	private IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	private void assertError(String snippet, LiveExpression<ValidationResult> validator) {
		ValidationResult value = validator.getValue();
		assertEquals(IStatus.ERROR, value.status);
		assertContains(snippet, value.msg);
	}

	public void assertContains(String needle, String haystack) {
		if (haystack==null || !haystack.contains(needle)) {
			fail("Not found: "+needle+"\n in \n"+haystack);
		}
 	}

}
