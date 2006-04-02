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
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.web.flow.core.IWebFlowProjectMarker;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class BeanReferenceValidator implements IWebFlowConfigValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.validation.IWebFlowConfigValidator#validate(org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig,
     *      org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void validate(IWebFlowConfig config, IWebFlowConfigSet configSet,
            IProgressMonitor monitor) {

        if (configSet != null) {
            // validate root node
            IWebFlowState rootState = config.getState();
            if (rootState != null && rootState instanceof IBeanReference) {
                this.validateBeanReference((IBeanReference) rootState, config,
                        configSet);
            }

            List states = config.getState().getStates();

            if (states != null && states.size() > 0) {
                for (int i = 0; i < states.size(); i++) {
                    IState state = (IState) states.get(i);
                    if (state instanceof IBeanReference) {
                        this.validateBeanReference((IBeanReference) state,
                                config, configSet);
                    }

                    if (state instanceof IActionState) {
                        List actions = ((IActionState) state).getActions();
                        this.validateActions(actions, config, configSet);
                    }

                    if (state instanceof ITransitionableFrom) {
                        List transitions = ((ITransitionableFrom) state)
                                .getOutputTransitions();
                        for (int j = 0; j < transitions.size(); j++) {
                            ITransition transition = (ITransition) transitions
                                    .get(j);
                            if (transition instanceof IStateTransition
                                    && ((IStateTransition) transition)
                                            .getActions() != null
                                    && ((IStateTransition) transition)
                                            .getActions().size() > 0) {
                                this.validateActions(
                                        ((IStateTransition) transition)
                                                .getActions(), config,
                                        configSet);
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateActions(List actions, IWebFlowConfig config,
            IWebFlowConfigSet configSet) {
        for (int j = 0; j < actions.size(); j++) {
            IAction action = (IAction) actions.get(j);
            this.validateBeanReference((IBeanReference) action, config,
                    configSet);
        }
    }

    private void validateBeanReference(IBeanReference reference,
            IWebFlowConfig config, IWebFlowConfigSet configSet) {
        if (reference.hasBeanReference()) {
            boolean valid = true;
            // check if all fields are filled correctly
            String bean = reference.getBean();

            if (bean != null) {
                valid = false;
            }
            if (!valid) {
                WebFlowModelUtils
                        .createProblemMarker(
                                config,
                                "Bean Reference is not valid within the WebFlow ConfigSet '"
                                        + configSet.getElementName()
                                        + "'. Either use bean, classref or class and autowire seperatly",
                                IMarker.SEVERITY_ERROR, reference
                                        .getElementStartLine(),
                                IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
            }

            if (configSet.getBeansConfigSet() != null) {

                IBeansConfigSet beansConfigSet = configSet.getBeansConfigSet();

                if (bean != null) {
                    IBean b = beansConfigSet.getBean(bean);
                    if (b == null) {
                        WebFlowModelUtils
                                .createProblemMarker(
                                        config,
                                        "Bean Reference is not valid within the WebFlow ConfigSet '"
                                                + configSet.getElementName()
                                                + "'. Specified bean with id '"
                                                + bean
                                                + "' can not be found in associated Beans ConfigSet '"
                                                + beansConfigSet
                                                        .getElementName()
                                                + "'",
                                        IMarker.SEVERITY_ERROR,
                                        reference.getElementStartLine(),
                                        IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
                    }
                }
            }
        }
    }
}
