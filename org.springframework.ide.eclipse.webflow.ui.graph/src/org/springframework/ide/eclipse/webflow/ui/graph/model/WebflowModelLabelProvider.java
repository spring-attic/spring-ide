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
package org.springframework.ide.eclipse.webflow.ui.graph.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.model.IAction;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IBeanAction;
import org.springframework.ide.eclipse.webflow.core.model.IBeanReference;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IEvaluateAction;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISet;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * 
 */
public class WebflowModelLabelProvider extends LabelProvider {

	/**
	 * 
	 */
	private final BeansModelLabelProvider BEANS_LABEL_PROVIDER = new BeansModelLabelProvider(
			true);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if (obj instanceof IActionState) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_ACTION_STATE);
		}
		else if (obj instanceof IViewState) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_VIEW_STATE);
		}
		else if (obj instanceof IEndState) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_END_STATE);
		}
		else if (obj instanceof ISubflowState) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_SUBFLOW_STATE);
		}
		else if (obj instanceof IBeanAction) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION);
		}
		else if (obj instanceof ISet) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION);
		}
		else if (obj instanceof IEvaluateAction) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION);
		}
		else if (obj instanceof IAction) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_ACTION);
		}
		else if (obj instanceof IAttributeMapper) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER);
		}
		else if (obj instanceof IDecisionState) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_DECISION_STATE);
		}
		else if (obj instanceof IIf) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_IF);
		}
		else if (obj instanceof IAttribute) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_PROPERTIES);
		}
		else if (obj instanceof IBean) {
			return BEANS_LABEL_PROVIDER.getImage(obj);
		}
		else if (obj instanceof IInputMapper) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_INPUT);
		}
		else if (obj instanceof IOutputMapper) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_OUTPUT);
		}
		else if (obj instanceof IInlineFlowState) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		else if (obj instanceof IExceptionHandler) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_EXCEPTION_HANDLER);
		}
		else if (obj instanceof IWebflowState) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		else if (obj instanceof IStateTransition) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_TRANSITION);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return this.getText(element, false, true, false);
	}

	/**
	 * 
	 * 
	 * @param element
	 * @param showBean
	 * @param showAdditionalInfo
	 * @param showElementType
	 * 
	 * @return
	 */
	public String getText(Object element, boolean showElementType,
			boolean showAdditionalInfo, boolean showError) {
		StringBuffer buf = new StringBuffer();
		if (element instanceof IState) {
			buf.append(((IState) element).getId());
		}
		else if (element instanceof Action) {
			Action action = (Action) element;
			if (action.getName() != null) {
				buf.append(action.getName());
				buf.append(": ");
			}
			if (action.getBean() != null) {
				buf.append(action.getBean());
			}
			if (action.getMethod() != null) {
				buf.append(".");
				buf.append(action.getMethod());
				if (action.getMethod().lastIndexOf("(") == -1) {
					buf.append("()");
				}
			}
		}
		else if (element instanceof BeanAction) {
			BeanAction action = (BeanAction) element;
			if (action.getName() != null) {
				buf.append(action.getName());
				buf.append(": ");
			}
			if (action.getBean() != null) {
				buf.append(action.getBean());
			}
			if (action.getMethod() != null) {
				buf.append(".");
				buf.append(action.getMethod());
				if (action.getMethod().lastIndexOf("(") == -1) {
					buf.append("()");
				}
			}
		}
		else if (element instanceof IEvaluateAction) {
			IEvaluateAction action = (IEvaluateAction) element;
			if (action.getName() != null) {
				buf.append(action.getName());
				buf.append(": ");
			}
			if (action.getExpression() != null) {
				buf.append(action.getExpression());
			}
		}
		else if (element instanceof ISet) {
			ISet action = (ISet) element;
			if (action.getAttribute() != null) {
				buf.append(action.getAttribute());
				buf.append(" = ");
			}
			if (action.getValue() != null) {
				buf.append(action.getValue());
			}
		}
		else if (element instanceof IExceptionHandler) {
			IExceptionHandler action = (IExceptionHandler) element;
			if (action.getBean() != null) {
				buf.append(action.getBean());
			}
		}
		else if (element instanceof IAttributeMapper) {
			IAttributeMapper attributeMapper = (IAttributeMapper) element;
			if (attributeMapper.getBean() != null) {
				buf.append(attributeMapper.getBean());
			}
			else {
				buf.append("attribute-mapper");
			}
		}
		else if (element instanceof IAttribute) {
			IAttribute property = (IAttribute) element;
			buf.append(property.getName());
			buf.append("=");
			buf.append(property.getValue());
		}
		else if (element instanceof IIf) {
			IIf theIf = (IIf) element;
			// int index = ((IDecisionState)
			// theIf.getElementParent()).getIfs().indexOf(theIf) + 1;
			// buf.append(index);
			// buf.append(": ");
			buf.append(theIf.getTest());
		}
		else if (element instanceof IBean) {
			IBean bean = (IBean) element;
			buf.append(bean.getElementName());
			if (bean.getClassName() != null) {
				buf.append(" [");
				buf.append(bean.getClassName());
				buf.append(']');
			}
		}
		else if (element instanceof IStateTransition) {
			IStateTransition state = (IStateTransition) element;
			if (state.getToStateId() != null) {
				buf.append("To: " + state.getToStateId());
			}
			if (state.getToStateId() != null) {
				buf.append("\nOn: " + state.getOn());
			}
		}
		else {
			buf.append(super.getText(element));
		}
		if (showAdditionalInfo) {
			if (element instanceof IViewState) {
				IViewState state = (IViewState) element;
				if (state.getView() != null) {
					buf.append("\nView: " + state.getView());
				}
			}
			if (element instanceof ISubflowState) {
				ISubflowState state = (ISubflowState) element;
				if (state.getFlow() != null) {
					buf.append("\nFlow: " + state.getFlow());
				}
			}
			if (element instanceof IEndState) {
				IEndState state = (IEndState) element;
				if (state.getView() != null) {
					buf.append("\nView: " + state.getView());
				}
			}
			if (element instanceof IStateTransition) {
				IStateTransition state = (IStateTransition) element;
				if (state.getOnException() != null) {
					buf.append("\nOn-exception: " + state.getOnException());
				}
			}
		}

		if (showElementType) {
			buf.append(" [");
			if (element instanceof IEndState) {
				buf.append("End State");
			}
			else if (element instanceof IViewState) {
				buf.append("View State");
			}
			else if (element instanceof ISubflowState) {
				buf.append("Subflow State");
			}
			else if (element instanceof IActionState) {
				buf.append("Action State");
			}
			else if (element instanceof Action) {
				buf.append("Action");
			}
			else if (element instanceof IBeanAction) {
				buf.append("Bean Action");
			}
			else if (element instanceof IEvaluateAction) {
				buf.append("Evaluation Action");
			}
			else if (element instanceof ISet) {
				buf.append("Set");
			}
			else if (element instanceof IAttributeMapper) {
				buf.append("Attribute Mapper");
			}
			else if (element instanceof IAttribute) {
				buf.append("Property");
			}
			else if (element instanceof IIf) {
				buf.append("If");
			}
			else if (element instanceof IDecisionState) {
				buf.append("Decision State");
			}
			else if (element instanceof IInputMapper) {
				buf.append("Input");
			}
			else if (element instanceof IOutputMapper) {
				buf.append("Output");
			}
			else if (element instanceof IInlineFlowState) {
				buf.append("Inline Flow");
			}
			else if (element instanceof IExceptionHandler) {
				buf.append("Exception Handler");
			}
			else if (element instanceof IStateTransition) {
				buf.append("Transition");
			}
			buf.append("]");
		}
		
		if (showError) {
			buf.append(WebflowUtils
					.getErrorTooltip((IWebflowModelElement) element));
		}

		return buf.toString();
	}

	/**
	 * 
	 * 
	 * @param element
	 * 
	 * @return
	 */
	public String getLongText(Object element) {
		StringBuffer buf = new StringBuffer();
		if (element instanceof IState) {
			buf.append(((IState) element).getId());
		}
		else if (element instanceof IBeanReference) {
			IBeanReference action = (IBeanReference) element;
			if (action.getBean() != null) {
				buf.append(action.getBean());
			}
		}
		else if (element instanceof IAttributeMapper) {
			IAttributeMapper attributeMapper = (IAttributeMapper) element;
			if (attributeMapper.getBean() != null) {
				buf.append(attributeMapper.getBean());
			}
		}
		else {
			buf.append(super.getText(element));
		}

		return buf.toString();
	}
}
