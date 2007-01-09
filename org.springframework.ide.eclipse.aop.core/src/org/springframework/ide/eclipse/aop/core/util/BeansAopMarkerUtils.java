/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.core.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.core.SpringCore;

@SuppressWarnings("restriction")
public class BeansAopMarkerUtils {

    private static final String SOURCE_RESOURCE = "source_resource";

    private static final String MARKER_COUNT = "marker_count";

    public static final String AOP_MARKER = Activator.PLUGIN_ID
            + ".aopmarker";

    public static final String AOP_PROBLEM_MARKER = Activator.PLUGIN_ID
            + ".aopproblemmarker";

    public static final String BEFORE_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".beforeadvicemarker";

    public static final String AFTER_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".afteradvicemarker";

    public static final String AROUND_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".aroundadvicemarker";

    public static final String SOURCE_BEFORE_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".sourcebeforeadvicemarker";

    public static final String SOURCE_AFTER_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".sourceafteradvicemarker";

    public static final String SOURCE_AROUND_ADVICE_MARKER = Activator.PLUGIN_ID
            + ".sourcearoundadvicemarker";

    public static final String ADIVCE_TYPE = "adivice_type";

    public static Map<ADVICE_TYPES, String> sourceMarkerMapping;

    public static Map<ADVICE_TYPES, String> targetMarkerMapping;

    static {
        sourceMarkerMapping = new HashMap<ADVICE_TYPES, String>();
        sourceMarkerMapping.put(ADVICE_TYPES.BEFORE,
                SOURCE_BEFORE_ADVICE_MARKER);
        sourceMarkerMapping.put(ADVICE_TYPES.AFTER, SOURCE_AFTER_ADVICE_MARKER);
        sourceMarkerMapping.put(ADVICE_TYPES.AFTER_RETURNING,
                SOURCE_AFTER_ADVICE_MARKER);
        sourceMarkerMapping.put(ADVICE_TYPES.AFTER_THROWING,
                SOURCE_AFTER_ADVICE_MARKER);
        sourceMarkerMapping.put(ADVICE_TYPES.AROUND,
                SOURCE_AROUND_ADVICE_MARKER);

        targetMarkerMapping = new HashMap<ADVICE_TYPES, String>();
        targetMarkerMapping.put(ADVICE_TYPES.BEFORE, BEFORE_ADVICE_MARKER);
        targetMarkerMapping.put(ADVICE_TYPES.AFTER, AFTER_ADVICE_MARKER);
        targetMarkerMapping.put(ADVICE_TYPES.AFTER_RETURNING,
                AFTER_ADVICE_MARKER);
        targetMarkerMapping.put(ADVICE_TYPES.AFTER_THROWING,
                AFTER_ADVICE_MARKER);
        targetMarkerMapping.put(ADVICE_TYPES.AROUND, AROUND_ADVICE_MARKER);
    }

    public static void createMarker(IAopReference reference,
            IResource sourceResource) {
        createSourceMarker(reference, sourceMarkerMapping.get(reference
                .getAdviceType()), sourceResource);
        createTargetMarker(reference, targetMarkerMapping.get(reference
                .getAdviceType()), sourceResource);
    }

    public static void createTargetMarker(IAopReference reference,
            String markerId, IResource sourceResource) {
        createProblemMarker(reference.getTarget().getResource(), "adviced by "
                + BeansAopUtils.getJavaElementLinkName(reference.getSource()),
                1, BeansAopUtils.getLineNumber(reference.getTarget()),
                markerId, sourceResource);
    }

    public static void createSourceMarker(IAopReference reference,
            String markerId, IResource sourceResource) {
        if (reference.getDefinition().getAspectLineNumber() > 0
                && !(reference.getDefinition() instanceof AnnotationAspectDefinition)) {
            createProblemMarker(reference.getDefinition().getResource(), "advises "
                    + BeansAopUtils.getJavaElementLinkName(reference
                            .getTarget()), 1, reference.getDefinition()
                    .getAspectLineNumber(), markerId, sourceResource);
        }
        createProblemMarker(reference.getSource().getResource(), "advises "
                + BeansAopUtils.getJavaElementLinkName(reference.getTarget()),
                1, BeansAopUtils.getLineNumber(reference.getSource()),
                markerId, sourceResource);
    }

    public static void deleteProblemMarkers(IResource resource) {
        if (resource != null && resource.isAccessible()) {
            try {
                resource.deleteMarkers(BeansAopMarkerUtils.AOP_PROBLEM_MARKER,
                        true, IResource.DEPTH_INFINITE);

                IProject project = resource.getProject();
                String resourceName = resource.getProjectRelativePath()
                        .toString();
                IMarker[] markers = project.findMarkers(
                        BeansAopMarkerUtils.AOP_MARKER, true,
                        IResource.DEPTH_INFINITE);
                for (IMarker marker : markers) {
                    String sourceResourceName = marker.getAttribute(
                            SOURCE_RESOURCE, resource.getProjectRelativePath()
                                    .toString());
                    if (resourceName.equals(sourceResourceName)) {
                        marker.delete();
                    }
                }
            }
            catch (CoreException e) {
                SpringCore.log(e);
            }
        }
    }

    public static void createProblemMarker(IResource resource, String message,
            int severity, int line, String markerId, IResource sourceResource) {
        createProblemMarker(resource, message, severity, line, markerId, 1,
                sourceResource);
    }

    public static void createProblemMarker(IResource resource, String message,
            int severity, int line, String markerId, int markerCount,
            IResource sourceResource) {
        if (resource != null && resource.isAccessible()) {
            try {
                // First check if specified marker already exists
                
                if (severity == IMarker.SEVERITY_ERROR) {
                    IMarker[] markers = resource.findMarkers(
                            BeansAopMarkerUtils.AOP_PROBLEM_MARKER, true,
                            IResource.DEPTH_ZERO);
                    for (IMarker marker : markers) {
                        int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                        String msg = marker.getAttribute(IMarker.MESSAGE, "");
                        if (l == line && message.equals(msg)) {
                            return;
                        }
                    }
                }
                
                IMarker[] markers = resource.findMarkers(
                        BeansAopMarkerUtils.AOP_MARKER, true,
                        IResource.DEPTH_ZERO);
                for (IMarker marker : markers) {
                    int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                    int count = marker.getAttribute(MARKER_COUNT, 1);
                    String msg = marker.getAttribute(IMarker.MESSAGE, "");
                    count++;
                    if (l == line && message.equals(msg)) {
                        return;
                    }
                    if (l == line && marker.getType() != markerId) {
                        resource.findMarker(marker.getId()).delete();
                        createProblemMarker(resource, count
                                + " Spring AOP marker at this line", 1, line,
                                BeansAopMarkerUtils.AOP_MARKER, count,
                                sourceResource);
                        return;
                    }
                    else if (l == line && marker.getType() == markerId) {
                        marker.setAttribute(IMarker.MESSAGE, count
                                + " Spring AOP marker at this line");
                        marker.setAttribute(MARKER_COUNT, count);
                        return;
                    }
                }

                // Create new marker
                IMarker marker = resource.createMarker(markerId);
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put(IMarker.MESSAGE, message);
                attributes.put(IMarker.SEVERITY, new Integer(severity));
                attributes.put(MARKER_COUNT, markerCount);
                if (sourceResource != null) {
                    attributes.put(SOURCE_RESOURCE, sourceResource
                            .getProjectRelativePath().toString());
                }
                if (line > 0) {
                    attributes.put(IMarker.LINE_NUMBER, new Integer(line));
                }
                marker.setAttributes(attributes);
            }
            catch (CoreException e) {
                SpringCore.log(e);
            }
        }
    }

}
