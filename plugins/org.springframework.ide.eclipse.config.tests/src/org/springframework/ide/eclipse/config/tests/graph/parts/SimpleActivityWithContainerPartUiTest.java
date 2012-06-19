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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.parts.SimpleActivityWithContainerPart;
import org.springframework.ide.eclipse.config.graph.parts.StructuredActivityPart;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.gef.EditPartMatcherFactory;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;


/**
 * @author Leo Dos Santos
 */
public class SimpleActivityWithContainerPartUiTest extends AbstractConfigUiTestCase {

	@SuppressWarnings("unchecked")
	public void testDeleteActivityPart() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigGraphicalEditor page = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
				assertNotNull("Could not load batch-graph page.", page);
				cEditor.setActiveEditor(page);
			}
		});

		Matcher matcher = AllOf.allOf(EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("limitDecision"));

		StsBotConfigEditor editor = getBot().activeConfigEditor();
		StsBotGefEditor gEditor = editor.toGefEditorFromUri(BatchSchemaConstants.URI);
		List<SWTBotGefEditPart> parts = gEditor.editParts(matcher);
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart activityPart = parts.get(0);
		activityPart.select();
		gEditor.pressShortcut(KeyStroke.getInstance(SWT.DEL));

		for (int i = 0; i < 3 && gEditor.editParts(matcher).size() > 0; i++) {
			Thread.sleep(1000);
		}
		parts = gEditor.editParts(matcher);
		assertEquals(Collections.emptyList(), parts);
	}

	@SuppressWarnings("unchecked")
	public void testDeleteContainerPart() throws Exception {
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
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart activityPart = parts.get(0);
		SWTBotGefConnectionEditPart transitionPart = activityPart.sourceConnections().get(0);
		SWTBotGefEditPart containerPart = transitionPart.target();
		containerPart.select();
		gEditor.pressShortcut(KeyStroke.getInstance(SWT.DEL));

		parts = gEditor.editParts(AllOf.allOf(
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertEquals(Collections.emptyList(), parts);
	}

	@SuppressWarnings("unchecked")
	public void testDeleteTransitionPart() throws Exception {
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
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart activityPart = parts.get(0);
		SWTBotGefConnectionEditPart transitionPart = activityPart.sourceConnections().get(0);
		transitionPart.select();
		gEditor.pressShortcut(KeyStroke.getInstance(SWT.DEL));

		parts = gEditor.editParts(AllOf.allOf(
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("limitDecision")));
		assertEquals(Collections.emptyList(), parts);
	}

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
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertFalse(parts.isEmpty());
		SWTBotGefEditPart activityPart = parts.get(0);

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertEquals(Collections.emptyList(), parts);

		gEditor.activateTool(BatchSchemaConstants.ELEM_STEP);
		activityPart.click();

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertEquals(Collections.emptyList(), parts);
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
				EditPartMatcherFactory.editPartOfType(SimpleActivityWithContainerPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertFalse(parts.isEmpty());
		SWTBotGefEditPart activityPart = parts.get(0);

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertEquals(Collections.emptyList(), parts);

		gEditor.activateTool(BatchSchemaConstants.ELEM_NEXT);
		activityPart.click();

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart containerPart = parts.get(0);
		assertFalse(containerPart.children().isEmpty());
	}
}
