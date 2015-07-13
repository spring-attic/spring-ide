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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryUiUtil;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

/**
 * @author Kris De Volder
 */
public class LatticeRunTargetType extends AbstractRunTargetType {

	public LatticeRunTargetType() {
		super("Lattice");
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}

	@Override
	public void openTargetCreationUi(LiveSet<RunTarget> targets) {
		LatticeCreateTargetWizardModel model = new LatticeCreateTargetWizardModel(targets);
		LatticeCreateTargetWizard wiz = new LatticeCreateTargetWizard(model);
		Shell shell = CloudFoundryUiUtil.getShell();
		WizardDialog dlg = new WizardDialog(shell, wiz);
		dlg.open();
	}

	@Override
	public RunTarget createRunTarget(TargetProperties properties) {
		return new LatticeRunTarget(properties);
	}

}
