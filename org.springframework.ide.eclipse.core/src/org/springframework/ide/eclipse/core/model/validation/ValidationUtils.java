/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Validation helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 2.0
 */
public final class ValidationUtils {

	/**
	 * Creates the {@link IMarker validation markers} on the specified resource for the given validation problems.
	 */
	public static void createProblemMarkers(IResource resource, Set<ValidationProblem> problems, String markerId) {
		if (problems != null) {
			for (ValidationProblem problem : problems) {
				// Don't install problems that are configured to get ignored
				if (problem.getSeverity() >= 0) {
					createProblemMarker(resource, problem, markerId);
				}
			}
		}
	}

	/**
	 * Creates an {@link IMarker validation marker} on the specified resource for the given validation problem.
	 * <p>
	 * Adds the originating resource as marker attribute with the key {@link MarkerUtils#ORIGINATING_RESOURCE_KEY}.
	 */
	public static void createProblemMarker(IResource resource, ValidationProblem problem, String markerId) {

		// Use resource used during reporting of the problem as this might be
		// more concise.
		IResource originatingResource = resource;
		if (problem.getResource() != null) {
			resource = problem.getResource();
		}

		if (resource != null && resource.isAccessible()) {
			try {

				// First check if specified marker already exists
				IMarker[] markers = resource.findMarkers(markerId, false, IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (line == problem.getLine()) {
						String msg = marker.getAttribute(IMarker.MESSAGE, "");
						if (msg.equals(problem.getMessage())) {
							return;
						}
					}
				}

				// Create new marker
				IMarker marker = resource.createMarker(markerId);
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, problem.getMessage());
				attributes.put(IMarker.SEVERITY, new Integer(problem.getSeverity()));

				// Store the originating resource reference in marker so that
				// the marker can later on be deleted with a reference from the
				// initial resource
				attributes.put(MarkerUtils.ORIGINATING_RESOURCE_KEY, originatingResource.getFullPath().toString());
				if (problem.getLine() > 0) {
					attributes.put(IMarker.LINE_NUMBER, new Integer(problem.getLine()));
				}
				if (problem.getErrorId() != null) {
					attributes.put(IValidationProblemMarker.ERROR_ID, problem.getErrorId());
				}
				if (problem.getRuleId() != null) {
					attributes.put(IValidationProblemMarker.RULE_ID, problem.getRuleId());
				}

				// Add validation attributes to the list of marker attributes
				for (ValidationProblemAttribute attribute : problem.getAttributes()) {
					Object value = attribute.getValue();

					// Marker can only handle value of type String, Integer or Boolean
					if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
						attributes.put(attribute.getKey(), value);
					} else if (value instanceof List<?>) {
						int counter = 1;
						for(Object v: (List<?>) value) {
							if (v instanceof String || value instanceof Integer || value instanceof Boolean) {
								attributes.put(attribute.getKey() + counter, v);
								counter++;
							}
						}
					}
				}

				marker.setAttributes(attributes);
			}
			catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}
}
