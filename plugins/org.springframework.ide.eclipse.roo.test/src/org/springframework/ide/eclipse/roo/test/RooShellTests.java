/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.test;

import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.getResource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;


/**
 * @author Kris De Volder
 */
public class RooShellTests extends RooUITestCase {
	
	private String[] testProjectNames = {
			"kroos", "nierooj", "androow"
	};
	
	@Override
	public void setupClass() throws Exception {
		super.setupClass();
		for (String name : testProjectNames) {
			createRooProject(name, "com."+name+".toplevel");
		}
	}
	
	public void testScaffolding() {
		assertEquals(1, 1);
		for (String projName : testProjectNames) {
			assertTrue(getResource(projName).exists());
		}
		
		SWTBotView rooShell = getView("Roo Shell");
		assertTrue(rooShell.isActive());

		for (String projectName : testProjectNames) {
			SWTBotCTabItem rooShellTab = getRooShellTab(rooShell, projectName);
			assertEquals(projectName, rooShellTab.getText());
		}
	}
	
	public void testShellClosesWhenProjectCloses() throws CoreException {
		SWTBotView rooShell = getView("Roo Shell");
		assertTrue(rooShell.isActive());
		
		SWTBotCTabItem shellTab = getRooShellTab(rooShell, "androow");
		IProject project = getProject("androow");
		project.close(null);
		assertFalse(project.isOpen());
		bot.waitUntil(SWTBotUtils.widgetIsDisposed(shellTab));
	}

	public void testShellClosesWhenProjectDeleted() throws CoreException {
		SWTBotView rooShell = getView("Roo Shell");
		assertTrue(rooShell.isActive());
		
		SWTBotCTabItem shellTab = getRooShellTab(rooShell, "nierooj");
		IProject project = getProject("nierooj");
		project.delete(true, true, null);
		assertFalse(project.isOpen());
		bot.waitUntil(SWTBotUtils.widgetIsDisposed(shellTab));
		assertFalse(project.exists());
	}
	
	/**
	 * Get a rooShell tab for a given projectName. This will only succeed if
	 * there already is an open roo shell tab for this project.
	 */
	private SWTBotCTabItem getRooShellTab(SWTBotView rooShell, String projectName) {
		SWTBotCTabItem result = rooShell.bot().cTabItem(projectName);
		assertEquals(projectName, result.getText());
		return result;
	}

	/**
	 * Get a project by a given name in the workspace, verify that the project exists
	 * before returning it.
	 */
	public static IProject getProject(String name) {
		IProject project = (IProject) getResource(name);
		assertTrue(project.exists());
		return project;
	}
}
