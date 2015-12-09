/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

/**
 * A 'SpringBootStarter is maven style dependency that can be added to
 * a boot enabled project.
 *
 * @author Kris De Volder
 */
public class SpringBootStarter {

	private String id; //id used by initalizr service
	private MavenId mavenId;
	private String scope;
	private IMavenCoordinates bom;

	public SpringBootStarter(String id, MavenId dep, String scope, IMavenCoordinates bom) {
		this.id = id;
		this.mavenId = dep;
		this.scope = scope;
		this.bom = bom;
	}

	public String getScope() {
		return scope;
	}

	@Override
	public String toString() {
		return "SpringBootStarter("+id+", "+mavenId+")";
	}

	public String getArtifactId() {
		return getMavenId().getArtifactId();
	}

	public String getGroupId() {
		return getMavenId().getGroupId();
	}

	public String getId() {
		return id;
	}

	/**
	 * GroupId + ArtifactId, can be used as a key in a map of SpringBootStarter objects.
	 * Typically the gid + aid will identify the starter. The version is 'fixed' within
	 * a project so it isn't part of the 'id'.
	 */
	public MavenId getMavenId() {
		return mavenId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mavenId == null) ? 0 : mavenId.hashCode());
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
		SpringBootStarter other = (SpringBootStarter) obj;
		if (mavenId == null) {
			if (other.mavenId != null)
				return false;
		} else if (!mavenId.equals(other.mavenId))
			return false;
		return true;
	}

	public IMavenCoordinates getBom() {
		return bom;
	}
}
