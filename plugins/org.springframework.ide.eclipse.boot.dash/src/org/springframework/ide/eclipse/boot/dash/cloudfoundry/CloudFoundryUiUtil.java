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

import java.net.URL;
import java.util.List;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.model.Operation;

public class CloudFoundryUiUtil {

	public static OrgsAndSpaces getCloudSpaces(final CloudFoundryTargetProperties targetProperties,
			IRunnableContext context) throws Exception {

		final OrgsAndSpaces[] spaces = new OrgsAndSpaces[1];

		Operation op = new Operation(
				"Connecting to the Cloud Foundry target. Please wait while the list of spaces is resolved...") {
			protected void runOp(IProgressMonitor monitor) throws Exception {

				SubMonitor sub = SubMonitor.convert(monitor, 100);

				sub.worked(50);
				List<CloudSpace> actualSpaces = getClient(targetProperties).getSpaces();
				if (actualSpaces != null && !actualSpaces.isEmpty()) {
					spaces[0] = new OrgsAndSpaces(actualSpaces);
				}
				sub.worked(50);
			}
		};
		Operation.runForked(op, context);

		return spaces[0];
	}

	public static CloudFoundryOperations getClient(CloudFoundryTargetProperties targetProperties) throws Exception {
		return targetProperties.getSpace() != null ? new CloudFoundryClient(
				new CloudCredentials(targetProperties.getUserName(), targetProperties.getPassword()),
				new URL(targetProperties.getUrl()), targetProperties.getSpace(), targetProperties.isSelfsigned())
				: new CloudFoundryClient(
						new CloudCredentials(targetProperties.getUserName(), targetProperties.getPassword()),
						new URL(targetProperties.getUrl()), targetProperties.isSelfsigned());

	}

	public static Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}
}
