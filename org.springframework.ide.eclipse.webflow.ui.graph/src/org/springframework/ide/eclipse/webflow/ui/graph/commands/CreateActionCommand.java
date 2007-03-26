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
package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.RenderActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class CreateActionCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;

	/**
	 * 
	 */
	private int index = -1;

	/**
	 * 
	 */
	private boolean isMove = false;

	private boolean createNew = true;

	/**
	 * 
	 */
	private IState parent;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (createNew) {
			if (child instanceof Action) {
				((Action) child).createNew(parent);
			}
			else if (child instanceof BeanAction) {
				((BeanAction) child).createNew(parent);
			}
			else if (child instanceof EvaluateAction) {
				((EvaluateAction) child).createNew(parent);
			}
			else if (child instanceof Set) {
				((Set) child).createNew(parent);
			}
		}
		if (!isMove) {
			if (DialogUtils.openPropertiesDialog(null, child, true) != Dialog.OK) {
				return;
			}
		}

		if (child.getType() == IActionElement.ACTION_TYPE.ACTION) {
			if (index > 0) {
				((IActionState) parent).addAction(child, index);
			}
			else {
				((IActionState) parent).addAction(child);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION) {
			IViewState viewState = (IViewState) parent;
			if (viewState.getRenderActions() == null) {
				RenderActions exit = new RenderActions();
				exit.createNew(viewState);
				viewState.setRenderActions(exit);
			}
			child.setElementParent(viewState.getRenderActions());
			if (index > 0) {
				viewState.getRenderActions().addRenderAction(child, index);
			}
			else {
				viewState.getRenderActions().addRenderAction(child);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION) {
			if (parent.getEntryActions() == null) {
				EntryActions entry = new EntryActions();
				entry.createNew(parent);
				parent.setEntryActions(entry);
			}
			child.setElementParent(parent.getEntryActions());
			if (index > 0) {
				parent.getEntryActions().addEntryAction(child, index);
			}
			else {
				parent.getEntryActions().addEntryAction(child);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION) {
			if (parent.getExitActions() == null) {
				ExitActions entry = new ExitActions();
				entry.createNew(parent);
				parent.setExitActions(entry);
			}
			child.setElementParent(parent.getExitActions());
			if (index > 0) {
				parent.getExitActions().addExitAction(child, index);
			}
			else {
				parent.getExitActions().addExitAction(child);
			}
		}

	}

	/**
	 * @param action
	 */
	public void setChild(IActionElement action) {
		child = action;
	}

	/**
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * @param isMove
	 */
	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	/**
	 * @param sa
	 */
	public void setParent(IState sa) {
		parent = sa;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (child.getType() == IActionElement.ACTION_TYPE.ACTION) {
			((IActionState) parent).removeAction(child);
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION) {
			IViewState viewState = (IViewState) child;
			viewState.getRenderActions().removeRenderAction(child);
			if (viewState.getRenderActions().getRenderActions().size() == 0) {
				viewState.setRenderActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION) {
			parent.getEntryActions().removeEntryAction(child);
			if (parent.getEntryActions().getEntryActions().size() == 0) {
				parent.setEntryActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION) {
			parent.getExitActions().removeExitAction(child);
			if (parent.getExitActions().getExitActions().size() == 0) {
				parent.setExitActions(null);
			}
		}
	}

	public void redo() {
		boolean tempMove = this.isMove;
		boolean tempCreateNew = this.createNew;
		this.isMove = true;
		this.createNew = false;
		execute();
		this.isMove = tempMove;
		this.createNew = tempCreateNew;
	}
}
