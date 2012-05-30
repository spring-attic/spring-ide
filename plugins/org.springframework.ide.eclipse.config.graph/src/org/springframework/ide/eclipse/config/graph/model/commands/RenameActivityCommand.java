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

import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * Command to rename Activities.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class RenameActivityCommand extends AbstractTextCommand {

	protected Activity source;

	protected String name;

	protected IDOMElement input;

	protected String oldName;

	public RenameActivityCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public boolean canExecute() {
		input = source.getInput();
		if (input == null) {
			return false;
		}

		List<String> attrs = processor.getAttributeNames(input);
		return attrs.contains(BeansSchemaConstants.ATTR_ID);
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		input.setAttribute(BeansSchemaConstants.ATTR_ID, name);
	}

	/**
	 * Sets the new Activity name
	 * @param string the new name
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Sets the old Activity name
	 * @param string the old name
	 */
	public void setOldName(String string) {
		oldName = string;
	}

	/**
	 * Sets the source Activity
	 * @param activity the source Activity
	 */
	public void setSource(Activity activity) {
		source = activity;
	}

}
