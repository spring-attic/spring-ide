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

package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.internal.model.InputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;

/**
 * 
 */
public class DialogUtils {

	/**
	 * 
	 * 
	 * @param element 
	 * @param newMode 
	 * @param parent 
	 * 
	 * @return 
	 */
	public static int openPropertiesDialog(IWebflowModelElement parent,
			IWebflowModelElement element, boolean newMode) {
		int result = Dialog.OK;
		Dialog dialog = null;
		Shell shell = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		if (element instanceof IEndState) {
			dialog = new EndStatePropertiesDialog(shell, parent,
					(IEndState) element);
		}
		else if (element instanceof IViewState) {
			dialog = new ViewStatePropertiesDialog(shell, parent,
					(IViewState) element);
		}
		else if (element instanceof ISubflowState) {
			dialog = new SubFlowStatePropertiesDialog(shell, parent,
					(ISubflowState) element);
		}
		else if (element instanceof IActionState) {
			dialog = new ActionStatePropertiesDialog(shell, parent,
					(IActionState) element);
		}
		else if (element instanceof Action) {
			dialog = new ActionPropertiesDialog(shell, parent, (Action) element);
		}
		else if (element instanceof BeanAction) {
			dialog = new BeanActionPropertiesDialog(shell, parent,
					(BeanAction) element);
		}
		else if (element instanceof EvaluateAction) {
			dialog = new EvaluateActionPropertiesDialog(shell, parent,
					(EvaluateAction) element);
		}
		else if (element instanceof Set) {
			dialog = new SetActionPropertiesDialog(shell, parent, (Set) element);
		}
		else if (element instanceof OutputAttribute) {
			dialog = new InputAttributeEditorDialog(shell,
					(IOutputAttribute) element);
		}
		else if (element instanceof InputAttribute) {
			dialog = new InputAttributeEditorDialog(shell,
					(IInputAttribute) element);
		}
		else if (element instanceof IMapping) {
			dialog = new MappingEditorDialog(shell, (IMapping) element);
		}
		else if (element instanceof ExceptionHandler) {
			dialog = new ExceptionHandlerPropertiesDialog(shell, parent,
					(ExceptionHandler) element);
		}
		else if (element instanceof IStateTransition) {
			dialog = new StateTransitionPropertiesDialog(shell, parent,
					(IStateTransition) element);
		}
		else if (element instanceof IDecisionState) {
			dialog = new DecisionStatePropertiesDialog(shell, parent,
					(IDecisionState) element);
		}
		else if (element instanceof IIf) {
			dialog = new IfPropertiesDialog(shell, (IDecisionState) parent,
					(IIf) element, newMode);
		}
		else if (element instanceof IWebflowState) {
			dialog = new WebflowStatePropertiesDialog(shell,
					(IWebflowState) element);
		}
		if (dialog != null) {
			dialog.setBlockOnOpen(true);
			result = dialog.open();
		}
		return result;
	}
}