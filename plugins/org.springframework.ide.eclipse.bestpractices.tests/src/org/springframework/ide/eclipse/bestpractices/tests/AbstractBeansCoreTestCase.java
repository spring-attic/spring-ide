/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.bestpractices.tests;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

/**
 * @author Wesley Coelho
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.0.3
 */
public abstract class AbstractBeansCoreTestCase extends StsTestCase {

	/**
	 * Checks if the given message text appears in any of the specified markers.
	 * @return true if the marker text is found
	 */
	protected void assertHasMarkerWithText(IMarker[] markers, String messageText) throws CoreException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < markers.length; i++) {
			IMarker currMarker = markers[i];
			String message = (String) currMarker.getAttribute("message");
			if (message != null && message.indexOf(messageText) >= 0) {
				return;
			}
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(message);
		}
		fail("Expected '" + messageText + "' in '" + sb.toString() + "'.");
	}

	protected void assertNotHasMarkerWithText(IMarker[] markers, String messageText) throws CoreException {
		boolean failed = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < markers.length; i++) {
			IMarker currMarker = markers[i];
			String message = (String) currMarker.getAttribute("message");
			if (message != null && message.indexOf(messageText) >= 0) {
				failed = true;
			}
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(message);
		}
		if (failed) {
			fail("Did not expect '" + messageText + "' in '" + sb.toString() + "'.");
		}
	}

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.bestpractices.tests";
	}

}
