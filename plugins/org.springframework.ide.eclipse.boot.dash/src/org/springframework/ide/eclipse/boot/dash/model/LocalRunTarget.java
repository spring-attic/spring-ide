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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

import com.google.common.collect.ImmutableSet;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.*;

public class LocalRunTarget extends AbstractRunTarget {

	public static final RunTarget INSTANCE = new LocalRunTarget();
	private static final BootDashColumn[] DEFAULT_COLUMNS = {RUN_STATE_ICN, APP, LIVE_PORT, DEFAULT_PATH, TAGS, EXPOSED_URL};

	private LocalRunTarget() {
		super(RunTargetTypes.LOCAL, "local");
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return RunTargets.LOCAL_RUN_GOAL_STATES;
	}

	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		if (mainType != null) {
			return BootLaunchConfigurationDelegate.createConf(mainType);
		} else {
			return BootLaunchConfigurationDelegate.createConf(jp);
		}
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashModel createElementsTabelModel(BootDashModelContext context, BootDashViewModel viewModel) {
		return new LocalBootDashModel(context, viewModel);
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public boolean canDeployAppsTo() {
		return false;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return true;
	}
}