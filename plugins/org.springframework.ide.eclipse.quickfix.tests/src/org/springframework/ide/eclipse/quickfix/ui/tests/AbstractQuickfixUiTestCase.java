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
package org.springframework.ide.eclipse.quickfix.ui.tests;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.runner.RunWith;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.StsConfigBot;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;
import org.springsource.ide.eclipse.commons.tests.util.swtbot.StsUiTestCase;


/**
 * @author Steffen Pingel
 * @author Terry Denney
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractQuickfixUiTestCase extends StsUiTestCase {

	protected boolean checkNoDuplicateQuickfixes(StsBotConfigEditor configEditor) {
		List<String> quickFixes = configEditor.getQuickFixes();
		Set<String> checkedQuickFixes = new HashSet<String>();

		for (String quickFix : quickFixes) {
			if (checkedQuickFixes.contains(quickFix)) {
				return true;
			}
			checkedQuickFixes.add(quickFix);
		}
		return false;
	}

	protected StsConfigBot getBot() {
		return (StsConfigBot) bot;
	}

	protected abstract IEditorPart openEditor() throws CoreException, IOException;

	protected IEditorPart openFileInEditor(String path) throws CoreException, IOException {
		BeansModel model = (BeansModel) BeansCorePlugin.getModel();
		// model.start();
		// model.stop();
		IProject project = StsTestUtil
				.createPredefinedProject("Test", "org.springframework.ide.eclipse.quickfix.tests");
		model.start();
		final IFile file = project.getFile(path);
		assertTrue(file.exists());
		IEditorPart editor = UIThreadRunnable.syncExec(new Result<IEditorPart>() {
			public IEditorPart run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				assertNotNull("Expected active workbench window", window);
				IWorkbenchPage page = window.getActivePage();
				assertNotNull("Expected active workbench page", page);
				IEditorPart editor;
				try {
					editor = IDE.openEditor(page, file);
				}
				catch (PartInitException e) {
					throw new RuntimeException(e);
				}
				return editor;
			}
		});

		// StsBotTestUtil.showView("Java", "Package Explorer", bot);
		//
		// bot.viewByTitle("Package Explorer").setFocus();
		// bot.tree().getTreeItem("Test").contextMenu("Properties").click();
		//
		// bot.shell("Properties for Test").activate();
		// bot.tree().expandNode("Spring").expandNode("Beans Support").select();

		// bot.button("Add...").click();

		// bot.shell("Spring Bean Configuration Selection").activate();
		// bot.tree().expandNode("src").expandNode(path.replace("src/",
		// "")).select();
		// bot.button("OK").click();

		// bot.shell("Properties for Test").activate();
		// bot.button("OK").click();

		try {
			SWTBotShell buildShell = bot.shell("Build workspace");
			if (buildShell != null) {
				bot.waitUntil(Conditions.shellCloses(buildShell));
			}
		}
		catch (WidgetNotFoundException e) {

		}

		return editor;
	}

	@Override
	protected void setUp() throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = new StsConfigBot();
		try {
			bot.viewByTitle("Welcome").close();
		}
		catch (WidgetNotFoundException e) {
			// ignore
		}

		// run in setUp() to enable super class to capture screenshot in
		// case of a failure
		cleanUp();
	}

}
