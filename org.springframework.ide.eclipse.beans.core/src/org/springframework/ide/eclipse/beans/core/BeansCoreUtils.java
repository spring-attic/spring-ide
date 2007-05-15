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
package org.springframework.ide.eclipse.beans.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker.ErrorCode;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 */
public class BeansCoreUtils {

	/**
	 * Returns <code>true</code> if given resource is a Spring bean factory
	 * config file.
	 */
	public static boolean isBeansConfig(IResource resource) {
		if (resource instanceof IFile) {
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					resource.getProject());
			if (project != null) {
				return project.hasConfig((IFile) resource);
			}
		}
		return false;
	}

	public static void createProblemMarker(IResource resource, String message,
			int severity, int line, ErrorCode errorCode) {
		createProblemMarker(resource, message, severity, line, errorCode, null,
				null);
	}

	public static void createProblemMarker(IResource resource, String message,
			int severity, int line, ErrorCode errorCode, String beanID,
			String errorData) {
		if (resource != null && resource.isAccessible()) {
			try {

				// First check if specified marker already exists
				IMarker[] markers = resource.findMarkers(
						IBeansProjectMarker.PROBLEM_MARKER, false,
						IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (l == line) {
						String msg = marker.getAttribute(IMarker.MESSAGE, "");
						if (msg.equals(message)) {
							return;
						}
					}
				}

				// Create new marker
				IMarker marker = resource
						.createMarker(IBeansProjectMarker.PROBLEM_MARKER);
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, message);
				attributes.put(IMarker.SEVERITY, new Integer(severity));
				if (line > 0) {
					attributes.put(IMarker.LINE_NUMBER, new Integer(line));
				}
				if (errorCode != ErrorCode.NONE) {
					attributes.put(IBeansProjectMarker.ERROR_CODE, errorCode);
				}
				if (beanID != null) {
					attributes.put(IBeansProjectMarker.BEAN_ID, beanID);
				}
				if (errorData != null) {
					attributes.put(IBeansProjectMarker.ERROR_DATA, errorData);
				}
				marker.setAttributes(attributes);
			} catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
		}
	}
}
