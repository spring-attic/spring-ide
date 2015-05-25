/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.EnumSet;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.*;

public class RunTargets {

	private static final EnumSet<RunState> LOCAL_RUN_GOAL_STATES = EnumSet.of(INACTIVE, RUNNING, DEBUGGING);
	public static final RunTarget LOCAL = new AbstractRunTarget("local") {
		@Override
		public EnumSet<RunState> supportedGoalStates() {
			return LOCAL_RUN_GOAL_STATES;
		}
	};

}
