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

import java.util.EnumSet;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A RunTarget represents an 'platform/environment' where we can 'Run' BootApps.
 *
 * @author Kris De Volder
 */
public interface RunTarget extends IdAble, Nameable {

	/**
	 * @return Subset of the runstate that a user can request when changing a DashBoardElement's 'run-state'.
	 * Essentially, this allows determining whether a given BootDahsElement can support the 'stop', 'run' and
	 * 'debug' operations which request that the element be brought into a given run-state.
	 */
	public abstract EnumSet<RunState> supportedGoalStates();

	/**
	 * Retrieve all existing launch configurations that are applicable for launching a given BootDashElement on
	 * this RunTarget.
	 */
	public abstract List<ILaunchConfiguration> getLaunchConfigs(BootDashElement element);

	/**
	 * Create a launch config for a given dash element and initialize it with some suitable defaults. The method
	 * should do its best to fill in good defaults, but it is not required that the launch config is already
	 * 'launch ready' as it will be opened in an editor rather than launched immediately.
	 */
	public abstract ILaunchConfiguration createLaunchConfigForEditing(BootDashElement element) throws Exception;
}
