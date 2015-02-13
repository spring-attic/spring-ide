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
 * @author Kris De Volder
 */
public class Type {
	private final String erasure;
	private final Type[] params;

	public Type(String erasure, Type[] params) {
		this.erasure = erasure;
		this.params = params;
	}

	public boolean isGeneric() {
		return params!=null;
	}

	public String getErasure() {
		return erasure;
	}
	public Type[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		toString(buf);
		return buf.toString();
	}

	private void toString(StringBuilder buf) {
		buf.append(getErasure());
		if (isGeneric()) {
			buf.append("<");
			boolean first = true;
			for (Type param : getParams()) {
				if (!first) {
					buf.append(",");
				}
				param.toString(buf);
				first = false;
			}
			buf.append(">");
		}
	}
}