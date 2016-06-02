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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springframework.ide.eclipse.editor.support.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableMap;

import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryClientTest.FLAKY_SERVICE_BROKER;

public class CloudFoundryServicesHarness implements Disposable {

	private static final Duration CREATE_SERVICE_TIMEOUT = Duration.ofMinutes(1);

	private DefaultClientRequestsV2 client;

	public CloudFoundryServicesHarness(DefaultClientRequestsV2 client) {
		this.client = client;
	}
	private Set<String> ownedServiceNames  = new HashSet<>();

	public String randomServiceName() {
		String name = StringUtil.datestamp()+"-"+randomAlphabetic(10);
		ownedServiceNames.add(name);
		return name;
	}

	public String createTestUserProvidedService() {
		String name = randomServiceName();
		client.createUserProvidedService(name, ImmutableMap.of()).get(CREATE_SERVICE_TIMEOUT);
		return name;
	}

	public String createTestService() throws Exception {
		String name = randomServiceName();
		RetryUtil.retryWhen("createTestService["+name+"]", 5, FLAKY_SERVICE_BROKER, () -> {
			client.createService(name, "cloudamqp", "lemur").get(CREATE_SERVICE_TIMEOUT);
		});
		return name;
	}

	protected void deleteOwnedServices() {
		if (!ownedServiceNames.isEmpty()) {
			System.out.println("owned services: "+ownedServiceNames);
			try {
				for (String serviceName : ownedServiceNames) {
					System.out.println("delete service: "+serviceName);
					try {
						RetryUtil.retryTimes("delete sercice "+serviceName, 3, () -> {
							this.client.deleteServiceAsync(serviceName).get();
						});
					} catch (Exception e) {
						System.out.println("Failed to delete ["+serviceName+"]: "+ExceptionUtil.getMessage(e));

						// May get 404 or other 400 errors if it is alrready
						// gone so don't prevent other owned apps from being
						// deleted
					}
				}

			} catch (Exception e) {
				fail("failed to cleanup owned services: " + e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
		assertTrue(!client.isLoggedOut());
		deleteOwnedServices();
	}
}
