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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.actions.LowerNodeAction;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.namespaces.NamespacesFormPage;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Leo Dos Santos
 * @author Tomasz Zarna
 */
@SuppressWarnings("restriction")
public class SpringConfigEditorTest extends AbstractConfigTestCase {

	public void testActivePagePreference() throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		IEditorInput input = cEditor.getEditorInput();
		cEditor.setActiveEditor(cEditor.getSourcePage());
		page.closeEditor(cEditor, false);

		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		assertEquals(cEditor.getSourcePage(), cEditor.getActiveEditor());
		cEditor.setActivePage(cEditor.getFormPageForUri(BatchSchemaConstants.URI).getId());
		page.closeEditor(cEditor, false);

		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		assertEquals(cEditor.getFormPageForUri(BatchSchemaConstants.URI), cEditor.getActivePageInstance());
		cEditor.setActiveEditor(cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI));
		page.closeEditor(cEditor, false);

		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		assertEquals(cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI), cEditor.getActiveEditor());
		cEditor.setActivePage(OverviewFormPage.ID);
		page.closeEditor(cEditor, false);

		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		assertEquals(cEditor.getFormPage(OverviewFormPage.ID), cEditor.getActivePageInstance());
		cEditor.setActivePage(NamespacesFormPage.ID);
		page.closeEditor(cEditor, false);

		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		assertEquals(cEditor.getFormPage(NamespacesFormPage.ID), cEditor.getActivePageInstance());
	}

	public void testGraphicalPagesPreference() throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		enableGefPages(true);
		IEditorInput input = cEditor.getEditorInput();
		AbstractConfigGraphicalEditor gEditor = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNotNull(gEditor);
		page.closeEditor(cEditor, false);

		enableGefPages(false);
		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		gEditor = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNull(gEditor);
		page.closeEditor(cEditor, false);

		enableGefPages(true);
		cEditor = (AbstractConfigEditor) page.openEditor(input, SpringConfigEditor.ID_EDITOR);
		gEditor = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNotNull(gEditor);

		enableGefPages(false);
		gEditor = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNull(gEditor);
	}

	public void testModelLeaksEditAndSave() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		// the config editor holds onto one model reference for edit and its
		// source page holds onto another reference
		IDOMModel model = cEditor.getDomDocument().getModel();
		int references = model.getReferenceCountForEdit();
		assertEquals(2, references);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		StructuredTextViewer textView = cEditor.getTextViewer();
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);

		LowerNodeAction lower = new LowerNodeAction(treeViewer, cEditor.getXmlProcessor(), textView);
		treeViewer.getTree().setSelection(root.getItem(0));
		lower.run();
		assertEquals(references, model.getReferenceCountForEdit());

		treeViewer.getTree().setSelection(root.getItem(0));
		lower.run();
		assertEquals(references, model.getReferenceCountForEdit());

		cEditor.doSave(new NullProgressMonitor());
		StsTestUtil.waitForEditor(cEditor);
		assertEquals(references, model.getReferenceCountForEdit());

		// calling cEditor.close(false) here will fail the following assertion
		// due to an asynchronous call in that method
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(cEditor, false);
		StsTestUtil.waitForDisplay();
		assertEquals(0, model.getReferenceCountForEdit());

		// wait for background threads to free the model
		for (int i = 0; i < 3 && model.getReferenceCount() > 0; i++) {
			Thread.sleep(1000);
		}
		assertEquals(0, model.getReferenceCount());
	}

	public void testModelLeaksVisitSeveralPages() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		// config editor holds onto one model reference for edit and its source
		// page holds onto another reference
		final IDOMModel model = cEditor.getDomDocument().getModel();
		int references = model.getReferenceCountForEdit();
		assertEquals(2, references);

		cEditor.setActiveEditor(cEditor.getFormPageForUri(BatchSchemaConstants.URI));
		assertEquals(references, model.getReferenceCountForEdit());
		cEditor.setActiveEditor(cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI));
		assertEquals(references, model.getReferenceCountForEdit());
		cEditor.setActiveEditor(cEditor.getFormPageForUri(BeansSchemaConstants.URI));
		assertEquals(references, model.getReferenceCountForEdit());
		cEditor.setActiveEditor(cEditor.getSourcePage());
		assertEquals(references, model.getReferenceCountForEdit());

		// calling cEditor.close(false) here will fail the following assertion
		// due to an asynchronous call in that method
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(cEditor, false);
		StsTestUtil.waitForDisplay();
		assertEquals(0, model.getReferenceCountForEdit());

		// wait for background threads to free the model
		for (int i = 0; i < 3 && model.getReferenceCount() > 0; i++) {
			Thread.sleep(1000);
		}
		assertEquals(0, model.getReferenceCount());
	}

}
