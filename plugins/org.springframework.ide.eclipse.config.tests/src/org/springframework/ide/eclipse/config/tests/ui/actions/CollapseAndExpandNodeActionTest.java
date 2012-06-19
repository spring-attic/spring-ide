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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.actions.CollapseNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.ExpandNodeAction;
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
public class CollapseAndExpandNodeActionTest extends AbstractConfigTestCase {

	public void testInnerNode() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		TreeItem innerNode = root.getItem(3);
		root.setExpanded(true);
		assertFalse(innerNode.getExpanded());

		ExpandNodeAction expand = new ExpandNodeAction(treeViewer, cEditor.getXmlProcessor());
		treeViewer.getTree().setSelection(innerNode);
		expand.run();
		assertTrue(innerNode.getExpanded());

		CollapseNodeAction collapse = new CollapseNodeAction(treeViewer, cEditor.getXmlProcessor());
		treeViewer.getTree().setSelection(innerNode);
		collapse.run();
		assertFalse(innerNode.getExpanded());
		assertTrue(root.getExpanded());
	}

	public void testRootNode() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		root.setExpanded(true);
		assertTrue(root.getExpanded());

		CollapseNodeAction collapse = new CollapseNodeAction(treeViewer, cEditor.getXmlProcessor());
		treeViewer.getTree().setSelection(root);
		collapse.run();
		assertFalse(root.getExpanded());

		ExpandNodeAction expand = new ExpandNodeAction(treeViewer, cEditor.getXmlProcessor());
		treeViewer.getTree().setSelection(root);
		expand.run();
		assertTrue(root.getExpanded());
	}

}
