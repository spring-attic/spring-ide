/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.util.Arrays;

/**
 * Implementation that can hold type hierarchy information about Java class files.
 * Used to check if classes are related to each other in terms of their types.
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
public class TypeHierarchyElement {
	
	public final char[] className;
	public final char[] superclassName;
	public TypeHierarchyElement superclassElement; // lazy set
	
	public final char[][] interfaces;
	public TypeHierarchyElement[] interfacesElements; // array initialized, but elements being set lazy
	
	public TypeHierarchyElement(char[] className, char[] superclassName, char[][] interfaces) {
		super();
		this.className = className;
		this.superclassName = superclassName;
		
		this.interfaces = interfaces;
		if (this.interfaces != null) {
			this.interfacesElements = new TypeHierarchyElement[this.interfaces.length];
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(className);
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
		TypeHierarchyElement other = (TypeHierarchyElement) obj;
		if (!Arrays.equals(className, other.className))
			return false;
		return true;
	}

}
