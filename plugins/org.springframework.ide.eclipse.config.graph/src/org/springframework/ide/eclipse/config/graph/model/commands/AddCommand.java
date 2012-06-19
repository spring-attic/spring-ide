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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.document.NodeImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.w3c.dom.Node;


/**
 * AddCommand
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class AddCommand extends AbstractTextCommand {

	private StructuredActivity parent;

	private Activity child;

	private IDOMElement parentElement;

	private IDOMElement childElement;

	private final EditPartViewer viewer;

	public AddCommand(ITextEditor textEditor, EditPartViewer viewer) {
		super(textEditor);
		this.viewer = viewer;
	}

	@Override
	public boolean canExecute() {
		parentElement = parent.getInput();
		childElement = child.getInput();
		if (parentElement == null || childElement == null) {
			return false;
		}

		Node ancestor = ((NodeImpl) parentElement).getCommonAncestor(childElement);
		if (ancestor != null && (ancestor.equals(childElement))) {
			return false;
		}

		List<String> children = processor.getChildNames(parentElement);
		return children.contains(childElement.getNodeName());
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		parent.addChild(child);

		Object part = viewer.getEditPartRegistry().get(child);
		if (part instanceof EditPart) {
			viewer.flush();
			viewer.reveal((EditPart) part);
			viewer.select((EditPart) part);
		}
	}

	/**
	 * Returns the StructuredActivity that is the parent
	 * @return the parent
	 */
	public StructuredActivity getParent() {
		return parent;
	}

	/**
	 * Sets the child to the passed Activity
	 * @param subpart the child
	 */
	public void setChild(Activity newChild) {
		child = newChild;
	}

	/**
	 * Sets the parent to the passed StructuredActiivty
	 * @param newParent the parent
	 */
	public void setParent(StructuredActivity newParent) {
		parent = newParent;
	}

}
