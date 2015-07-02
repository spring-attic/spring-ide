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

import static org.springframework.ide.eclipse.boot.dash.model.RunState.DEBUGGING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class RunTargets {

	private static final EnumSet<RunState> LOCAL_RUN_GOAL_STATES = EnumSet.of(INACTIVE, RUNNING, DEBUGGING);

	private static final LiveSet<RunTarget> nonLocalTargets = new LiveSet<RunTarget>();

	public static final RunTarget LOCAL = new AbstractRunTarget("local") {
		@Override
		public EnumSet<RunState> supportedGoalStates() {
			return LOCAL_RUN_GOAL_STATES;
		}

		@Override
		public List<ILaunchConfiguration> getLaunchConfigs(BootDashElement element) {
			IProject p = element.getProject();
			if (p != null) {
				return BootLaunchConfigurationDelegate.getLaunchConfigs(p);
			}
			return Collections.emptyList();
		}

		public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
			if (mainType != null) {
				return BootLaunchConfigurationDelegate.createConf(mainType);
			} else {
				return BootLaunchConfigurationDelegate.createConf(jp);
			}
		}
	};

	/**
	 * 
	 * @return non-null list of Targets.
	 */
	public static synchronized LiveSet<RunTarget> getTargets() {
		return nonLocalTargets;
	}
}
