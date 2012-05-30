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
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.actions.LowerNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.RaiseNodeAction;
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
public class RaiseAndLowerNodeActionTest extends AbstractConfigTestCase {

	public void testLowerBeforeRaise() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		RaiseNodeAction raise = new RaiseNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(19));
		raise.run();
		assertEquals("bean20", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean19", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		LowerNodeAction lower = new LowerNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(18));
		lower.run();
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
	}

	public void testRaiseBeforeLower() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		LowerNodeAction lower = new LowerNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(18));
		lower.run();
		assertEquals("bean20", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean19", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		RaiseNodeAction raise = new RaiseNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(19));
		raise.run();
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
	}

	public void testRevertingLowerActions() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		LowerNodeAction lower = new LowerNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(18));
		lower.run();
		assertEquals("bean20", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean19", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		treeViewer.getTree().setSelection(root.getItem(18));
		lower.run();
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
	}

	public void testRevertingRaiseActions() throws Exception {
		cEditor = openFileInEditor("src/many-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		RaiseNodeAction raise = new RaiseNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(19));
		raise.run();
		assertEquals("bean20", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean19", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));

		treeViewer.getTree().setSelection(root.getItem(19));
		raise.run();
		assertEquals("bean19", ((IDOMElement) root.getItem(18).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
		assertEquals("bean20", ((IDOMElement) root.getItem(19).getData()).getAttribute(BeansSchemaConstants.ATTR_ID));
	}

}
