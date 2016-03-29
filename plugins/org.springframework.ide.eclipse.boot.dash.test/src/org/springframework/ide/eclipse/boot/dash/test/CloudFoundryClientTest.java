/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

import com.google.common.collect.ImmutableSet;

import reactor.core.publisher.Flux;

public class CloudFoundryClientTest {

	private ClientRequests createClient(CFClientParams fromEnv) throws Exception {
		DefaultCloudFoundryClientFactoryV2 factory = new DefaultCloudFoundryClientFactoryV2();
		return factory.getClient(fromEnv);
	}

	@Test
	public void testGetDomains() throws Exception {
		ClientRequests client = createClient(CfTestTargetParams.fromEnv());
		List<CFCloudDomain> domains = client.getDomains();
		assertEquals("cfapps.io", domains.get(0).getName());

		Set<String> names = Flux.fromIterable(domains)
			.map(CFCloudDomain::getName)
			.toList()
			.map(ImmutableSet::copyOf)
			.get();
		assertContains(names,
				"projectreactor.org",
				"projectreactor.io",
				"dsyer.com"
		);
	}

	@Test
	public void testGetBuildpacks() throws Exception {
		ClientRequests client = createClient(CfTestTargetParams.fromEnv());
		List<CFBuildpack> buildpacks = client.getBuildpacks();

		Set<String> names = Flux.fromIterable(buildpacks)
				.map(CFBuildpack::getName)
				.toList()
				.map(ImmutableSet::copyOf)
				.get();

		assertContains(names,
			"staticfile_buildpack",
			"java_buildpack",
			"ruby_buildpack"
		);
	}

	private void assertContains(Set<String> strings, String... expecteds) {
		for (String e : expecteds) {
			assertContains(e, strings);
		}
	}

	private void assertContains(String expected, Set<String> names) {
		assertTrue("Expected '"+expected+"' not found in: "+names, names.contains(expected));
	}


}
