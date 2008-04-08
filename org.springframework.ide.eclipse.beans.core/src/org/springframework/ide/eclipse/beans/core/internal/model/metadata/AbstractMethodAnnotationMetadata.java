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
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.io.Serializable;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.model.metadata.IMethodMetadata;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Abstract base method annotation meta data implementation.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractMethodAnnotationMetadata implements IMethodMetadata, Serializable {

	private static final long serialVersionUID = -825792015551282251L;

	private String key;

	private String handle;

	private Object value;

	private IModelSourceLocation location;

	public AbstractMethodAnnotationMetadata(String key, String handle, Object value,
			IModelSourceLocation location) {
		this.handle = handle;
		this.value = value;
		this.location = location;
		this.key = key;
	}

	public String getMethodHandle() {
		return this.handle;
	}
	
	public String getHandleIdentifier() {
		return this.handle;
	}

	public IModelSourceLocation getElementSourceLocation() {
		return location;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public String getValueAsText() {
		if (value instanceof Set) {
			StringBuilder buf = new StringBuilder();
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) value) {
				if (pair.getName() != null) {
					buf.append(pair.getName());
					buf.append(" = ");
				}
				buf.append(pair.getValue().toString());
				buf.append(", ");
			}

			if (buf.length() > 0) {
				return buf.substring(0, buf.length() - 2) + " - ";
			}
			return "";
		}
		return "";
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractMethodAnnotationMetadata)) {
			return false;
		}
		AbstractMethodAnnotationMetadata that = (AbstractMethodAnnotationMetadata) other;
		if (!ObjectUtils.nullSafeEquals(this.value, that.value))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.handle, that.handle))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.key, that.key))
			return false;
		 return ObjectUtils.nullSafeEquals(this.location, that.location);
	}
	
	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(value);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(handle);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(key);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(location);
		return 9 * hashCode;
	}

}
