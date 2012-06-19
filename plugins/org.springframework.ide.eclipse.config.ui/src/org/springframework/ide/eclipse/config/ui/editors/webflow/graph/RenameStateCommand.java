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

import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.RefactoringRenameCommand;


/**
 * @author Leo Dos Santos
 */
public class RenameStateCommand extends RefactoringRenameCommand {

	public RenameStateCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	protected List<String> getAttributesToCheck() {
		return Arrays.asList(new String[] { WebFlowSchemaConstants.ATTR_ELSE, WebFlowSchemaConstants.ATTR_THEN,
				WebFlowSchemaConstants.ATTR_TO });
	}
}
