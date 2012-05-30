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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.hamcrest.core.AllOf;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.parts.SequentialActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.StructuredActivityPart;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.gef.EditPartMatcherFactory;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;


/**
 * @author Leo Dos Santos
 */
public class StructuredActivityPartUiTest extends AbstractConfigUiTestCase {

	@SuppressWarnings("unchecked")
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
		List<SWTBotGefEditPart> parts = gEditor.editParts(AllOf.allOf(
				EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart containerPart = parts.get(0);
		assertEquals(2, containerPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_STEP);
		containerPart.click();

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		containerPart = parts.get(0);
		assertEquals(2, containerPart.children().size());
	}

	@SuppressWarnings("unchecked")
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
		List<SWTBotGefEditPart> parts = gEditor.editParts(AllOf.allOf(
				EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart containerPart = parts.get(0);
		assertEquals(2, containerPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_NEXT);
		containerPart.click();

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		containerPart = parts.get(0);
		assertEquals(3, containerPart.children().size());
	}

	public void testPerformOpen() throws Exception {
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
				.editPartOfType(SequentialActivityPart.class));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart editPart = parts.get(0);
		editPart.doubleClick();

		SWTBotView view = bot.viewById("org.eclipse.ui.views.PropertySheet");
		assertTrue(view.isActive());
		view.close();
	}

}
