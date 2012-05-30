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

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;


/**
 * OrphanChildCommand
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class OrphanChildCommand extends AbstractTextCommand {

	private StructuredActivity parent;

	private Activity child;

	public OrphanChildCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		parent.removeChild(child);
	}

	/**
	 * Sets the child to the passed Activity
	 * @param child the child
	 */
	public void setChild(Activity child) {
		this.child = child;
	}

	/**
	 * Sets the parent to the passed StructuredActivity
	 * @param parent the parent
	 */
	public void setParent(StructuredActivity parent) {
		this.parent = parent;
	}

}
