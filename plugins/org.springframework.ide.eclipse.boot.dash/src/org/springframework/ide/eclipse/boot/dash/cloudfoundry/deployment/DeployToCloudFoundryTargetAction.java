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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class DeployToCloudFoundryTargetAction extends AbstractBootDashElementsAction {

	private RunState runOrDebug;
	private RunTarget target;
	private ValueListener<ClientRequests> connectionListener;

	public DeployToCloudFoundryTargetAction(Params params, RunTarget target, RunState runningOrDebugging) {
		super(params);
		this.setText(target.getName());
		Assert.isLegal(target.getType() instanceof CloudFoundryRunTargetType);
		Assert.isLegal(runningOrDebugging==RunState.RUNNING || runningOrDebugging==RunState.DEBUGGING);
		this.target = target;
		this.runOrDebug = runningOrDebugging;

		this.connectionListener = new ValueListener<ClientRequests>() {
			@Override
			public void gotValue(LiveExpression<ClientRequests> exp, ClientRequests value) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						update();
					}
				});
			}
		};

		if (target instanceof CloudFoundryRunTarget) {
			((CloudFoundryRunTarget) target).addConnectionStateListener(connectionListener);
		}

		updateEnablement();
	}

	@Override
	public void updateVisibility() {
		BootDashElement element = getSingleSelectedElement();
		setVisible(element != null && element instanceof BootProjectDashElement);
	}

	@Override
	public void updateEnablement() {
		BootDashElement element = getSingleSelectedElement();
		setVisible(element != null && element instanceof BootProjectDashElement);

		if (this.target != null && this.target.getType() instanceof CloudFoundryRunTargetType) {
			setEnabled(((CloudFoundryRunTarget) this.target).isConnected());
		}
	}

	@Override
	public void run() {
		try {
			final BootDashElement element = getSingleSelectedElement();
			if (element != null) {
				final IProject project = element.getProject();
				if (project != null) {
					CloudFoundryBootDashModel cfModel = (CloudFoundryBootDashModel) model.getSectionByTargetId(target.getId());
					//No need to wrap this in a job as it already does that itself:
					cfModel.performDeployment(ImmutableSet.of(project), ui, runOrDebug);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public void dispose() {
		if (target instanceof CloudFoundryRunTarget) {
			((CloudFoundryRunTarget) target).removeConnectionStateListener(connectionListener);
		}

		super.dispose();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+runOrDebug+", "+target+")";
	}

}
