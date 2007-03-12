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

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.RenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class AddActionCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;

	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (child.getType() == IActionElement.ACTION_TYPE.ACTION
				&& parent instanceof IActionState) {
			child.setElementParent(parent);
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION
				&& parent instanceof IViewState) {
			IViewState state = (IViewState) parent;
			if (state.getRenderActions() == null) {
				RenderActions entry = new RenderActions();
				entry.createNew(state);
				state.setRenderActions(entry);
			}
			child.setElementParent(state.getRenderActions());
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION
				&& parent instanceof IState) {
			IState state = (IState) parent;
			if (state.getEntryActions() == null) {
				EntryActions entry = new EntryActions();
				entry.createNew(state);
				state.setEntryActions(entry);
			}
			child.setElementParent(state.getEntryActions());
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION
				&& parent instanceof IState) {
			IState state = (IState) parent;
			if (state.getExitActions() == null) {
				ExitActions entry = new ExitActions();
				entry.createNew(state);
				state.setExitActions(entry);
			}
			child.setElementParent(state.getExitActions());
		}

		if (child.getElementParent() instanceof IEntryActions) {
			((IEntryActions) child.getElementParent()).addEntryAction(child);
		}
		else if (child.getElementParent() instanceof IExitActions) {
			((IExitActions) child.getElementParent()).addExitAction(child);
		}
		else if (child.getElementParent() instanceof IRenderActions) {
			((IRenderActions) child.getElementParent()).addRenderAction(child);
		}
		else if (child.getElementParent() instanceof IActionState) {
			((IActionState) child.getElementParent()).addAction(child);
		}
	}

	/**
	 * 
	 * 
	 * @param newChild 
	 */
	public void setChild(IActionElement newChild) {
		child = newChild;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (child.getElementParent() instanceof IEntryActions) {
			((IEntryActions) child.getElementParent()).removeEntryAction(child);
		}
		else if (child.getElementParent() instanceof IExitActions) {
			((IExitActions) child.getElementParent()).removeExitAction(child);
		}
		else if (child.getElementParent() instanceof IRenderActions) {
			((IRenderActions) child.getElementParent()).removeRenderAction(child);
		}
		else if (child.getElementParent() instanceof IActionState) {
			((IActionState) child.getElementParent()).removeAction(child);
		}

		if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION
				&& child.getElementParent() instanceof IRenderActions) {
			IRenderActions state = (IRenderActions) child.getElementParent();
			if (state != null
					&& state.getRenderActions().size() == 0) {
				((IViewState) state.getElementParent()).setRenderActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION
				&& child.getElementParent() instanceof IEntryActions) {
			IEntryActions state = (IEntryActions) child.getElementParent();
			if (state != null
					&& state.getEntryActions().size() == 0) {
				((IState) state.getElementParent()).setEntryActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION
				&& child.getElementParent() instanceof IExitActions) {
			IExitActions state = (IExitActions) child.getElementParent();
			if (state != null
					&& state.getExitActions().size() == 0) {
				((IState) state.getElementParent()).setExitActions(null);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param element 
	 */
	public void setParent(IWebflowModelElement element) {
		this.parent = element;
	}

}