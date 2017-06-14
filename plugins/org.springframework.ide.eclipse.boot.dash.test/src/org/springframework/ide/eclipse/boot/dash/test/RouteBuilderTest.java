/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteAttributes;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBuilder;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlTraversal;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

import com.google.common.collect.ImmutableList;

public class RouteBuilderTest {

	List<CFCloudDomainData> TEST_DOMAINS = ImmutableList.of(
			new CFCloudDomainData("cfapps.io"),
			new CFCloudDomainData("testing.me"),
			new CFCloudDomainData("tcp.cfapps.io", CFDomainType.TCP, CFDomainStatus.SHARED)
	);

	@Test
	public void domainAndHost() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domain: testing.me\n" +
				"  host: some-host"
				, //=>
				"some-host[.testing.me]"
		);
	}

	private void assertRoutes(String manifestText, String... expectedRoutes) {
		assertRoutes(TEST_DOMAINS, manifestText, expectedRoutes);
	}

	private void assertRoutes(List<CFCloudDomainData> domains, String manifestText, String[] expectedRoutes) {
		RouteBuilder rb = new RouteBuilder(domains);
		List<RouteBinding> routes = rb.buildRoutes(parse(
			"applications:\n" +
			"- name: my-app\n" +
			"  domain: testing.me\n" +
			"  host: some-host"
		));
		StringBuilder expected = new StringBuilder();
		for (String r : expectedRoutes) {
			expected.append(r+"\n");
		}
		StringBuilder actual = new StringBuilder();
		for (RouteBinding r : routes) {
			actual.append(r);
		}
		assertEquals(expected.toString(), actual.toString());
	}

	/**
	 * Parse a manifest with a single
	 * @param manifestText
	 * @return
	 */
	private RouteAttributes parse(String manifestText) {
//		IDocument _doc = new Document(manifestText);
//		YamlDocument yml = new YamlDocument(_doc, YamlStructureProvider.DEFAULT);
//		new YamlFileAST(_doc, iter)
//		YamlTraversal.EMPTY.thenValAt("applications").thenAnyChild().thenValAt("name")
//		.traverseAmbiguously(yml.get);
		return null;
	}

}
