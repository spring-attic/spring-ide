/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowPlugin;

public class DialogUtils {

    public static int openPropertiesDialog(IWebFlowModelElement parent,
            IWebFlowModelElement element, boolean newMode) {
        int result = Dialog.OK;
        Dialog dialog = null;
        if (element instanceof IEndState) {
            dialog = new EndStatePropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IEndState) element);
        }
        else if (element instanceof IViewState) {
            dialog = new ViewStatePropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IViewState) element);
        }
        else if (element instanceof ISubFlowState) {
            dialog = new SubFlowStatePropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (ISubFlowState) element);
        }
        else if (element instanceof IActionState) {
            dialog = new ActionStatePropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IActionState) element);
        }
        else if (element instanceof IAction) {
            dialog = new ActionPropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IAction) element);
        }
        else if (element instanceof IStateTransition) {
            dialog = new StateTransitionPropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IStateTransition) element);
        }
        else if (element instanceof IDecisionState) {
            dialog = new DecisionStatePropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IDecisionState) element);
        }
        else if (element instanceof IIf) {
            dialog = new IfPropertiesDialog(WebFlowPlugin
                    .getActiveWorkbenchWindow().getShell(), parent,
                    (IIf) element, newMode);
        }
        if (dialog != null) {
            dialog.setBlockOnOpen(true);
            result = dialog.open();
        }
        return result;
    }

}