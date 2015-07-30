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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.cfeclipse;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ExternalModelObserver;

public class CloudFoundryEclipseModelObserver implements ExternalModelObserver {
	private ServersIntegration integration = null;

	@Override
	public void observe(BootDashViewModel model) {
		integration = new ServersIntegration(model);
	}

	@Override
	public void showLogs(BootDashElement element) throws Exception {
		if (integration != null && element instanceof CloudDashElement) {
			integration.openConsole((CloudDashElement) element, new NullProgressMonitor());
		}
	}

}
