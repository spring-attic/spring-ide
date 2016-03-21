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

import org.cloudfoundry.client.lib.domain.InstanceStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationStats;

public class MockCFApplicationStats implements CFApplicationStats {

	private List<InstanceStats> records;

	public MockCFApplicationStats(List<InstanceStats> records) {
		super();
		this.records = records;
	}

	@Override
	public List<InstanceStats> getRecords() {
		return records;
	}

}
