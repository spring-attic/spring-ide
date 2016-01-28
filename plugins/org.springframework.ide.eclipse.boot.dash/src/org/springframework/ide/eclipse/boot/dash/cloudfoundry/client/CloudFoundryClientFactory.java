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

import java.net.URL;
import java.util.List;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableContext;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.OrgsAndSpaces;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;

public abstract class CloudFoundryClientFactory {

	public static final CloudFoundryClientFactory DEFAULT = new DefaultCloudFoundryClientFactory();

	public abstract CloudFoundryOperations getClient(
			CloudCredentials credentials,
			URL apiUrl, String orgName, String spaceName,
			boolean isSelfsigned
	) throws Exception;

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
		return getClient(targetProperties);
	}

	public ClientRequests getClient(CloudFoundryTargetProperties targetProperties) throws Exception {
		return wrap(getClient(
				new CloudCredentials(targetProperties.getUsername(), targetProperties.getPassword()),
					new URL(targetProperties.getUrl()), targetProperties.getOrganizationName(),
					targetProperties.getSpaceName(), targetProperties.isSelfsigned()
				),
				targetProperties
		);
	}

	/**
	 * Wrapper around the client. API used by CF support in boot dash.
	 * <p>
	 * Note: this method is private for a a reason. Anyhting outside this class should have
	 * no reason to have a reference to an 'unwrapped' CloudFoundryOperations and so therefore they should also
	 * never need to wrap one.
	 * <p>
	 * All interactions with the CF client should be done through ClientRequests.
	 *
	 * @param targetProperties
	 */
	private static ClientRequests wrap(CloudFoundryOperations client, CloudFoundryTargetProperties targetProperties) {
		return new ClientRequests(client, targetProperties);
	}

	public OrgsAndSpaces getCloudSpaces(final CloudFoundryTargetProperties targetProperties, IRunnableContext context)
			throws Exception {

		//TODO: this doesn't belong in a 'factory'. Where should/can it go?

		OrgsAndSpaces spaces = null;

		Operation<List<CloudSpace>> op = new Operation<List<CloudSpace>>(
				"Connecting to the Cloud Foundry target. Please wait while the list of spaces is resolved...") {
			protected List<CloudSpace> runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				return CloudFoundryClientFactory.this.getClient(targetProperties).getSpaces();
			}
		};

		List<CloudSpace> actualSpaces = op.run(context, true);
		if (actualSpaces != null && !actualSpaces.isEmpty()) {
			spaces = new OrgsAndSpaces(actualSpaces);
		}

		return spaces;
	}
}
