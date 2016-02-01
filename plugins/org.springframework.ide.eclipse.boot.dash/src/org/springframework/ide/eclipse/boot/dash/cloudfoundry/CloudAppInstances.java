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

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;

/**
 * A Cloud application with additional stats and instances information
 *
 *
 */
public class CloudAppInstances {

	private final CFApplication app;
	private final ApplicationStats stats;

	public CloudAppInstances(CFApplication app, ApplicationStats stats) {
		Assert.isNotNull(app);
		this.app = app;
		this.stats = stats;
	}

	public CFApplication getApplication() {
		return app;
	}

	public ApplicationStats getStats() {
		return stats;
	}
}
