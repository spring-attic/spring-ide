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

	private String name;
	private int length;
	private int offset;
	
	public SpringPropertyProblem(String name, int offset, int length) {
		this.name = name;
		this.offset = offset;
		this.length = length;
	}

	public String getMessage() {
		return "Unknown property: "+name;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return name.length();
	}

}
