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
package org.springframework.ide.eclipse.config.tests.ui.actions;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.actions.DeleteNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.InsertNodeAction;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * These tests call Thread.sleep() to get around a bug in
 * {@link SpringConfigContentAssistProcessor#getChildNames(IDOMElement)} but may
 * still fail randomly.}
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class InsertAndDeleteNodeActionTest extends AbstractConfigTestCase {

	public void testEmptyFile() throws Exception {
		cEditor = openFileInEditor("src/empty-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals(0, root.getItemCount());

		InsertNodeAction insert = new InsertNodeAction(treeViewer, cEditor.getXmlProcessor(), textView, "bean");
		treeViewer.getTree().setSelection(root);
		insert.run();
		assertEquals(1, root.getItemCount());

		IDOMElement node = (IDOMElement) root.getItem(0).getData();
		DeleteNodeAction delete = new DeleteNodeAction(textView, node);
		delete.run();
		assertEquals(0, root.getItemCount());
	}

	public void testLargeFileDeletion() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getItem(0).getData();
		assertEquals(20, root.getItemCount());

		DeleteNodeAction action = new DeleteNodeAction(textView, node);
		action.run();
		assertEquals(19, root.getItemCount());
	}

	public void testLargeFileInsertion() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals(20, root.getItemCount());

		InsertNodeAction action = new InsertNodeAction(treeViewer, cEditor.getXmlProcessor(), textView, "bean");
		treeViewer.getTree().setSelection(root);
		action.run();
		assertEquals(21, root.getItemCount());
	}
}
