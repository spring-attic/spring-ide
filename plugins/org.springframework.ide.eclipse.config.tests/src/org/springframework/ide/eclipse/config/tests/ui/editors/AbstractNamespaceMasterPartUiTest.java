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
package org.springframework.ide.eclipse.config.tests.ui.editors;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Leo Dos Santos
 */
public class AbstractNamespaceMasterPartUiTest extends AbstractConfigUiTestCase {

	public void testCreateButtons() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);
		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		Thread.sleep(StsTestUtil.WAIT_TIME);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load beans page.", page.getMasterPart());
			}
		});

		SWTBotButton newBeanButton = bot.flatButton("New Bean...");
		SWTBotButton upButton = bot.flatButton("Up");
		SWTBotButton downButton = bot.flatButton("Down");
		assertFalse(upButton.isEnabled());
		assertFalse(downButton.isEnabled());

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem beanItem = root.getItem(1);
				page.setSelection(new StructuredSelection(beanItem.getData()));
			}
		});

		assertTrue(upButton.isEnabled());
		assertTrue(downButton.isEnabled());
		newBeanButton.click();

		SWTBotShell newBeanDialog = bot.shell("Create New Bean");
		assertTrue(newBeanDialog.isOpen());
		newBeanDialog.close();
	}

}
