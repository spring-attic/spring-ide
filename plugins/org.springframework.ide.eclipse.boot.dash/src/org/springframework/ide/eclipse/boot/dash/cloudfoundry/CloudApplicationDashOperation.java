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
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;

/**
 * Operations that are performed on a Cloud Foundry application (e.g. start,
 * stop, update instances). Once the operation is completed, it resolves an up
 * to date {@link CloudApplication} that reflects changes done by the operation.
 *
 */
public abstract class CloudApplicationDashOperation extends CloudOperation<CloudApplication> {

	protected final WrappingBootDashElement<String> element;

	public CloudApplicationDashOperation(String op, WrappingBootDashElement<String> element, CloudFoundryOperations client,
			UserInteractions ui) {
		super(op + ": " + element.getName(), client, ui);
		this.element = element;
	}

	public WrappingBootDashElement<String> getElement() {
		return element;
	}

}
