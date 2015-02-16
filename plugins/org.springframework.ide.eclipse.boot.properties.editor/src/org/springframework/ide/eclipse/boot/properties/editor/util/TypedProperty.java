/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

/**
 * Represents a property on a Type that can be accessed by name.
 *
 * @author Kris De Volder
 */
public class TypedProperty {

	/**
	 * The name of the property
	 */
	private final String name;

	/**
	 * The type of value associated with the property.
	 */
	private final Type type;

	public TypedProperty(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return name + "::" + type;
	}
}
