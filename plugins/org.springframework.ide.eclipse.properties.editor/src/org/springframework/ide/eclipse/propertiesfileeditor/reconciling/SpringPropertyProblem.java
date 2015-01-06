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
package org.springframework.ide.eclipse.propertiesfileeditor.reconciling;

/**
 * @author Kris De Volder
 */
public class SpringPropertyProblem {

	private String msg;
	private int length;
	private int offset;
	
	public SpringPropertyProblem(String msg, int offset, int length) {
		this.msg = msg;
		this.offset = offset;
		this.length = length;
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
	
}
