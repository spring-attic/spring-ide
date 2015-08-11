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

import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;

/**
 * A 'SpringBootStarter is maven style dependency that can be added to
 * a boot enabled project.
 *
 * @author Kris De Volder
 */
public class SpringBootStarter implements Ilabelable {

	/**
	 * ArtifactId prefix to recognize when a dependency is a spring-boot-starter.
	 */
	public static final String AID_PREFIX = "spring-boot-starter-";

	public static final String SB_PREFIX = "spring-boot-";

	private String name;
	private IMavenCoordinates dep;

	public SpringBootStarter(IMavenCoordinates dep) {
		String artifact = dep.getArtifactId();
		if (artifact.startsWith(AID_PREFIX)) {
			this.name = artifact.substring(AID_PREFIX.length());
		} else if (artifact.startsWith(SB_PREFIX)){
			this.name = artifact.substring(SB_PREFIX.length());
		} else {
			this.name = artifact;
		}
		this.dep = dep;
	}

	public String getName() {
		return name;
	}

	public IMavenCoordinates getDep() {
		return dep;
	}

	public static boolean isStarter(IMavenCoordinates dep) {
		return isStarterAId(dep.getArtifactId());
	}

	public static boolean isStarterAId(String aid) {
		return aid!=null && (
				aid.startsWith(AID_PREFIX) ||
				aid.equals("spring-boot-devtools")
		);
	}

	@Override
	public String toString() {
		return "SpringBootStarter("+getName()+", "+dep.getVersion()+")";
	}

	public String getArtifactId() {
		return getDep().getArtifactId();
	}

	public String getGroupId() {
		return getDep().getGroupId();
	}

	/**
	 * GroupId + ArtifactId, can be used as a key in a map of SpringBootStarter objects.
	 * Typically the gid + aid will identify the starter. The version is 'fixed' within
	 * a project to the spring boot version associated with that project.
	 */
	public StarterId getId() {
		return new StarterId(getGroupId(), getArtifactId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dep == null) ? 0 : dep.hashCode());
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
		if (dep == null) {
			if (other.dep != null)
				return false;
		} else if (!dep.equals(other.dep))
			return false;
		return true;
	}

	/**
	 * Provides a nice label that can be shown UI to identify a starter. By implementing this
	 * interface we don't need to provide custom label providers in many contexts.
	 */
	@Override
	public String getLabel() {
		return getName();
	}

}
