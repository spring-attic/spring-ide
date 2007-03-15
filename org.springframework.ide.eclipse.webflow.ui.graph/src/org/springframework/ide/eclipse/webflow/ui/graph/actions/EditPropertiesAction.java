/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
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
						((IWebflowModelElement) child).getElementParent(),
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