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

/**
 * Represents a released Spring version. A Default value also exists which
 * indicates that the default spring version as defined in a template should be
 * used.
 * 
 */
public class SpringVersion {

	private final String fullVersion;

	private final String display;

	public static final SpringVersion DEFAULT = new SpringVersion("Default");

	public SpringVersion(String display) {
		this.display = display;
		this.fullVersion = display + ".RELEASE";
	}

	public String getDisplay() {
		return display;
	}

	public String getVersion() {
		return fullVersion;
	}

	/**
	 * Returns RELEASE versions of Spring only.
	 * @return
	 */
	public static List<SpringVersion> getVersions() {
		List<SpringVersion> versions = new ArrayList<SpringVersion>();
		// TODO: Hardcoded for now. Externalise, or find a way to read supported
		// release versions
		versions.add(DEFAULT);
		versions.add(new SpringVersion("3.2.2"));
		versions.add(new SpringVersion("3.1.4"));
		versions.add(new SpringVersion("3.1.1"));
		versions.add(new SpringVersion("3.0.7"));
		versions.add(new SpringVersion("2.5.6"));

		return versions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((display == null) ? 0 : display.hashCode());
		result = prime * result + ((fullVersion == null) ? 0 : fullVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpringVersion other = (SpringVersion) obj;
		if (display == null) {
			if (other.display != null) {
				return false;
			}
		}
		else if (!display.equals(other.display)) {
			return false;
		}
		if (fullVersion == null) {
			if (other.fullVersion != null) {
				return false;
			}
		}
		else if (!fullVersion.equals(other.fullVersion)) {
			return false;
		}
		return true;
	}

}
