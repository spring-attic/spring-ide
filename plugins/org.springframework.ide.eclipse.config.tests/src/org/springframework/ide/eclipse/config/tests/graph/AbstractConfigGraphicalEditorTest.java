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
package org.springframework.ide.eclipse.config.tests.graph;

import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.UtilSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;

/**
 * @author Leo Dos Santos
 * @author Tomasz Zarna
 */
public class AbstractConfigGraphicalEditorTest extends AbstractConfigTestCase {

	public void testBatchFile() throws Exception {
		enableGefPages(true);
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigGraphicalEditor batch = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNotNull(batch);
		assertEquals(BatchSchemaConstants.URI, batch.getNamespaceUri());

		AbstractConfigGraphicalEditor beans = cEditor.getGraphicalEditorForUri(BeansSchemaConstants.URI);
		assertNull(beans);

		AbstractConfigGraphicalEditor util = cEditor.getGraphicalEditorForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testBeansFile() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigGraphicalEditor batch = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNull(batch);

		AbstractConfigGraphicalEditor beans = cEditor.getGraphicalEditorForUri(BeansSchemaConstants.URI);
		assertNull(beans);

		AbstractConfigGraphicalEditor util = cEditor.getGraphicalEditorForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testIntegrationFile() throws Exception {
		enableGefPages(true);
		cEditor = openFileInEditor("src/integration-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigGraphicalEditor batch = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNull(batch);

		AbstractConfigGraphicalEditor beans = cEditor.getGraphicalEditorForUri(BeansSchemaConstants.URI);
		assertNull(beans);

		AbstractConfigGraphicalEditor integration = cEditor.getGraphicalEditorForUri(IntegrationSchemaConstants.URI);
		assertNotNull(integration);
		assertEquals(IntegrationSchemaConstants.URI, integration.getNamespaceUri());

		AbstractConfigGraphicalEditor util = cEditor.getGraphicalEditorForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

	public void testScopedFile() throws Exception {
		enableGefPages(true);
		cEditor = openFileInEditor("src/scoped-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigGraphicalEditor batch = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
		assertNotNull(batch);
		assertEquals(BatchSchemaConstants.URI, batch.getNamespaceUri());

		AbstractConfigGraphicalEditor beans = cEditor.getGraphicalEditorForUri(BeansSchemaConstants.URI);
		assertNull(beans);

		AbstractConfigGraphicalEditor util = cEditor.getGraphicalEditorForUri(UtilSchemaConstants.URI);
		assertNull(util);
	}

}
