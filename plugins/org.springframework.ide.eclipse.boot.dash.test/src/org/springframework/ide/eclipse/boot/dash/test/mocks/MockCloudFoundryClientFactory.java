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

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;

public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {

	private Map<String, CFOrganization> orgsByName = new HashMap<>();
	private Map<String, CFSpace> spacesByName = new HashMap<>();

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return client;
	}

	public CFSpace defSpace(String orgName, String spaceName) {
		String key = orgName+"/"+spaceName;
		CFSpace existing = spacesByName.get(key);
		if (existing==null) {
			CFOrganization org = defOrg(orgName);
			spacesByName.put(key, existing= new CFSpaceData(
					spaceName,
					UUID.randomUUID(),
					org
			));
		}
		return existing;
	}

	private CFOrganization defOrg(String orgName) {
		CFOrganization existing = orgsByName.get(orgName);
		if (existing==null) {
			orgsByName.put(orgName, existing = new CFOrganizationData(
					orgName,
					UUID.randomUUID()
			));
		}
		return existing;
	}

	public final ClientRequests client = mock(ClientRequests.class,
			new Answer<Object>() {

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					System.out.println("mock invocation: "+invocation);
					return null;
				}
			}
	);

}
