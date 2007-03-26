/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Holds a literal (string value wrapped in {@link TypedStringValue}).
 * 
 * @author Torsten Juergeleit
 */
public class BeansTypedString extends AbstractBeansModelElement implements
		IBeansTypedString {

	private String value;
	private String targetTypeName;

	public BeansTypedString(IModelElement parent,
			TypedStringValue stringValue) {
		super(parent, "(typed string)", stringValue);
		value = stringValue.getValue();
		targetTypeName = stringValue.getTargetTypeName();
	}

	public BeansTypedString(ISourceModelElement parent, String stringValue) {
		super(parent, "(typed string)", parent.getElementSourceLocation());
		value = stringValue;
	}

	public int getElementType() {
		return IBeansModelElementTypes.TYPED_STRING_TYPE;
	}

	public String getString() {
		return value;
	}

	public String getTargetTypeName() {
		return targetTypeName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansTypedString)) {
			return false;
		}
		BeansTypedString that = (BeansTypedString) other;
		if (!ObjectUtils.nullSafeEquals(this.value, that.value)) return false;
		if (!ObjectUtils.nullSafeEquals(this.targetTypeName,
				that.targetTypeName)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(value);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(targetTypeName);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": value=");
		text.append(value);
		if (StringUtils.hasText(targetTypeName)) {
			text.append(" (");
			text.append(targetTypeName);
			text.append(')');
		}
		return text.toString();
	}
}
