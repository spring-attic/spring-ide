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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableContext;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.OrgsAndSpaces;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1.DefaultCloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;

public abstract class CloudFoundryClientFactory {

//	public static final CloudFoundryClientFactory DEFAULT = new DefaultCloudFoundryClientFactory();

	public static final CloudFoundryClientFactory DEFAULT = new DefaultCloudFoundryClientFactoryV2();

	public abstract ClientRequests getClient(CFClientParams params) throws Exception;

	/**
	 * Get the client for an existing {@link CloudFoundryRunTarget}. Note that
	 * this may require the password to be set for that runtarget.
	 *
	 * @param runTarget
	 * @return client if connection was successful.
	 * @throws Exception
	 *             if there was an error connecting, including if password is
	 *             not set or invalid.
	 */
	public ClientRequests getClient(CloudFoundryRunTarget runTarget) throws Exception {
		CloudFoundryTargetProperties targetProperties = (CloudFoundryTargetProperties) runTarget.getTargetProperties();
		return getClient(new CFClientParams(targetProperties));
	}

	public final ClientRequests getClient(CloudFoundryTargetProperties targetProperties) throws Exception {
		return getClient(new CFClientParams(targetProperties));
	}

	public final OrgsAndSpaces getCloudSpaces(final CloudFoundryTargetProperties targetProperties, IRunnableContext context)
			throws Exception {

		//TODO: this doesn't belong in a 'factory'. Where should/can it go?

		OrgsAndSpaces spaces = null;

		Operation<List<CFSpace>> op = new Operation<List<CFSpace>>(
				"Connecting to the Cloud Foundry target. Please wait while the list of spaces is resolved...") {
			protected List<CFSpace> runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				return CloudFoundryClientFactory.this.getClient(targetProperties).getSpaces();
			}
		};

		List<CFSpace> actualSpaces = op.run(context, true);
		if (actualSpaces != null && !actualSpaces.isEmpty()) {
			spaces = new OrgsAndSpaces(actualSpaces);
		}

		return spaces;
	}
}
