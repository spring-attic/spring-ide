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

	/**
	 * ArtifactId prefix to recognize when a dependency is a spring-boot-starter.
	 */
	public static final String AID_PREFIX = "spring-boot-starter-";

	public static final String SB_PREFIX = "spring-boot-";

	private String id; //id used by initalizr service
	private MavenId dep;

	public SpringBootStarter(String id, MavenId dep) {
		this.id = id;
		this.dep = dep;
	}

	public MavenId getDep() {
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
		return "SpringBootStarter("+id+", "+dep+")";
	}

	public String getArtifactId() {
		return getDep().getArtifactId();
	}

	public String getGroupId() {
		return getDep().getGroupId();
	}

	public String getName() {
		//TODO: Confusing name!
		return id;
	}

	/**
	 * GroupId + ArtifactId, can be used as a key in a map of SpringBootStarter objects.
	 * Typically the gid + aid will identify the starter. The version is 'fixed' within
	 * a project so it isn't part of the 'id'.
	 */
	public MavenId getId() {
		//TODO: Confusing name!
		return new MavenId(getGroupId(), getArtifactId());
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

}
