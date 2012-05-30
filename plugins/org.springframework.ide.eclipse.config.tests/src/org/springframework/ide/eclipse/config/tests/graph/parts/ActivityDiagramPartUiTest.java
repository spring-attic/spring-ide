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
package org.springframework.ide.eclipse.config.tests.graph.parts;

import java.util.List;

import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.gef.EditPartMatcherFactory;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;


/**
 * @author Leo Dos Santos
 */
public class ActivityDiagramPartUiTest extends AbstractConfigUiTestCase {

	public void testDropInvalidPart() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigGraphicalEditor page = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
				assertNotNull("Could not load batch-graph page.", page);
				cEditor.setActiveEditor(page);
			}
		});

		StsBotConfigEditor editor = getBot().activeConfigEditor();
		StsBotGefEditor gEditor = editor.toGefEditorFromUri(BatchSchemaConstants.URI);
		List<SWTBotGefEditPart> parts = gEditor.editParts(EditPartMatcherFactory
				.editPartOfType(ActivityDiagramPart.class));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_SPLIT);
		diagramPart.click();

		gEditor.editParts(EditPartMatcherFactory.editPartOfType(ActivityDiagramPart.class));
		diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

	}

	public void testDropValidPart() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigGraphicalEditor page = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
				assertNotNull("Could not load batch-graph page.", page);
				cEditor.setActiveEditor(page);
			}
		});

		StsBotConfigEditor editor = getBot().activeConfigEditor();
		StsBotGefEditor gEditor = editor.toGefEditorFromUri(BatchSchemaConstants.URI);
		List<SWTBotGefEditPart> parts = gEditor.editParts(EditPartMatcherFactory
				.editPartOfType(ActivityDiagramPart.class));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_JOB);
		diagramPart.click();

		gEditor.editParts(EditPartMatcherFactory.editPartOfType(ActivityDiagramPart.class));
		diagramPart = parts.get(0);
		assertEquals(3, diagramPart.children().size());
	}

}
