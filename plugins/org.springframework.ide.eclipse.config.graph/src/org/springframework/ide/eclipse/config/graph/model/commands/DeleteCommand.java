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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.document.NodeImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.w3c.dom.Node;


/**
 * Handles the deletion of Activities.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class DeleteCommand extends AbstractTextCommand {

	private Activity child;

	private StructuredActivity parent;

	private IDOMElement childElement;

	private IDOMElement parentElement;

	private final List<Transition> sourceConnections = new ArrayList<Transition>();

	private final List<Transition> targetConnections = new ArrayList<Transition>();

	public DeleteCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public boolean canExecute() {
		childElement = child.getInput();
		parentElement = parent.getInput();
		if (childElement == null || parentElement == null) {
			return false;
		}

		if (childElement.getOwnerDocument() == null || childElement.getParentNode() == null
				|| parentElement.getParentNode() == null) {
			return false;
		}

		Node ancestor = ((NodeImpl) childElement.getOwnerDocument()).getCommonAncestor(childElement);
		if (ancestor == null) {
			return false;
		}
		return super.canExecute();
	}

	private void deleteConnections(Activity a) {
		if (a instanceof StructuredActivity) {
			List<Activity> children = ((StructuredActivity) a).getChildren();
			for (int i = 0; i < children.size(); i++) {
				deleteConnections(children.get(i));
			}
		}
		sourceConnections.addAll(a.getIncomingTransitions());
		for (int i = 0; i < sourceConnections.size(); i++) {
			Transition t = sourceConnections.get(i);
			t.source.removeOutgoing(t);
			a.removeIncoming(t);
		}
		targetConnections.addAll(a.getOutgoingTransitions());
		for (int i = 0; i < targetConnections.size(); i++) {
			Transition t = targetConnections.get(i);
			t.target.removeIncoming(t);
			a.removeOutgoing(t);
		}
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		if (canExecute()) {
			primExecute();
		}
	}

	/**
	 * Invokes the execution of this command.
	 */
	protected void primExecute() {
		deleteConnections(child);
		parent.removeChild(child);
	}

	/**
	 * Sets the child to the passed Activity
	 * @param a the child
	 */
	public void setChild(Activity a) {
		child = a;
	}

	/**
	 * Sets the parent to the passed StructuredActivity
	 * @param sa the parent
	 */
	public void setParent(StructuredActivity sa) {
		parent = sa;
	}

}
