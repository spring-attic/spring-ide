/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class BeansCoreUtils {

	/**
	 * Returns true if given resource is a Spring bean factory config file.
	 */
	public static boolean isBeansConfig(IResource resource) {
		if (resource instanceof IFile && resource.isAccessible()) {
			IBeansProject project = BeansCorePlugin.getModel().getProject(
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
										  int severity, int line, int errorCode,
										  String beanID, String errorData) {
		if (file != null && file.isAccessible()) {
			try {

				// First check if specified marker already exists
				IMarker[] markers = file.findMarkers(
									  IBeansProjectMarker.PROBLEM_MARKER, false,
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
				IMarker marker = file.createMarker(
											IBeansProjectMarker.PROBLEM_MARKER);
				Map attributes = new HashMap();
				attributes.put(IMarker.MESSAGE, message);
				attributes.put(IMarker.SEVERITY, new Integer(severity));
				if (line > 0) {
					attributes.put(IMarker.LINE_NUMBER, new Integer(line));
				}
				if (errorCode != 0) {
					attributes.put(IBeansProjectMarker.ERROR_CODE,
								   new Integer(errorCode));
				}
				if (beanID != null) {
					attributes.put(IBeansProjectMarker.BEAN_ID, beanID);
				}
				if (errorData != null) {
					attributes.put(IBeansProjectMarker.ERROR_DATA,
										errorData);
				}
				marker.setAttributes(attributes);
			} catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	public static void deleteProblemMarkers(IFile file) {
		if (file != null && file.isAccessible()) {
			try {
				file.deleteMarkers(IBeansProjectMarker.PROBLEM_MARKER, false,
								   IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
		}
	}
}
