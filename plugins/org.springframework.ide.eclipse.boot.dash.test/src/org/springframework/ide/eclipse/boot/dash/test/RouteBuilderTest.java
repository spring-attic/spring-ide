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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteAttributes;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBuilder;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlTraversal;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

public class RouteBuilderTest {

	List<CFCloudDomain> TEST_DOMAINS = ImmutableList.of(
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

	@Test
	public void multipleDomainsAndHosts() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domains: [testing.me, cfapps.io]\n" +
				"  hosts: [foo, bar]\n"
				, // ==>
				"blah"
		);
	}

	@Test
	public void randomRouteNoDomain() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  random-route: true\n"
				,
				"?[.cfapps.io]"
		);
	}
	//TODO: randomroute with a http domain
	//TODO: randomroute with a tcp domain

	@Test
	public void noRoute() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: foo\n" +
				"  no-route: true"
				// ==>
				/* NONE */
		);
	}

	////////////////////////////////////////////////////////////////////////////////////

	private void assertRoutes(String manifestText, String... expectedRoutes) {
		assertRoutes(TEST_DOMAINS, manifestText, expectedRoutes);
	}

	private void assertRoutes(List<CFCloudDomain> domains, String manifestText, String[] expectedRoutes) {
		RouteBuilder rb = new RouteBuilder(domains);
		List<RouteBinding> routes = rb.buildRoutes(parse(domains,
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
	private RouteAttributes parse(List<CFCloudDomain> domains, String manifestText) {
		IDocument doc = new Document(manifestText);
		YamlASTProvider parser = new YamlASTProvider(new Yaml());
		YamlFileAST ast = parser.getAST(doc);
		List<Node> names = YamlTraversal.EMPTY
				.thenAnyChild()
				.thenValAt("applications")
				.thenAnyChild()
				.thenValAt("name")
				.traverseAmbiguously(ast)
				.collect(Collectors.toList());
		assertEquals("Number of apps in manifest", 1, names.size());
		String appName = NodeUtil.asScalar(names.get(0));
		CloudData cloudData = new CloudData(domains, "some-buildpack", ImmutableList.of());
		YamlGraphDeploymentProperties dp = new YamlGraphDeploymentProperties(manifestText, appName, cloudData);
		return new RouteAttributes(dp);
	}

}
