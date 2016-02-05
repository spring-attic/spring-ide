/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableMap;

/**
 * @author Kris De Volder
 */
public class DeployToCloudFoundryTargetAction extends AbstractBootDashElementsAction {

	private static final boolean DEBUG = true;
	private static void debug(String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}

	private RunState runOrDebug;
	private RunTarget target;

	public DeployToCloudFoundryTargetAction(BootDashViewModel model, RunTarget target, RunState runningOrDebugging, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(model, selection, ui);
		this.setText(target.getName());
		Assert.isLegal(target.getType() instanceof CloudFoundryRunTargetType);
		Assert.isLegal(runningOrDebugging==RunState.RUNNING || runningOrDebugging==RunState.DEBUGGING);
		this.target = target;
		this.runOrDebug = runningOrDebugging;
	}

	@Override
	public void updateVisibility() {
		BootDashElement element = getSingleSelectedElement();
		setVisible(element!=null && element instanceof BootProjectDashElement);
	}

	@Override
	public void updateEnablement() {
		BootDashElement element = getSingleSelectedElement();
		setVisible(element!=null && element instanceof BootProjectDashElement);
	}

	@Override
	public void run() {
		try {
			final BootDashElement element = getSingleSelectedElement();
			if (element!=null) {
				final IProject project = element.getProject();
				CloudFoundryBootDashModel cfModel = (CloudFoundryBootDashModel) model.getSectionByTargetId(target.getId());
				//No need to wrap this in a job as it already does that itself:
				cfModel.performDeployment(ImmutableMap.of(project, element), ui, runOrDebug);
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	public void dispose() {
		debug("Disposing "+this);
		super.dispose();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+runOrDebug+", "+target+")";
	}

}
