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
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;

public class CloudFoundryServicesHarness implements TestRule {
	
	private DefaultClientRequestsV2 client;

	public CloudFoundryServicesHarness(DefaultClientRequestsV2 client) {
		this.client = client;
	}
	private Set<String> ownedServiceNames  = new HashSet<>();

	public String randomServiceName() {
		String name = randomAlphabetic(15);
		ownedServiceNames.add(name);
		return name;
	}

	@Override
	public Statement apply(Statement base, Description description)   {
		return new Statement() {
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} finally {
					deleteOwnedServices();
				}
			}
		};
	}
	
	protected void deleteOwnedServices() {
		if (!ownedServiceNames.isEmpty()) {

			try {
				for (String serviceName : ownedServiceNames) {
					try {
						this.client.deleteService(serviceName).get();
					} catch (Exception e) {
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
}
