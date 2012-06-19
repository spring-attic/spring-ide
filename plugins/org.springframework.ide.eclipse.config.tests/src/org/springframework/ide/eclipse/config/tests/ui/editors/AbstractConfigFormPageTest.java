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

import org.springframework.ide.eclipse.config.core.schemas.AopSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntStreamSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.UtilSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class AbstractConfigFormPageTest extends AbstractConfigTestCase {

	public void testAopFile() throws Exception {
		cEditor = openFileInEditor("src/aop-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage overview = cEditor.getFormPage(OverviewFormPage.ID);
		assertEquals(OverviewFormPage.ID, overview.getId());
		assertNull(overview.getNamespaceUri());
		assertEquals(null, overview.getPrefixForNamespaceUri());

		AbstractConfigFormPage aop = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.aop", aop.getId());
		assertEquals(AopSchemaConstants.URI, aop.getNamespaceUri());
		assertEquals("", aop.getPrefixForNamespaceUri());

		AbstractConfigFormPage beans = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.beans", beans.getId());
		assertEquals(BeansSchemaConstants.URI, beans.getNamespaceUri());
		assertEquals("beans", beans.getPrefixForNamespaceUri());

		AbstractConfigFormPage util = cEditor.getFormPageForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testBeansFile() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage overview = cEditor.getFormPage(OverviewFormPage.ID);
		assertEquals(OverviewFormPage.ID, overview.getId());
		assertNull(overview.getNamespaceUri());
		assertEquals(null, overview.getPrefixForNamespaceUri());

		AbstractConfigFormPage aop = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.aop", aop.getId());
		assertEquals(AopSchemaConstants.URI, aop.getNamespaceUri());
		assertEquals("aop", aop.getPrefixForNamespaceUri());

		AbstractConfigFormPage beans = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.beans", beans.getId());
		assertEquals(BeansSchemaConstants.URI, beans.getNamespaceUri());
		assertEquals("", beans.getPrefixForNamespaceUri());

		AbstractConfigFormPage util = cEditor.getFormPageForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testIntegrationFile() throws Exception {
		cEditor = openFileInEditor("src/integration-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage overview = cEditor.getFormPage(OverviewFormPage.ID);
		assertEquals(OverviewFormPage.ID, overview.getId());
		assertNull(overview.getNamespaceUri());
		assertEquals(null, overview.getPrefixForNamespaceUri());

		AbstractConfigFormPage aop = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		assertNull(aop);

		AbstractConfigFormPage beans = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.beans", beans.getId());
		assertEquals(BeansSchemaConstants.URI, beans.getNamespaceUri());
		assertEquals("beans", beans.getPrefixForNamespaceUri());

		AbstractConfigFormPage integration = cEditor.getFormPageForUri(IntegrationSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.integration", integration.getId());
		assertEquals(IntegrationSchemaConstants.URI, integration.getNamespaceUri());
		assertEquals("", integration.getPrefixForNamespaceUri());

		AbstractConfigFormPage stream = cEditor.getFormPage(IntStreamSchemaConstants.URI);
		assertNull(stream);

		AbstractConfigFormPage util = cEditor.getFormPageForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testScopedFile() throws Exception {
		cEditor = openFileInEditor("src/scoped-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage overview = cEditor.getFormPage(OverviewFormPage.ID);
		assertEquals(OverviewFormPage.ID, overview.getId());
		assertNull(overview.getNamespaceUri());
		assertEquals(null, overview.getPrefixForNamespaceUri());

		AbstractConfigFormPage aop = cEditor.getFormPageForUri(AopSchemaConstants.URI);
		assertNull(aop);

		AbstractConfigFormPage batch = cEditor.getFormPageForUri(BatchSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.batch", batch.getId());
		assertEquals(BatchSchemaConstants.URI, batch.getNamespaceUri());
		assertEquals(null, batch.getPrefixForNamespaceUri());

		AbstractConfigFormPage beans = cEditor.getFormPageForUri(BeansSchemaConstants.URI);
		assertEquals("com.springsource.sts.config.ui.editors.beans", beans.getId());
		assertEquals(BeansSchemaConstants.URI, beans.getNamespaceUri());
		assertEquals("", beans.getPrefixForNamespaceUri());

		AbstractConfigFormPage util = cEditor.getFormPageForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

}
