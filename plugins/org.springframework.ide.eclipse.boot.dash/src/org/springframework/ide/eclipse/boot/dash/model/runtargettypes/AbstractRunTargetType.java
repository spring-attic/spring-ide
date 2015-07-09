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
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;

/**
 * @author Kris De Volder
 */
public abstract class AbstractRunTargetType implements RunTargetType {

	private String name;

	public AbstractRunTargetType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RunTargetType("+getName()+")";
	}
	
	@Override
	public RunTarget createRunTarget(TargetProperties properties) {
		return null;
	}

}
