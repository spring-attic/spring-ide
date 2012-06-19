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
package org.springframework.ide.eclipse.config.graph.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class AbstractTextCommand extends Command {

	protected SpringConfigContentAssistProcessor processor;

	private final ITextEditor textEditor;

	public AbstractTextCommand(ITextEditor textEditor) {
		this.textEditor = textEditor;
		processor = new SpringConfigContentAssistProcessor();
	}

	@Override
	public void dispose() {
		processor.release();
		super.dispose();
	}

	@Override
	public void redo() {
		IAction action = textEditor.getAction(ActionFactory.REDO.getId());
		action.run();
	}

	@Override
	public void undo() {
		IAction action = textEditor.getAction(ActionFactory.UNDO.getId());
		action.run();
	}

}
