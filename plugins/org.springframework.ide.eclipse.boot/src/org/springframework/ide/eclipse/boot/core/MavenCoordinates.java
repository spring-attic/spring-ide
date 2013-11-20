/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;

public class MavenCoordinates implements IMavenCoordinates, Ilabelable {

	private final String group;
	private final String artifact;
	private final String version;
	
	public MavenCoordinates(String group, String artifact, String version) {
		this.group = group;
		this.artifact = artifact;
		this.version = version;
	}

	@Override
	public String getGroupId() {
		return group;
	}
	
	@Override
	public String getArtifactId() {
		return artifact;
	}
	
	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MavenCoordinates other = (MavenCoordinates) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return group + ":"+ artifact+":"+version;
	}

	@Override
	public String getLabel() {
		return artifact;
	}
}
