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
package org.springframework.ide.eclipse.config.tests.util;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartId;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.finders.WorkbenchContentsFinder;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigEditor;
import org.springsource.ide.eclipse.commons.tests.util.swtbot.StsBot;


/**
 * @author Leo Dos Santos
 */
public class StsConfigBot extends StsBot {

	private final WorkbenchContentsFinder workbenchContentsFinder = new WorkbenchContentsFinder();

	public StsBotConfigEditor activeConfigEditor() {
		IEditorReference editor = workbenchContentsFinder.findActiveEditor();
		if (!(editor.getEditor(false) instanceof SpringConfigEditor)) {
			throw new WidgetNotFoundException("There is no active editor");
		}
		return new StsBotConfigEditor(editor, this);
	}

	public List<StsBotConfigEditor> configEditors() {
		Matcher<?> matcher = withPartId(SpringConfigEditor.ID_EDITOR);
		return configEditors(matcher);
	}

	public List<StsBotConfigEditor> configEditors(Matcher<?> matcher) {
		List<IEditorReference> editorReferences = workbenchContentsFinder.findEditors(matcher);

		List<StsBotConfigEditor> editorBots = new ArrayList<StsBotConfigEditor>();
		for (IEditorReference editorReference : editorReferences) {
			editorBots.add(new StsBotConfigEditor(editorReference, this));
		}

		return editorBots;
	}

}
