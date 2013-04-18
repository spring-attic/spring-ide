/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.ArrayList;
import java.util.List;

public class SpringVersion {

	private final double version;

	public SpringVersion(double version) {
		this.version = version;
	}

	public double getVersion() {
		return version;
	}

	public static List<SpringVersion> getVersions() {
		List<SpringVersion> versions = new ArrayList<SpringVersion>();

		versions.add(new SpringVersion(3.1));
		versions.add(new SpringVersion(4.0));
		return versions;
	}

}
