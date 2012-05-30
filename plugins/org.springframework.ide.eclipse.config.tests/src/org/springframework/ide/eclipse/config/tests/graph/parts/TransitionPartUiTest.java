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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.hamcrest.core.AllOf;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.parts.SimpleActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.StructuredActivityPart;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.gef.EditPartMatcherFactory;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;


/**
 * @author Leo Dos Santos
 */
public class TransitionPartUiTest extends AbstractConfigUiTestCase {

	@SuppressWarnings("unchecked")
	public void testDeleteBetweenSimpleActivityParts() throws Exception {
		cEditor = openFileInEditor("src/split-batch.xml");
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
				EditPartMatcherFactory.editPartOfType(SimpleActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart step1 = parts.get(0);
		SWTBotGefConnectionEditPart transitionPart = step1.sourceConnections().get(0);
		SWTBotGefEditPart step2 = transitionPart.target();
		assertTrue(step2.part() instanceof SimpleActivityPart);

		transitionPart.select();
		gEditor.pressShortcut(KeyStroke.getInstance(SWT.DEL));

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(SimpleActivityPart.class),
				EditPartMatcherFactory.withLabel("step1")));
		assertFalse(parts.isEmpty());
		assertTrue(parts.get(0).sourceConnections().isEmpty());
	}

	@SuppressWarnings("unchecked")
	public void testDeleteBetweenSimpleAndStructuredActivityParts() throws Exception {
		cEditor = openFileInEditor("src/split-batch.xml");
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
				EditPartMatcherFactory.withLabel("split")));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart split = parts.get(0);
		SWTBotGefConnectionEditPart transitionPart = split.sourceConnections().get(0);
		SWTBotGefEditPart step3 = transitionPart.target();
		assertTrue(step3.part() instanceof SimpleActivityPart);

		transitionPart.select();
		gEditor.pressShortcut(KeyStroke.getInstance(SWT.DEL));

		parts = gEditor.editParts(AllOf.allOf(EditPartMatcherFactory.editPartOfType(StructuredActivityPart.class),
				EditPartMatcherFactory.withLabel("split")));
		assertFalse(parts.isEmpty());
		assertTrue(parts.get(0).sourceConnections().isEmpty());
	}

}
