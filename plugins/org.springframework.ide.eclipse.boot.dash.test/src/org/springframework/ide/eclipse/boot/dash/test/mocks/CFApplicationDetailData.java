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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;

public class CFApplicationDetailData extends CFApplicationData implements CFApplicationDetail {

	//TODO: similar looking data objects exist in the 'real' client implementation. So why do
	// we need to mock them?

	private final List<CFInstanceStats> instanceDetails;

	public CFApplicationDetailData(CFApplication summary, List<CFInstanceStats> instanceDetails) {
		super(
				summary.getName(),
				summary.getGuid(),
				summary.getInstances(),
				summary.getRunningInstances(),
				summary.getState(),
				summary.getMemory(),
				summary.getDiskQuota(),
				summary.getDetectedBuildpack(),
				summary.getBuildpackUrl(),
				summary.getEnvAsMap(),
				summary.getServices(),
				summary.getUris(),
				summary.getTimeout(),
				summary.getCommand(),
				summary.getStack()
		);
		this.instanceDetails = instanceDetails;
	}

	@Override
	public List<CFInstanceStats> getInstanceDetails() {
		return instanceDetails;
	}


}
