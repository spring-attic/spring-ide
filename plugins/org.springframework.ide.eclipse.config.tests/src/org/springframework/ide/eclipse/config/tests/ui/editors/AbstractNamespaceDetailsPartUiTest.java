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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.AopSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springsource.ide.eclipse.commons.tests.util.swtbot.SWTBotHyperlink;


/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class AbstractNamespaceDetailsPartUiTest extends AbstractConfigUiTestCase {

	public void testBeanAttributeLink() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigFormPage page = cEditor.getFormPageForUri(AopSchemaConstants.URI);
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load aop page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem aspectItem = root.getItem(0).getItem(1);
				page.setSelection(new StructuredSelection(aspectItem.getData()));
			}
		});
		bot.hyperlink(AopSchemaConstants.ATTR_REF.concat(":")).click();

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
				assertEquals(page, cEditor.getSelectedPage());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem[] items = treeViewer.getTree().getSelection();
				IDOMElement selection = (IDOMElement) items[0].getData();
				assertEquals(BeansSchemaConstants.ELEM_BEAN, selection.getLocalName());
			}
		});
	}

	public void testBeanIdContentProposalProvider() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load beans page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem beanItem = root.getItem(1);
				page.setSelection(new StructuredSelection(beanItem.getData()));
			}
		});

		SWTBotText text = bot.text("myConcreteClass");
		text.setText("");
		text.pressShortcut(SWT.CTRL, ' ');

		SWTBotShell shell = bot.activeShell();
		assertTrue(shell.isOpen()); // Weak test?
	}

	public void testBeanReferenceContentProposalProvider() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load aop page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem aspectItem = root.getItem(0).getItem(1);
				page.setSelection(new StructuredSelection(aspectItem.getData()));
			}
		});

		SWTBotText text = bot.text("propertyChangeTracker");
		text.setText("");
		text.pressShortcut(SWT.CTRL, ' ');

		SWTBotShell shell = bot.activeShell();
		assertTrue(shell.isOpen()); // Weak test?
	}

	public void testClassAttributeButton() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load beans page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem beanItem = root.getItem(1);
				page.setSelection(new StructuredSelection(beanItem.getData()));
			}
		});
		bot.flatButton("Browse...").click();

		SWTBotShell typeDialog = bot.shell("Select Type");
		assertTrue(typeDialog.isOpen());
		typeDialog.close();
	}

	public void testClassAttributeLink() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load beans page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem beanItem = root.getItem(1);
				page.setSelection(new StructuredSelection(beanItem.getData()));
			}
		});

		SWTBotHyperlink link = bot.hyperlink(BeansSchemaConstants.ATTR_CLASS.concat(":"));
		link.click();

		SWTBotEditor editor = bot.editorByTitle("MyConcreteClass.java");
		assertTrue(editor.isActive());
		editor.close();

		bot.cTabItem("beans").activate().show();
		bot.text("com.test.MyConcreteClass").setText("");
		link.click();

		SWTBotShell classDialog = bot.shell("New Java Class");
		assertTrue(classDialog.isOpen());
		classDialog.close();
	}

	public void testClassContentProposalProvider() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load beans page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem beanItem = root.getItem(1);
				page.setSelection(new StructuredSelection(beanItem.getData()));
			}
		});

		SWTBotText text = bot.text("com.test.MyConcreteClass");
		text.setText("com");
		text.pressShortcut(SWT.CTRL, ' ');

		SWTBotShell shell = bot.activeShell();
		assertTrue(shell.isOpen()); // Weak test?
	}

	public void testPointCutAttributeLink() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load aop page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem item = root.getItem(0).getItem(1).getItem(0);
				page.setSelection(new StructuredSelection(item.getData()));
			}
		});
		bot.hyperlink(AopSchemaConstants.ATTR_POINTCUT_REF.concat(":")).click();

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem[] items = treeViewer.getTree().getSelection();
				IDOMElement selection = (IDOMElement) items[0].getData();
				assertEquals(AopSchemaConstants.ELEM_POINTCUT, selection.getLocalName());
			}
		});
	}

	public void testPointcutReferenceContentProposalProvider() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load aop page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem item = root.getItem(0).getItem(1).getItem(0);
				page.setSelection(new StructuredSelection(item.getData()));
			}
		});

		SWTBotText text = bot.text("setterMethod");
		text.setText("");
		text.pressShortcut(SWT.CTRL, ' ');

		SWTBotShell shell = bot.activeShell();
		assertTrue(shell.isOpen()); // Weak test?
	}

	public void testStepAttributeLink() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BatchSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load batch page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem item = root.getItem(0).getItem(0);
				IDOMElement selection = (IDOMElement) item.getData();
				page.setSelection(new StructuredSelection(selection));
				assertEquals("step1", selection.getAttribute(BatchSchemaConstants.ATTR_ID));
			}
		});
		bot.hyperlink(BatchSchemaConstants.ATTR_NEXT.concat(":")).click();

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				TreeItem[] items = treeViewer.getTree().getSelection();
				IDOMElement selection = (IDOMElement) items[0].getData();
				assertEquals(BatchSchemaConstants.ELEM_STEP, selection.getLocalName());
				assertEquals("step2", selection.getAttribute(BatchSchemaConstants.ATTR_ID));
			}
		});
	}

	public void testStepReferenceContentProposalProvider() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");

		final AbstractConfigFormPage page = cEditor.getFormPageForUri(BatchSchemaConstants.URI);
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				cEditor.setActivePage(page.getId());
				assertNotNull("Could not load batch page.", page.getMasterPart());

				TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
				treeViewer.expandAll();
				TreeItem root = treeViewer.getTree().getItem(0);
				TreeItem item = root.getItem(0).getItem(0);
				page.setSelection(new StructuredSelection(item.getData()));
			}
		});

		SWTBotText text = bot.text("step2");
		text.setText("");
		text.pressShortcut(SWT.CTRL, ' ');

		SWTBotShell shell = bot.activeShell();
		assertTrue(shell.isOpen()); // Weak test?

		cleanUp();
	}
}
