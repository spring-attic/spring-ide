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

public class CloudFoundryApplicationHarness implements TestRule {

	private Set<String> ownedAppNames  = new HashSet<>();
	private DefaultClientRequestsV2 client;

	public CloudFoundryApplicationHarness(DefaultClientRequestsV2 client) {
		this.client = client;
	}
	
	public String randomAppName() {
		String name = randomAlphabetic(15);
		ownedAppNames.add(name);
		return name;
	}
	
	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} finally {
					deleteOwnedApps();
				}
			}
		};
	}
	protected void deleteOwnedApps() {
		if (!ownedAppNames.isEmpty()) {

			try {
				for (String name : ownedAppNames) {
					try {
						this.client.deleteApplication(name);
					} catch (Exception e) {
						// May get 404 or other 400 errors if it is alrready
						// gone so don't prevent other owned apps from being
						// deleted
					}
				}

			} catch (Exception e) {
				fail("failed to cleanup owned apps: " + e.getMessage());
			}
		}
	}

}
