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
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Marker Resolution for converting legacy XML bean definitions to new namespace
 * syntax. Delegates to syntax converter classes to perform the actual
 * modifications to the XML file.
 * Note: This code is not being released as part of the 1.0 distribution because
 * there are more corner cases that need to be considered. The fix is disabled
 * by excluding it from the MarkerResolutionGenerator class. To re-enable it,
 * see revision 722 of MarkerResolutionGenerator.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UseNamespaceSyntaxMarkerResolution implements IMarkerResolution2 {

	private static final String ERROR_ID_KEY = "errorId";

	public String getDescription() {
		return "Change to XML namespace syntax and insert required namespace declaration.";
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		return "Change to XML namespace syntax";
	}

	public void run(IMarker marker) {

		String errorId = "";
		try {
			errorId = (String) marker.getAttribute(ERROR_ID_KEY);

			if (errorId.endsWith("org.springframework.jndi.JndiObjectFactoryBean")) {
				JndiObjectFactorySyntaxConverter converter = new JndiObjectFactorySyntaxConverter();
				converter.convert(marker);
			}
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
			// TODO: Need to inform the user that there was a problem.
			return;
		}
	}

}
