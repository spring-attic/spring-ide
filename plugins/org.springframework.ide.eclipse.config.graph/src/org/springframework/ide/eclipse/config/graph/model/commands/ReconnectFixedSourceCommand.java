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

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.parts.FixedConnectionAnchor;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ReconnectFixedSourceCommand extends AbstractTextCommand {

	private String id;

	private String attr;

	private Activity source;

	private Activity target;

	private Transition transition;

	private IDOMElement sourceElement;

	private IDOMElement targetElement;

	private FixedConnectionAnchor sourceAnchor;

	private Activity oldSource;

	private IDOMElement oldSourceElement;

	private IDOMAttr transitionAttr;

	public ReconnectFixedSourceCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public boolean canExecute() {
		if (source == null || target == null || source.equals(target)) {
			return false;
		}

		sourceElement = source.getInput();
		targetElement = target.getInput();
		oldSourceElement = oldSource.getInput();
		if (sourceElement == null || targetElement == null || oldSourceElement == null) {
			return false;
		}

		if (!(transition.getInput() instanceof IDOMAttr)) {
			return false;
		}
		transitionAttr = (IDOMAttr) transition.getInput();

		if (sourceAnchor != null && transitionAttr.getOwnerElement().equals(targetElement)) {
			return false;
		}
		if (sourceAnchor == null && transitionAttr.getOwnerElement().equals(oldSourceElement)) {
			return false;
		}

		if (sourceAnchor != null) {
			attr = sourceAnchor.getConnectionLabel();
			id = transitionAttr.getValue();
		}
		else {
			attr = transitionAttr.getName();
			id = sourceElement.getAttribute(BeansSchemaConstants.ATTR_ID);
		}

		if (id != null && id.trim().length() != 0) {
			return true;
		}
		return false;
	}

	@Override
	public void execute() {
		if (sourceAnchor != null) {
			IDOMDocument document = (IDOMDocument) transitionAttr.getOwnerDocument();
			IDOMModel model = document.getModel();
			model.beginRecording(this);
			oldSourceElement.removeAttribute(transitionAttr.getName());
			sourceElement.setAttribute(attr, id);
			model.endRecording(this);
		}
		else {
			targetElement.setAttribute(attr, id);
		}
	}

	public void setSource(Activity activity) {
		source = activity;
	}

	public void setSourceAnchor(FixedConnectionAnchor anchor) {
		sourceAnchor = anchor;
	}

	public void setTransition(Transition trans) {
		transition = trans;
		target = trans.target;
		oldSource = trans.source;
	}

}
