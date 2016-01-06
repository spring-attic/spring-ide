/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.wizard.Wizard;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetWizardPage;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

/**
 * Creates a run target
 *
 */
public class RunTargetWizard extends Wizard {

	private CloudFoundryTargetWizardPage page;
	private CloudFoundryTargetWizardModel model;

	public RunTargetWizard(CloudFoundryTargetWizardModel model) {
		this.model = model;

		setWindowTitle("Add a Run Target");

		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		// TODO: Turn into framework and load pages based on an initial page
		// that shows different target types.
		// Right it is hardcoded to load the Cloud Foundry target page.

		page = new CloudFoundryTargetWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		return getRunTarget() != null;
	}

	public RunTarget getRunTarget() {
		return page != null ? page.getRunTarget() : null;
	}
}
