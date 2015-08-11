/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.lattice;

import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;

/**
 * @author Kris De Volder
 */
public class LatticeCreateTargetWizardModel {

	private static final String DEFAULT_LATTICE_TARGET = "192.168.11.11.xip.io";

	private LiveSet<RunTarget> targets;

	private FieldModel<String> target = new StringFieldModel("Target", DEFAULT_LATTICE_TARGET);

	public LatticeCreateTargetWizardModel(LiveSet<RunTarget> targets) {
		this.targets = targets;
	}

	public FieldModel<String> getTarget() {
		return target;
	}

	public boolean performFinish() throws Exception {
		String targetHost = target.getValue();
		if (targetHost != null) {
			TargetProperties props = new TargetProperties();
			props.put(LatticeRunTarget.HOST_PROP, targetHost);
			RunTarget newTarget = RunTargetTypes.LATTICE.createRunTarget(props);
			targets.add(newTarget);
		}
		return true;
	}

}
