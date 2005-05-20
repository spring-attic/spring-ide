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

package org.springframework.ide.eclipse.web.flow.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class WebFlowCoreUtils {

    /**
     * Returns true if given resource is a Spring bean factory config file.
     */
    public static boolean isWebFlowConfig(IResource resource) {
        if (resource instanceof IFile && resource.isAccessible()) {
            IWebFlowProject project = WebFlowCorePlugin.getModel().getProject(
                    resource.getProject());
            if (project != null) {
                return project.hasConfig((IFile) resource);
            }
        }
        return false;
    }

    public static void createProblemMarker(IFile file, String message,
            int severity, int line, int errorCode) {
        createProblemMarker(file, message, severity, line, errorCode, null,
                null);
    }

    public static void createProblemMarker(IFile file, String message,
            int severity, int line, int errorCode, String beanID,
            String errorData) {
        if (file != null && file.isAccessible()) {
            try {

                // First check if specified marker already exists
                IMarker[] markers = file.findMarkers(
                        IWebFlowProjectMarker.PROBLEM_MARKER, false,
                        IResource.DEPTH_ZERO);
                for (int i = 0; i < markers.length; i++) {
                    IMarker marker = markers[i];
                    int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                    if (l == line) {
                        String msg = marker.getAttribute(IMarker.MESSAGE, "");
                        if (msg.equals(message)) {
                            return;
                        }
                    }
                }

                // Create new marker
                IMarker marker = file
                        .createMarker(IWebFlowProjectMarker.PROBLEM_MARKER);
                Map attributes = new HashMap();
                attributes.put(IMarker.MESSAGE, message);
                attributes.put(IMarker.SEVERITY, new Integer(severity));
                if (line > 0) {
                    attributes.put(IMarker.LINE_NUMBER, new Integer(line));
                }
                if (errorCode != 0) {
                    attributes.put(IWebFlowProjectMarker.ERROR_CODE,
                            new Integer(errorCode));
                }
                if (errorData != null) {
                    attributes.put(IWebFlowProjectMarker.ERROR_DATA, errorData);
                }
                marker.setAttributes(attributes);
            }
            catch (CoreException e) {
                WebFlowCorePlugin.log(e);
            }
        }
    }

    public static void deleteProblemMarkers(IFile file) {
        if (file != null && file.isAccessible()) {
            try {
                file.deleteMarkers(IWebFlowProjectMarker.PROBLEM_MARKER, false,
                        IResource.DEPTH_ZERO);
            }
            catch (CoreException e) {
                WebFlowCorePlugin.log(e);
            }
        }
    }

    public static boolean isIdAlreadyChoosenByAnotherState(
            IWebFlowModelElement parent, IState state, String newId) {
        boolean choosen = false;
        if (parent instanceof IWebFlowState) {
            List states = ((IWebFlowState) parent).getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (newId.equals(eState.getId())
                        && !eState.getElementName().equals(
                                state.getElementName())) {
                    choosen = true;
                }
            }
        }
        return choosen;
    }

    public static boolean existsStateById(IWebFlowModelElement parent,
            String newId) {
        boolean choosen = false;
        if (parent instanceof IWebFlowState) {
            List states = ((IWebFlowState) parent).getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (newId.equals(eState.getId())) {
                    choosen = true;
                }
            }
        }
        else if (parent instanceof IState) {
            List states = ((IWebFlowState) parent.getElementParent())
                    .getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (newId.equals(eState.getId())) {
                    choosen = true;
                }
            }
        }
        return choosen;
    }

    public static IState getStateById(IWebFlowModelElement parent, String newId) {
        IState choosen = null;
        if (parent instanceof IWebFlowState) {
            List states = ((IWebFlowState) parent).getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (newId.equals(eState.getId())) {
                    choosen = eState;
                }
            }
        }
        else if (parent instanceof IState) {
            List states = ((IWebFlowState) parent.getElementParent())
                    .getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (newId.equals(eState.getId())) {
                    choosen = eState;
                }
            }
        }
        return choosen;
    }

    public static List getStatesWithoutParent(IWebFlowModelElement state) {
        List returnStates = new ArrayList();
        if (state instanceof IWebFlowState) {
            List states = ((IWebFlowState) state).getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (!eState.equals(state)) {
                    returnStates.add(eState);
                }
            }
        }
        else if (state instanceof IState) {
            List states = ((IWebFlowState) state.getElementParent())
                    .getStates();
            for (int i = 0; i < states.size(); i++) {
                IState eState = (IState) states.get(i);
                if (!eState.equals(state)
                        && !eState.getId().equals(((IState) state).getId())) {
                    returnStates.add(eState);
                }
            }
        }
        return returnStates;
    }
}