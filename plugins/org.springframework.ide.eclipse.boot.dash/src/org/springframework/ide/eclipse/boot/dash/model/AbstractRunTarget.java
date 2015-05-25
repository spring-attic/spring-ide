/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

public abstract class AbstractRunTarget implements RunTarget {

	private String id;
	private String name;

	public AbstractRunTarget(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public AbstractRunTarget(String idAndName) {
		this(idAndName, idAndName);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RunTarget("+id+")";
	}


}
