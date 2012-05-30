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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.RefactoringRenameCommand;


/**
 * @author Leo Dos Santos
 */
public class RenameChannelCommand extends RefactoringRenameCommand {

	public RenameChannelCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	protected List<String> getAttributesToCheck() {
		return Arrays.asList(new String[] { IntegrationSchemaConstants.ATTR_CHANNEL,
				IntegrationSchemaConstants.ATTR_DEFAULT_CHANNEL,
				IntegrationSchemaConstants.ATTR_DEFAULT_OUTPUT_CHANNEL,
				IntegrationSchemaConstants.ATTR_DEFAULT_REPLY_CHANNEL,
				IntegrationSchemaConstants.ATTR_DEFAULT_REQUEST_CHANNEL,
				IntegrationSchemaConstants.ATTR_DISCARD_CHANNEL, IntegrationSchemaConstants.ATTR_ERROR_CHANNEL,
				IntegrationSchemaConstants.ATTR_INPUT_CHANNEL, IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL,
				IntegrationSchemaConstants.ATTR_REPLY_CHANNEL, IntegrationSchemaConstants.ATTR_REQUEST_CHANNEL });
	}

}
