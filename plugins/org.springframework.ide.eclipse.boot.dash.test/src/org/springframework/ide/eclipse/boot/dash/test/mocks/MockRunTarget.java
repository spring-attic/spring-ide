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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.NAME;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.EnumSet;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class MockRunTarget extends AbstractRunTarget implements RunTargetWithProperties {

	private TargetProperties properties;
	private boolean requiresCredentials;

	public MockRunTarget(RunTargetType type, TargetProperties properties) {
		this(type, properties, false);
	}

	public MockRunTarget(RunTargetType type, TargetProperties properties, boolean requiresCredentials) {
		super(type, properties.getRunTargetId());
		this.properties = properties;
		this.requiresCredentials = requiresCredentials;
	}

	private final BootDashColumn[] defaultColumns = { RUN_STATE_ICN, NAME, PROJECT, INSTANCES, DEFAULT_PATH, TAGS };

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return  EnumSet.of(RunState.RUNNING, RunState.DEBUGGING);
	}

	@Override
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		return null;
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return defaultColumns;
	}

	@Override
	public MockBootDashModel createElementsTabelModel(BootDashModelContext context, BootDashViewModel viewModel) {
		return new MockBootDashModel(this, context, viewModel);
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsTo() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	public String get(String prop) {
		return properties.get(prop);
	}

	@Override
	public TargetProperties getTargetProperties() {
		return properties;
	}

	@Override
	public void refresh() throws Exception {
	}

	@Override
	public boolean requiresCredentials() {
		return requiresCredentials;
	}

}
