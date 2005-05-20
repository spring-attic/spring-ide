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

package org.springframework.ide.eclipse.web.flow.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

public class WebFlowModelUtils {

    public static final void createProblemMarker(IWebFlowModelElement element,
            String message, int severity, int line, int errorCode) {
        createProblemMarker(element, message, severity, line, errorCode, null,
                null);
    }

    public static final void createProblemMarker(IWebFlowModelElement element,
            String message, int severity, int line, int errorCode,
            String beanID, String errorData) {
        IFile file;
        if (element instanceof IWebFlowConfig) {
            file = ((IWebFlowConfig) element).getConfigFile();
        }
        else if (element instanceof IState) {
            file = ((IState) element).getConfig().getConfigFile();
        }
        else if (element instanceof IAction) {
            IState bean = (IState) ((IAction) element).getElementParent();
            file = bean.getConfig().getConfigFile();
        }
        else {
            file = null;
        }
        if (file != null) {
            WebFlowCoreUtils.createProblemMarker(file, message, severity, line,
                    errorCode, beanID, errorData);
        }
    }
}