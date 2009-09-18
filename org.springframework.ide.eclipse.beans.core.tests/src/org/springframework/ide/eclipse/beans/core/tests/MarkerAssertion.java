/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.tests;

import static junit.framework.Assert.assertTrue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class MarkerAssertion {

	private String message;

	private String line;

	private String severity;

	public MarkerAssertion(String message, int line) {
		this(message, line, IMarker.SEVERITY_ERROR);
	}

	public MarkerAssertion(String message, int line, int severity) {
		this.message = message;
		this.line = String.valueOf(line);
		this.severity = String.valueOf(severity);
	}

	public boolean doesMatch(IMarker marker) {
		try {
			return message.equals(marker.getAttribute(IMarker.MESSAGE))
					&& line.equals(String.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, -1)))
					&& severity.equals(String.valueOf(marker.getAttribute(IMarker.SEVERITY)));
		}
		catch (CoreException e) {
		}
		return false;
	}

	public static void assertMarker(IMarker[] markers, MarkerAssertion[] markerAssertions) {
		for (IMarker marker : markers) {
			try {
				System.out.println("Marker: " + marker.getAttribute(IMarker.MESSAGE) + " ("
						+ String.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, -1)) + ")");
			}
			catch (CoreException e) {
			}
		}
		for (MarkerAssertion markerAssertion : markerAssertions) {
			boolean found = false;
			for (IMarker marker : markers) {
				if (markerAssertion.doesMatch(marker)) {
					found = true;
					break;
				}
			}
			assertTrue("Marker '" + markerAssertion.message + "' at line '" + markerAssertion.line + "' not found",
					found);
		}
	}
}