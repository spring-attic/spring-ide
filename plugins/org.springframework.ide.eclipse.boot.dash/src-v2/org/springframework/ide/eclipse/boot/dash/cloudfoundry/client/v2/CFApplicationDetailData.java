/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;

import com.google.common.collect.ImmutableList;

class CFApplicationDetailData extends CFApplicationSummaryData implements CFApplicationDetail {

	private List<CFInstanceStats> instanceDetails;

	public CFApplicationDetailData(ApplicationDetail app) {
		super(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				//XXX CF V2: env
				null,
				app.getMemoryLimit(),
				UUID.fromString(app.getId()),
				//XXX CF V2: services,
				ImmutableList.of(),
				//XXX CF V2: detectedBuildpack,
				null,
				app.getBuildpack(),
				app.getUrls(),
				CFWrappingV2.wrapAppState(app.getRequestedState()),
				app.getDiskQuota(),
				//XXX CF V2: timeout
				null,
				//XXX CF V2: command
				null,
				//XXX CF V2: stack
				null
		);
		this.instanceDetails = ImmutableList.copyOf(app.getInstanceDetails().stream()
				.map(CFWrappingV2::wrap)
				.collect(Collectors.toList())
		);
	}

	public CFApplicationDetailData(CFApplication app) {
		//TODO: this constructor should not exist. We use it to create 'detailed' data when
		/// its not possible to get details.
		super(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				//XXX CF V2: env
				null,
				app.getMemory(),
				app.getGuid(),
				//XXX CF V2: services,
				ImmutableList.of(),
				//XXX CF V2: detectedBuildpack,
				null,
				null,
				app.getUris(),
				app.getState(),
				app.getDiskQuota(),
				//XXX CF V2: timeout
				null,
				//XXX CF V2: command
				null,
				//XXX CF V2: stack
				null
		);
		this.instanceDetails = ImmutableList.of();
	}

	@Override
	public List<CFInstanceStats> getInstanceDetails() {
		return instanceDetails;
	}


}
