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
package org.springframework.ide.eclipse.webflow.ui.graph.properties;

import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IBeanAction;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IEvaluateAction;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISet;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * 
 */
public class PropertiesModelLabelProvider extends LabelProvider {

	/**
	 * 
	 */
	protected static ILabelProvider labelProvider = new DecoratingLabelProvider(
			new WebflowModelLabelProvider(), new WebflowModelLabelDecorator());

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object struct) {
		Object obj = ((IStructuredSelection) struct).getFirstElement();
		if (obj instanceof AbstractEditPart) {
			return labelProvider.getImage(((AbstractEditPart) obj).getModel());
		}
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object struct) {
		Object element = ((IStructuredSelection) struct).getFirstElement();
		StringBuffer buf = new StringBuffer();
		if (element instanceof AbstractEditPart ) {
			Object obj = ((AbstractEditPart) element).getModel();
			if (obj instanceof IEndState) {
				buf.append("End State");
			}
			else if (obj instanceof IViewState) {
				buf.append("View State");
			}
			else if (obj instanceof ISubflowState) {
				buf.append("Subflow State");
			}
			else if (obj instanceof IActionState) {
				buf.append("Action State");
			}
			else if (obj instanceof Action) {
				buf.append("Action");
			}
			else if (obj instanceof IBeanAction) {
				buf.append("Bean Action");
			}
			else if (obj instanceof IEvaluateAction) {
				buf.append("Evaluation Action");
			}
			else if (obj instanceof ISet) {
				buf.append("Set");
			}
			else if (obj instanceof IAttributeMapper) {
				buf.append("Attribute Mapper");
			}
			else if (obj instanceof IAttribute) {
				buf.append("Property");
			}
			else if (obj instanceof IIf) {
				buf.append("If");
			}
			else if (obj instanceof IDecisionState) {
				buf.append("Decision State");
			}
			else if (obj instanceof IInputMapper) {
				buf.append("Input");
			}
			else if (obj instanceof IOutputMapper) {
				buf.append("Output");
			}
			else if (obj instanceof IInlineFlowState) {
				buf.append("Inline Flow");
			}
			else if (obj instanceof IExceptionHandler) {
				buf.append("Exception Handler");
			}
			else if (obj instanceof IWebflowState) {
				buf.append("Flow");
			}
			else if (obj instanceof IStateTransition) {
				buf.append("Transition");
			}
		}
		else {
			buf.append("Web Flow");
		}
		return buf.toString();
	}
}
