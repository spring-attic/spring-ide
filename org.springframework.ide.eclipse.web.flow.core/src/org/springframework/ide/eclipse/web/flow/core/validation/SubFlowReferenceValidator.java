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

package org.springframework.ide.eclipse.web.flow.core.validation;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.web.flow.core.IWebFlowProjectMarker;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class SubFlowReferenceValidator implements IWebFlowConfigValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.validation.IWebFlowConfigValidator#validate(org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig,
     *      org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void validate(IWebFlowConfig config, IWebFlowConfigSet configSet,
            IProgressMonitor monitor) {

        if (configSet != null && configSet.getConfigs().size() > 0) {
            List states = config.getState().getStates();

            for (int i = 0; i < states.size(); i++) {
                IState state = (IState) states.get(i);
                if (state instanceof ISubFlowState) {
                    boolean found = false;
                    String subFlow = ((ISubFlowState) state).getFlow();

                    Iterator iterator = configSet.getConfigs().iterator();

                    while (iterator.hasNext()) {
                        IWebFlowConfig subFlowConfig = new WebFlowConfig(
                                (IWebFlowProject) configSet.getElementParent(),
                                (String) iterator.next());
                        IWebFlowState subFlowState = subFlowConfig.getState();
                        if (subFlowConfig.getException() == null) {
                            if (subFlow.equals(subFlowState.getId())) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        WebFlowModelUtils
                                .createProblemMarker(
                                        config,
                                        "Sub Flow reference '"
                                                + subFlow
                                                + "' is not valid within the WebFlow ConfigSet '"
                                                + configSet.getElementName()
                                                + "'",
                                        IMarker.SEVERITY_ERROR,
                                        state.getElementStartLine(),
                                        IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
                    }
                }
            }
        }
    }
}
