/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html).
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.eclipse.wizard.gettingstarted.guides.ext;

import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;

/**
 * @author Miles Parker
 *
 */
public class OpenGSWizardButtonProvider extends IEclipseToBrowserFunction.Extension {

	private static final String LF = "\n";

	private String getGuideA(String name) {
		return "<a class=\"ide_widget btn btn-black uppercase\" href=\"\" onclick=\"ide.call('org.springframework.openGuideWizard','"
				+ name + "')\">Import " + name + "</a>";
	}

	@Override
	public String getDynamicArgumentValue(String id) {
		if (id.equals("html")) {
			return getGuideA("Getting Started Guide") + LF + getGuideA("Tutorial Guide") + LF + getGuideA("Reference App") + LF;
		}
		return null;
	}
}
