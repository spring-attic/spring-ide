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

package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Some {@link IMarker}Êhelper methods.
 * 
 * @author Torsten Juergeleit
 */
public final class MarkerUtils {

	public static int getHighestSeverityFromMarkersInRange(IResource resource,
			int startLine, int endLine) {
		int severity = -1;
		try {
			IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (startLine == -1
						|| isMarkerInRange(marker, startLine, endLine)) {
					int sev = marker.getAttribute(IMarker.SEVERITY, -1);
					if (sev == IMarker.SEVERITY_WARNING) {
						severity = sev;
					}
					else if (sev == IMarker.SEVERITY_ERROR) {
						severity = sev;
						break;
					}
				}
			}
		}
		catch (CoreException e) {
			// ignore
		}
		return severity;
	}

	public static boolean isMarkerInRange(IMarker marker, int startLine,
			int endLine) throws CoreException {
		if (startLine >= 0 && endLine >= startLine
				&& marker.isSubtypeOf(IMarker.TEXT)) {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			return (line >= startLine && line <= endLine);
		}
		return false;
	}

	/**
	 * Removes all {@link IMarker markers} with given id (including the
	 * inherited ones) from given {@link IResource} and it's members.
	 */
    public static void deleteMarkers(IResource resource, String id) {
        if (resource != null && resource.isAccessible()) {
            try {
                resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
            }
            catch (CoreException e) {
                SpringCore.log(e);
            }
        }
    }
}
