/*******************************************************************************
 * Copyright (c) 2008, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Holder for annotation meta data.
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.5
 */
public class Annotation {

	private String annotationClass;

	private Set<AnnotationMemberValuePair> members;

	public Annotation(String annotationClass) {
		this.annotationClass = annotationClass;
		this.members = new LinkedHashSet<AnnotationMemberValuePair>();
	}

	public void addMember(AnnotationMemberValuePair member) {
		this.members.add(member);
	}
	
	public boolean hasMember(String name) {
		for (AnnotationMemberValuePair pair : this.members) {
			if (name != null && name.equals(pair.getName())) {
				return true;
			}
		}
		return false;
	}

	public String getAnnotationClass() {
		return annotationClass;
	}

	public Set<AnnotationMemberValuePair> getMembers() {
		return members;
	}
}