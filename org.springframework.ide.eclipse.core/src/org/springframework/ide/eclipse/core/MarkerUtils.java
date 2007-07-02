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
package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Some {@link IMarker} helper methods.
 * 
 * @author Torsten Juergeleit
 */
public final class MarkerUtils {

	public static int getHighestSeverityFromMarkersInRange(IResource resource,
			int startLine, int endLine) {
		int severity = -1;
		try {
			// TODO CD change this to SpringCore.MARKER_ID or IMarker.PROBLEM
			IMarker[] markers = resource.findMarkers(SpringCore.MARKER_ID, true,
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
