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
package org.springframework.ide.eclipse.config.tests.util.gef;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.ui.IEditorReference;

/**
 * @author Leo Dos Santos
 */
public class StsBotGefEditor extends SWTBotGefEditor {

	private Keyboard keyboard;

	public StsBotGefEditor(IEditorReference reference, SWTWorkbenchBot bot) throws WidgetNotFoundException {
		super(reference, bot);
	}

	public void pressShortcut(final KeyStroke... keys) {
		if (keyboard == null) {
			keyboard = KeyboardFactory.getSWTKeyboard();
		}
		setFocus();
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				keyboard.pressShortcut(keys);
			}
		});
	}

}
