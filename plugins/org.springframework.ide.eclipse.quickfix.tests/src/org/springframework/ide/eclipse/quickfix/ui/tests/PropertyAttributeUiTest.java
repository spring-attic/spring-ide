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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Terry Denney
 * @author Steffen Pingel
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PropertyAttributeUiTest extends AbstractQuickfixUiTestCase {

	@Override
	protected IEditorPart openEditor() throws CoreException, IOException {
		return openFileInEditor("src/rename-proposal.xml");
	}

	@Test
	public void testPropertyQuickfix() throws CoreException, IOException {
		IEditorPart editor = openEditor();

		assertNotNull("Expects editor to open", editor);
		assertTrue("Expects spring config editor", editor instanceof IConfigEditor);

		StsBotConfigEditor configEditor = getBot().activeConfigEditor();
		configEditor.navigateTo(21, 24);

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.waitForEditor(editor);
		int quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects quick fixes", quickfixListItemCount > 1);
		assertFalse("Expects no duplicate quick fixes", checkNoDuplicateQuickfixes(configEditor));
		configEditor.getStyledText().setFocus();

		configEditor.quickfix("Change to balance");
		configEditor.getStyledText().setFocus();

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.saveAndWaitForEditor(editor);
		quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects no quick fix", quickfixListItemCount == 1);
		configEditor.getStyledText().setFocus();

		assertEquals("Expects quickfix to change property name", "		<property  name=\"balance\"/>",
				configEditor.getTextOnCurrentLine());
	}
}
