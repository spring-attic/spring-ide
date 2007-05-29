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
package org.springframework.ide.eclipse.core.model.validation;

import org.springframework.util.ObjectUtils;

/**
 * This class holds key-value pairs used in a {@link ValidationProblem}.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ValidationProblemAttribute {

	private String key;
	private String value;

	public ValidationProblemAttribute(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(key);
		hashCode = 2 * ObjectUtils.nullSafeHashCode(value);
		return 3 * hashCode + super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ValidationProblemAttribute)) {
			return false;
		}
		ValidationProblemAttribute that = (ValidationProblemAttribute) other;
		if (!ObjectUtils.nullSafeEquals(this.key, that.key)) return false;
		if (!ObjectUtils.nullSafeEquals(this.value, that.value)) return false;
		return super.equals(other);
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}
}
