/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire;

import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * An actual bean reference that was discovered through annotation processing.
 * @author Jared Rodriguez
 * @since 2.0.5
 */
public class AnnotatedBeanReference {

	private String referenceName = null;

	private String className = null;

	private String propertyName = null;

	private IModelSourceLocation location = null;

	/**
	 * @return the location
	 */
	public IModelSourceLocation getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(IModelSourceLocation location) {
		this.location = location;
	}

	public AnnotatedBeanReference() {
	}

	/**
	 * Returns the name of the bean to which this reference points.
	 * 
	 * @return the reference name
	 */
	public String getReferenceName() {
		return referenceName;
	}

	/**
	 * Set the name of the bean to which this reference points.
	 * 
	 * @param referenceName the reference name
	 */
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}

	/**
	 * Get the name of the class represented by the bean to which this reference points.
	 * @return the name of the class
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Set the name of the class represented by the bean to which this reference points.
	 * @param className the name of the class
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Get the property name inside of the source bean which represents the reference.
	 * @return the property name
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * set the property name inside of the source bean which represents the reference.
	 * @param propertyName the name of the property
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public int hashCode() {
		int hcode = 0;
		if (referenceName != null)
			hcode += referenceName.hashCode();
		if (className != null)
			hcode += className.hashCode();
		if (propertyName != null)
			hcode += propertyName.hashCode();
		return hcode;
	}

	private boolean stringRelativeEquals(String s1, String s2) {
		if (s1 != null && s2 != null)
			return s1.equals(s2);
		if (s1 == null && s2 == null)
			return true;
		return false;
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AnnotatedBeanReference))
			return false;

		AnnotatedBeanReference temp = (AnnotatedBeanReference) o;
		boolean isEqual = stringRelativeEquals(referenceName, temp.referenceName);
		isEqual &= stringRelativeEquals(className, temp.className);
		isEqual &= stringRelativeEquals(propertyName, temp.propertyName);
		return isEqual;
	}
}
