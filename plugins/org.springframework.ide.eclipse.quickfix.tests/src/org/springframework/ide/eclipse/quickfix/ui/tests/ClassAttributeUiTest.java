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
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Terry Denney
 * @author Steffen Pingel
 */
public class ClassAttributeUiTest extends AbstractQuickfixUiTestCase {

	@Override
	protected IEditorPart openEditor() throws CoreException, IOException {
		return openFileInEditor("src/class-attribute.xml");
	}

	@Test
	public void testClassAttributeQuickfix() throws CoreException, IOException {
		IEditorPart editor = openEditor();

		assertNotNull("Expects editor to open", editor);
		assertTrue("Expects spring config editor", editor instanceof IConfigEditor);
		StsBotConfigEditor configEditor = getBot().activeConfigEditor();
		configEditor.navigateTo(8, 37);

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.waitForEditor(editor);
		int quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects quick fixes", quickfixListItemCount > 1);
		configEditor.getStyledText().setFocus();

		configEditor.quickfix("Change to Account (com.test)");
		configEditor.getStyledText().setFocus();

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.saveAndWaitForEditor(editor);
		quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects no quick fix", quickfixListItemCount == 1);
		configEditor.getStyledText().setFocus();

		assertEquals("Expects quickfix to change class name", "	<bean id=\"classTest2\" class=\"com.test.Account\"/>",
				configEditor.getTextOnCurrentLine());
	}

	@Test
	public void testCreateClassQuickfix() throws CoreException, IOException {
		IEditorPart editor = openEditor();

		assertNotNull("Expects editor to open", editor);
		assertTrue("Expects spring config editor", editor instanceof IConfigEditor);
		StsBotConfigEditor configEditor = getBot().activeConfigEditor();
		configEditor.navigateTo(21, 34);

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.waitForEditor(editor);
		int quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects quick fixes", quickfixListItemCount > 1);
		assertFalse("Expects no duplicate quick fixes", checkNoDuplicateQuickfixes(configEditor));
		configEditor.getStyledText().setFocus();

		configEditor.quickfix("Create class 'Account'");
		bot.shell("New Class").activate();

		SWTBotText text = bot.text(1);
		assertNotNull(text);
		String str = text.getText();
		assertNotNull(str);
		assertEquals("com.test.ui", str);

		bot.button("Finish").click();

		// bot.sleep(StsTestUtil.WAIT_TIME);
		StsTestUtil.saveAndWaitForEditor(editor);
		configEditor.getStyledText().setFocus();

		quickfixListItemCount = configEditor.getQuickfixListItemCount();
		assertTrue("Expects no quick fix", quickfixListItemCount == 1);

		SWTBotEditor javaEditor = bot.editorByTitle("Account.java");
		assertNotNull(javaEditor);
		javaEditor.close();
	}

}
