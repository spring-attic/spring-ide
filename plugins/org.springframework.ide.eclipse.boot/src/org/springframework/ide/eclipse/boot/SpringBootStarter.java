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
package org.springframework.ide.eclipse.boot;

import org.eclipse.core.runtime.Assert;

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
	
	private String name;
	private IMavenCoordinates dep;

	public SpringBootStarter(IMavenCoordinates dep) {
		Assert.isTrue(dep.getArtifactId().startsWith(AID_PREFIX));
		this.name = dep.getArtifactId().substring(AID_PREFIX.length());
		this.dep = dep;
	}
	
	public String getName() {
		return name;
	}

	public IMavenCoordinates getDep() {
		return dep;
	}
	
	public static boolean isStarter(IMavenCoordinates dep) {
		String id = dep.getArtifactId();
		return id!=null && id.startsWith(AID_PREFIX);
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
	

}
