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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;

/**
 * An action who's intended effect is to transition a BootDashElement to a
 * given goal state.
 *
 * @author Kris De Volder
 */
public abstract class RunStateAction extends AbstractBootDashAction {


	private static final boolean DEBUG = false; //(""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}


	private static final ISchedulingRule SCEDULING_RULE = JobUtil.lightRule("RunStateAction.RULE");
	protected final RunState goalState;
	private ElementStateListener stateListener = null;
	private BootDashModel model;

	protected void configureJob(Job job) {
		job.setRule(SCEDULING_RULE);
	}

	public RunStateAction(
			BootDashModel model,
			MultiSelection<BootDashElement> selection,
			UserInteractions ui,
			RunState goalState) {
		super(selection, ui);
		debug("Create RunStateAction "+goalState);
		this.model = model;
		this.goalState = goalState;
		model.addElementStateListener(stateListener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateEnablement();
					}
				});
			}
		});
	}
	@Override
	public void updateEnablement() {
		Collection<BootDashElement> selecteds = getSelectedElements();
		setEnabled(appliesTo(selecteds));
	}

	private boolean appliesTo(Collection<BootDashElement> selection) {
		for (BootDashElement e : selection) {
			if (appliesTo(e)) {
				return true;
			}
		}
		return false;
	}

	private boolean appliesTo(BootDashElement e) {
		return goalStateAppliesTo(e) && currentStateAcceptable(e.getRunState());
	}

	/**
	 * Subclass can override when action should only apply to
	 * processes in a specific runState.
	 */
	protected boolean currentStateAcceptable(RunState runState) {
		return true;
	}

	protected boolean goalStateAppliesTo(BootDashElement e) {
		RunTarget target = e.getTarget();
		if (target!=null) {
			return target.supportedGoalStates().contains(goalState);
		}
		return false;
	}


	@Override
	public String toString() {
		return "RunStateAction("+goalState+")";
	}

	/**
	 * Subclass must override to define what 'work' this action does when it triggered.
	 */
	protected abstract Job createJob();

	public final void run() {
		Job job = createJob();
		if (job!=null) {
			configureJob(job);
			job.schedule();
		}
	}

	@Override
	public void dispose() {
		debug("DISPOSE RunStateAction "+goalState);
		super.dispose();
		if (stateListener!=null) {
			//Avoid leaking model listeners
			model.removeElementStateListener(stateListener);
			stateListener = null;
		}
	}

}
