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
package org.springframework.ide.eclipse.boot.dash.lattice;

import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.APP;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.HOST;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class LatticeRunTarget extends AbstractRunTarget implements RunTargetWithProperties {

	private static final EnumSet<RunState> SUPPORTED_RUN_GOAL_STATES = EnumSet.of(RUNNING, INACTIVE);

	private static final BootDashColumn[] DEFAULT_COLUMNS = {RUN_STATE_ICN, APP, HOST, INSTANCES, TAGS};

	private String targetHost;

	private final TargetProperties targetProperties;

	public static final String HOST_PROP = "host";

	public LatticeRunTarget(String targetHost) {
		super(RunTargetTypes.LATTICE, "LTC:"+targetHost, "Lattice @ "+targetHost);
		this.targetHost = targetHost;
		targetProperties = new TargetProperties(RunTargetTypes.LATTICE, getId());
		targetProperties.put(HOST_PROP, targetHost);
	}

	public LatticeRunTarget(TargetProperties properties) {
		super(RunTargetTypes.LATTICE, "LTC:" + properties.get(HOST_PROP), "Lattice @ " + properties.get(HOST_PROP));
		this.targetHost = properties.get(HOST_PROP);
		targetProperties = new TargetProperties(properties.getAllProperties(), RunTargetTypes.LATTICE, getId());
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return SUPPORTED_RUN_GOAL_STATES;
	}

	@Override
	public List<ILaunchConfiguration> getLaunchConfigs(BootDashElement element) {
		return Collections.emptyList();
	}

	@Override
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		return null;
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashColumn[] getAllColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashModel createElementsTabelModel(BootDashModelContext context) {
		return new LatticeBootDashModel(this, context);
	}

	public String getHost() {
		return targetHost;
	}

	public String getReceptorHost() {
		return "receptor."+getHost();
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public TargetProperties getTargetProperties() {
		return targetProperties;
	}

	@Override
	public boolean canDeployAppsTo() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return true;
	}

	@Override
	public void validate() throws Exception {
		// validate properties. Ignore for lattice for now.
	}

	@Override
	public boolean requiresCredentials() {
		return false;
	}
}
