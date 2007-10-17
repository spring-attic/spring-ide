/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * {@link SelectionAction} that opens the Properties Dialog of the selected
 * {@link IWebflowModelElement} and in case the {@link Dialog} is closed with
 * {@link Dialog.OK} the values will be applied to the original element.
 * @author Christian Dupuis
 * @since 2.0
 */
public class EditPropertiesAction extends SelectionAction {

	public static final String EDITPROPERTIES_REQUEST = "Edit_propeties";

	public static final String EDITPROPERTIES = "Edit_propeties";

	private Request request;

	boolean openDialog = true;

	public void setOpenDialog(boolean openDialog) {
		this.openDialog = openDialog;
	}

	public EditPropertiesAction(IWorkbenchPart part) {
		super(part);
		request = new Request(EDITPROPERTIES_REQUEST);
		setText("Properties");
		setId(EDITPROPERTIES);
		setToolTipText("Edit properties of selected state");
		setImageDescriptor(WebflowUIImages.DESC_OBJS_PROPERTIES);
		setHoverImageDescriptor(getImageDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {
		return canPerformAction();
	}

	/**
	 * @return
	 */
	private boolean canPerformAction() {
		if (getSelectedObjects().isEmpty())
			return false;
		List parts = getSelectedObjects();
		for (int i = 0; i < parts.size(); i++) {
			Object o = parts.get(i);
			if (!(o instanceof EditPart))
				return false;
			EditPart part = (EditPart) o;
			return part.getModel() instanceof IWebflowModelElement;
		}
		return true;
	}

	/**
	 * @return
	 */
	private CompoundCommand getCommand() {
		List editparts = getSelectedObjects();
		CompoundCommand cc = new CompoundCommand();
		for (int i = 0; i < editparts.size(); i++) {
			EditPart part = (EditPart) editparts.get(i);
			cc.add(part.getCommand(request));
		}
		return cc;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		CompoundCommand cc = getCommand();

		for (int i = 0; i < cc.getCommands().size(); i++) {
			if (cc.getCommands().get(i) instanceof EditPropertiesCommand) {
				EditPropertiesCommand command = (EditPropertiesCommand) cc
						.getCommands().get(i);
				this.openDialog = true;
				runWithCommand(command);
			}
		}
	}

	/**
	 * @param command
	 */
	public void runWithCommand(Command cc) {
		int result = -1;
		EditPropertiesCommand command = (EditPropertiesCommand) cc;
		IWebflowModelElement child = command.getChild();
		IWebflowModelElement newChild = command.getChildClone();

		if (openDialog) {
			if (((IWebflowModelElement) child).getElementParent() instanceof IWebflowModelElement) {
				result = DialogUtils.openPropertiesDialog(
						(IWebflowModelElement)((IWebflowModelElement) child).getElementParent(),
						(IWebflowModelElement) newChild, false);
			}
			else {
				result = DialogUtils.openPropertiesDialog(null,
						(IWebflowModelElement) newChild, false);
			}
			if (result == Dialog.OK) {
				execute(command);
			}
		}
		else {
			execute(command);
		}
	}
}
