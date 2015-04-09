/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

/**
 * @author Kris De Volder
 */
public class SpringPropertyProblem {

	private String msg;
	private int length;
	private int offset;

	private String severity = SpringPropertyAnnotation.ERROR_TYPE;

	/**
	 * Create a SpringProperty file annotation with a given severity.
	 * The severity should be one of the XXX_TYPE constants defined in
	 * {@link SpringPropertyAnnotation}.
	 */
	private SpringPropertyProblem(String severity, String msg, int offset, int length) {
		this.msg = msg;
		this.offset = offset;
		this.length = length;
		this.severity = severity;
	}

	public String getMessage() {
		return msg;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "@["+offset+","+length+"]: "+msg;
	}

	public String getSeverity() {
		return severity;
	}

	public static SpringPropertyProblem error(String message, int offset, int len) {
		return new SpringPropertyProblem(SpringPropertyAnnotation.ERROR_TYPE, message, offset, len);
	}
	public static SpringPropertyProblem warning(String message, int offset, int len) {
		return new SpringPropertyProblem(SpringPropertyAnnotation.WARNING_TYPE, message, offset, len);
	}

}
