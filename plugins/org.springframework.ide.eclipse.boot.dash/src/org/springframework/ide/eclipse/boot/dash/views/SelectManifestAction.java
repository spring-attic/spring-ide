/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class SelectManifestAction extends AbstractCloudAppDashElementsAction {

	public SelectManifestAction(Params params) {
		super(params);
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
			model.runAsynch(new SelectManifestOp(element, ui), ui);
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
