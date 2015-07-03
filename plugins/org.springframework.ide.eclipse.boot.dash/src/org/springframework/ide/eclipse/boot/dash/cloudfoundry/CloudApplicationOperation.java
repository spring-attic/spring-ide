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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.model.Operation;

/**
 * Operations that are performed on a Cloud Foundry application (e.g. start,
 * stop, update instances). Once the operation is completed, it resolves an up
 * to date {@link CloudApplication} that reflects changes done by the operation.
 *
 */
public abstract class CloudApplicationOperation extends Operation<CloudApplication> {

	protected final String appName;
	protected final CloudFoundryOperations operations;

	public CloudApplicationOperation(String appName, CloudFoundryOperations operations, String opName) {
		super(appName);
		this.operations = operations;
		this.appName = appName;
	}

	@Override
	protected CloudApplication runOp(IProgressMonitor monitor) throws Exception {

		doAppOperation(operations, monitor);

		// fetch an updated Cloud Application that reflects changes that were
		// performed on it
		return operations.getApplication(appName);
	}

	abstract protected void doAppOperation(CloudFoundryOperations operations, IProgressMonitor monitor)
			throws Exception;

}
