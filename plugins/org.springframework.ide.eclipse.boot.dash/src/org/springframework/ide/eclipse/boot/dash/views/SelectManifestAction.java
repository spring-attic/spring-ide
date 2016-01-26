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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.SelectManifestOp;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class SelectManifestAction extends AbstractBootDashElementsAction {

	public SelectManifestAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		Assert.isNotNull(ui);
		this.setText("Select Manifest");
		this.setToolTipText("Selects a manifest YAML file to use during application restart.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/selectmanifest.gif"));
	}

	@Override
	public void run() {
		CloudAppDashElement element = getSelectedCloudElementWithProject();
		if (element != null) {
			CloudFoundryBootDashModel model = element.getCloudModel();
			model.getOperationsExecution().runOpAsynch(new SelectManifestOp(element, ui));
		}
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(getSelectedCloudElementWithProject() != null);
	}

	protected CloudAppDashElement getSelectedCloudElementWithProject() {
		if (getSelectedElements().size() == 1) {

			for (BootDashElement e : getSelectedElements()) {
				if (e instanceof CloudAppDashElement) {
					CloudAppDashElement cde = (CloudAppDashElement) e;
					if (cde.getProject() != null && cde.getProject().isAccessible()) {
						return cde;
					}
				}
			}
		}
		return null;
	}
}
