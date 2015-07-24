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
package org.springframework.ide.eclipse.boot.dash.model;

public enum RunState {

	//Note that the order in which these are listed is important as the implementation of 'merge'
	// depends on it.
	INACTIVE,
	STARTING,
	RUNNING,
	DEBUGGING;

	/**
	 * Combine the runstates of two processes. This operation is used so that we can
	 * compute a summarized state when some entity (e.g. a workspace project) is represented
	 * by multiple instances that can have a RunState (e.g. launches, lattice LRPs, CF instances etc)
	 */
	public RunState merge(RunState other) {
		if (this.ordinal()>other.ordinal()) {
			return this;
		} else {
			return other;
		}
	}

	public String getImageUrl() {
		return "icons/rs_"+toString().toLowerCase()+".gif";
	}
}
