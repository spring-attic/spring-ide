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
import org.eclipse.ui.forms.IDetailsPage;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterDetailsBlock;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigInputAccessor;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Leo Dos Santos
 */
public class SpringConfigInputAccessorTest extends AbstractConfigTestCase {

	public void testEditAttribute() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load beans page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		TreeItem subItem = root.getItem(1);
		page.setSelection(new StructuredSelection(subItem.getData()));

		AbstractConfigMasterDetailsBlock block = page.getMasterDetailsBlock();
		IDetailsPage details = block.getDetailsPart().getCurrentPage();
		assertTrue("Could not load details part.", details instanceof AbstractNamespaceDetailsPart);

		AbstractNamespaceDetailsPart detailsPart = (AbstractNamespaceDetailsPart) details;
		SpringConfigInputAccessor accessor = new SpringConfigInputAccessor(cEditor, detailsPart.getInput());

		assertEquals("myConcreteClass", accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));
		assertEquals(detailsPart.getInput().getAttribute(BeansSchemaConstants.ATTR_ID),
				accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));

		accessor.editAttribute(BeansSchemaConstants.ATTR_ID, "foo");
		assertEquals("foo", accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));
		assertEquals(detailsPart.getInput().getAttribute(BeansSchemaConstants.ATTR_ID),
				accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));

		accessor.editAttribute(BeansSchemaConstants.ATTR_ID, "");
		assertEquals("", accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));
		assertNull(detailsPart.getInput().getAttributeNode(BeansSchemaConstants.ATTR_ID));

		accessor.editAttribute(BeansSchemaConstants.ATTR_ID, "myConcreteClass");
		assertEquals("myConcreteClass", accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));
		assertEquals(detailsPart.getInput().getAttribute(BeansSchemaConstants.ATTR_ID),
				accessor.getAttributeValue(BeansSchemaConstants.ATTR_ID));
	}

	public void testEditElement() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load beans page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		TreeItem subItem = root.getItem(0);
		page.setSelection(new StructuredSelection(subItem.getData()));

		AbstractConfigMasterDetailsBlock block = page.getMasterDetailsBlock();
		IDetailsPage details = block.getDetailsPart().getCurrentPage();
		assertTrue("Could not load details part.", details instanceof AbstractNamespaceDetailsPart);

		AbstractNamespaceDetailsPart detailsPart = (AbstractNamespaceDetailsPart) details;
		SpringConfigInputAccessor accessor = new SpringConfigInputAccessor(cEditor, detailsPart.getInput());

		assertEquals("A sample configuration file.", accessor.getElementValue());

		accessor.editElement("foo");
		assertEquals("foo", accessor.getElementValue());

		accessor.editElement("");
		assertEquals("", accessor.getElementValue());

		accessor.editElement("A sample configuration file.");
		assertEquals("A sample configuration file.", accessor.getElementValue());
	}

}
