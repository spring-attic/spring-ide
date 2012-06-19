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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.RenameActivityCommand;


/**
 * @author Leo Dos Santos
 */
public class RenameTransitionCommand extends RenameActivityCommand {

	public RenameTransitionCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public boolean canExecute() {
		input = source.getInput();
		if (input == null) {
			return false;
		}

		List<String> attrs = processor.getAttributeNames(input);
		return attrs.contains(WebFlowSchemaConstants.ATTR_ON);
	}

	@Override
	public void execute() {
		input.setAttribute(WebFlowSchemaConstants.ATTR_ON, name);
	}

}
