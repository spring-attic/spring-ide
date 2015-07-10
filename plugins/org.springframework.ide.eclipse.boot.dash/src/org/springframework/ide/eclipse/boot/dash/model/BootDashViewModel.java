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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * @author Kris De Volder
 */
public class BootDashViewModel implements Disposable {

	private LiveSet<RunTarget> runTargets;
	private BootDashModelManager models;
	private Set<RunTargetType> runTargetTypes;

	/**
	 * Create an 'empty' BootDashViewModel with no run targets. Targets can be
	 * added by adding them to the runTarget's LiveSet.
	 */
	public BootDashViewModel(BootDashModelContext context, RunTargetType... runTargetTypes) {
		runTargets = new LiveSet<RunTarget>();

		models = new BootDashModelManager(context, runTargets);

		// Load additional targets AFTER the manager loads the local target
		RunTargetPropertiesManager manager = new RunTargetPropertiesManager(context, runTargetTypes);
		List<RunTarget> existingtargets = manager.getStoredTargets();
		runTargets.addAll(existingtargets);
		runTargets.addListener(manager);

		this.runTargetTypes = new LinkedHashSet<RunTargetType>(Arrays.asList(runTargetTypes));
	}

	public LiveSet<RunTarget> getRunTargets() {
		return runTargets;
	}

	@Override
	public void dispose() {
		models.dispose();
	}

	public void addElementStateListener(ElementStateListener l) {
		models.addElementStateListener(l);
	}

	public void removeElementStateListener(ElementStateListener l) {
		models.removeElementStateListener(l);
	}

	public LiveExpression<Set<BootDashModel>> getSectionModels() {
		return models.getModels();
	}

	public Set<RunTargetType> getRunTargetTypes() {
		return runTargetTypes;
	}

	public void removeTarget(RunTarget toRemove, UserInteractions userInteraction) {

		if (toRemove != null) {
			RunTarget found = null;
			for (RunTarget existingTarget : runTargets.getValues()) {
				if (existingTarget.getId().equals(toRemove.getId())) {
					found = existingTarget;
					break;
				}
			}
			if (found != null && userInteraction.confirmOperation("Deleting run target: " + found.getName(),
					"Are you sure that you want to delete " + found.getName()
							+ "? All information regarding this target will be permanently removed.")) {
				runTargets.remove(found);
			}
		}
	}

}
