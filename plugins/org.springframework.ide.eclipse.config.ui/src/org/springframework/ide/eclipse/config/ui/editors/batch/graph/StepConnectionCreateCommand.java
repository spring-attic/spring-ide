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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph;

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.commands.AbstractConnectionCreateCommand;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StepConnectionCreateCommand extends AbstractConnectionCreateCommand {

	protected String oldTargetId;

	protected String targetId;

	public StepConnectionCreateCommand(ITextEditor textEditor, int lineStyle) {
		super(textEditor, lineStyle);
	}

	@Override
	public boolean canExecute() {
		if (super.canExecute()) {
			oldTargetId = sourceElement.getAttribute(BatchSchemaConstants.ATTR_NEXT);
			targetId = targetElement.getAttribute(BatchSchemaConstants.ATTR_ID);
			if (targetId != null && targetId.trim().length() != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void execute() {
		sourceElement.setAttribute(BatchSchemaConstants.ATTR_NEXT, targetId);
	}

}
