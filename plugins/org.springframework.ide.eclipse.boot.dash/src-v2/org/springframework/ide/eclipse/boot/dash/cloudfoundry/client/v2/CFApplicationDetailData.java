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

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;

import com.google.common.collect.ImmutableList;

public class CFApplicationDetailData extends CFApplicationSummaryData implements CFApplicationDetail {

	private List<CFInstanceStats> instanceDetails;

	public CFApplicationDetailData(
			CFApplicationSummaryData app,
			List<CFInstanceStats> instanceDetails
	) {
		super(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				app.getMemory(),
				app.getGuid(),
				app.getServices(),
				app.getDetectedBuildpack(),
				app.getBuildpackUrl(),
				app.getUris(),
				app.getState(),
				app.getDiskQuota(),
				app.getTimeout(),
				app.getCommand(),
				app.getStack(),
				app.getEnvAsMapMono()
		);
		this.instanceDetails = instanceDetails;
	}

	public CFApplicationDetailData(CFApplicationSummaryData app) {
		//TODO: this constructor should not exist. We use it to create 'detailed' data when
		/// its not possible to get details.
		super(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
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
				null,
				app.getEnvAsMapMono()
		);
		this.instanceDetails = ImmutableList.of();
	}

	@Override
	public List<CFInstanceStats> getInstanceDetails() {
		return instanceDetails;
	}


}
