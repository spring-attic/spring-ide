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

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

/**
 * An action who's intended effect is to transition a BootDashElement to a
 * given goal state.
 *
 * @author Kris De Volder
 */
public class RunStateAction extends Action {

	protected final RunState goalState;

	public RunStateAction(RunState goalState) {
		this.goalState = goalState;
	}

	public void updateEnablement(Collection<BootDashElement> selection) {
		setEnabled(appliesTo(selection));
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

}
