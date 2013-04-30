/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Terry Denney
 */
public class LocationInformation {

	private final int offset;

	private final int length;

	private final String filter;

	private final boolean quoted;

	private final StringLiteral stringLiteral;

	public LocationInformation(int offset, int length, String filter, StringLiteral stringLiteral) {
		this(offset, length, filter, false, stringLiteral);
	}

	public LocationInformation(int offset, int length, String filter, boolean quoted, StringLiteral stringLiteral) {
		this.offset = offset;
		this.length = length;
		this.filter = filter;
		this.quoted = quoted;
		this.stringLiteral = stringLiteral;
	}

	public String getFilter() {
		return filter;
	}

	public boolean isValid() {
		return offset >= 0 && length >= 0;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public StringLiteral getStringLiteral() {
		return stringLiteral;
	}

	public boolean isQuoted() {
		return quoted;
	}
}
