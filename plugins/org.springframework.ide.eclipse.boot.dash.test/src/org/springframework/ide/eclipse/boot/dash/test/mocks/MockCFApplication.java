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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.cloudfoundry.client.lib.domain.InstanceStats;

import com.google.common.collect.ImmutableList;

public class MockCFApplication extends CFApplicationData {
	private List<InstanceStats> stats = new ArrayList<>();

	public MockCFApplication(String name, UUID guid, int instances, int runningInstances, AppState state) {
		super(name, guid, instances, runningInstances, state);
	}

	public ApplicationStats getStats() {
		return new ApplicationStats(ImmutableList.copyOf(stats));
	}
}
