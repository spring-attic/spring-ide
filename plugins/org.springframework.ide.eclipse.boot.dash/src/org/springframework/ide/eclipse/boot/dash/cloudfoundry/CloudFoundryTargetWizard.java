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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.jface.wizard.Wizard;

/**
 * Creates a Cloud Foundry target
 *
 */
public class CloudFoundryTargetWizard extends Wizard {

	private CloudFoundryTargetWizardPage page;

	public CloudFoundryTargetWizard() {

		setWindowTitle("Add a Cloud Foundry Target");

		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new CloudFoundryTargetWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		return getTargetProperties() != null;
	}

	public CloudFoundryTargetProperties getTargetProperties() {
		return page != null ? page.getTargetProperties() : null;
	}
}
