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

import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public abstract class AbstractRunTarget implements RunTarget {

	private String id;
	private String name;
	private RunTargetType type;

	public AbstractRunTarget(RunTargetType type, String id, String name) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public AbstractRunTarget(RunTargetType type, String idAndName) {
		this(type, idAndName, idAndName);
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
		return "RunTarget("+getType().getName()+", "+id+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRunTarget other = (AbstractRunTarget) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public BootDashColumn[] getAllColumns() {
		return BootDashColumn.values();
	}

	@Override
	public RunTargetType getType() {
		return type;
	}

	@Override
	public int compareTo(RunTarget other) {
		return this.getId().compareTo(other.getId());
	}

}
