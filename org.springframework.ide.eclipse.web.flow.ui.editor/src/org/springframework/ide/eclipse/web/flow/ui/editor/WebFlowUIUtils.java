/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.ActionProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.ActionStateProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.AttributeMapperProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.EndStateProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.IfTransitionProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.StateTransitionProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.SubFlowStateProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.ViewStateProperties;
import org.springframework.ide.eclipse.web.flow.ui.editor.properties.WebFlowStateProperties;

public abstract class WebFlowUIUtils {

    public static final IPropertySource getPropertySource(Object state) {
        if (state instanceof IActionState) {
            return new ActionStateProperties((IActionState) state);
        }
        else if (state instanceof IViewState) {
            return new ViewStateProperties((IViewState) state);
        }
        else if (state instanceof IEndState) {
            return new EndStateProperties((IEndState) state);
        }
        else if (state instanceof ISubFlowState) {
            return new SubFlowStateProperties((ISubFlowState) state);
        }
        else if (state instanceof IWebFlowState) {
            return new WebFlowStateProperties((IWebFlowState) state);
        }
        else if (state instanceof IStateTransition) {
            return new StateTransitionProperties((IStateTransition) state);
        }
        else if (state instanceof IIfTransition) {
            return new IfTransitionProperties((IIfTransition) state);
        }
        else if (state instanceof IAction) {
            return new ActionProperties((IAction) state);
        }
        else if (state instanceof IAttributeMapper) {
            return new AttributeMapperProperties((IAttributeMapper) state);
        }
        return null;
    }

    public static String returnNotNullOnString(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

    public static WebFlowEditor getActiveFlowEditor() {

        IEditorPart editorPart = WebFlowPlugin.getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        if (editorPart instanceof WebFlowEditor) {
            return (WebFlowEditor) editorPart;
        }
        return null;
    }

    public static WebFlowEditorInput getActiveFlowEditorInput() {

        WebFlowEditor editor = getActiveFlowEditor();
        if (editor != null) {
            return (WebFlowEditorInput) editor.getEditorInput();
        }
        return null;
    }
}