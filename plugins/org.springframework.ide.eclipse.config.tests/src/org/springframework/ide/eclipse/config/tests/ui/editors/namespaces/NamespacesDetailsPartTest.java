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
package org.springframework.ide.eclipse.config.tests.ui.editors.namespaces;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IDetailsPage;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterDetailsBlock;
import org.springframework.ide.eclipse.config.ui.editors.namespaces.NamespacesDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.namespaces.NamespacesFormPage;
import org.springframework.ide.eclipse.config.ui.editors.namespaces.NamespacesMasterPart;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NamespacesDetailsPartTest extends AbstractConfigTestCase {

	public void testViewerEnablement() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(NamespacesFormPage.ID);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load namespaces page.", page.getMasterPart());

		CountDownLatch latch = ((NamespacesMasterPart) page.getMasterPart()).getLazyInitializationLatch();
		assertTrue("Table initialization did not complete before timeout.", latch.await(30, TimeUnit.SECONDS));
		StsTestUtil.waitForDisplay();

		AbstractConfigMasterDetailsBlock block = page.getMasterDetailsBlock();
		CheckboxTableViewer checkViewer = (CheckboxTableViewer) page.getMasterPart().getViewer();
		assertTrue(checkViewer.getTable().getItemCount() > 0);

		for (TableItem item : checkViewer.getTable().getItems()) {
			page.setSelection(new StructuredSelection(item.getData()));
			IDetailsPage details = block.getDetailsPart().getCurrentPage();
			assertTrue("Could not load details part.", details instanceof NamespacesDetailsPart);

			NamespacesDetailsPart detailsPart = (NamespacesDetailsPart) details;
			ColumnViewer versionViewer = detailsPart.getVersionViewer();
			assertEquals(item.getChecked(), versionViewer.getControl().getEnabled());
		}
	}

}
