/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
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

import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockManifestEditor;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;

public class ManifestYamlEditorTest {

	@Test
	public void toplevelCompletions() throws Exception {
		MockManifestEditor editor;
		editor = new MockManifestEditor("<*>");
		editor.assertCompletions(
				"applications:\n"+
				"- <*>",
				// ---------------
				"buildpack: <*>",
				// ---------------
				"buildpacks:\n"+
				"- <*>",
				// ---------------
				"command: <*>",
				// ---------------
				"disk_quota: <*>",
				// ---------------
				"domain: <*>",
				// ---------------
				"domains:\n"+
				"- <*>",
				// ---------------
				"env:\n"+
				"  <*>",
				// ---------------
				"health-check-http-endpoint: <*>",
				// ----------------
				"health-check-type: <*>",
				// ---------------
//				"host: <*>",
				// ---------------
//				"hosts: \n"+
//				"  - <*>",
				// ---------------
				"inherit: <*>",
				// ---------------
				"instances: <*>",
				// ---------------
				"memory: <*>",
				// ---------------
//				"name: <*>",
				// ---------------
				"no-hostname: <*>",
				// ---------------
				"no-route: <*>",
				// ---------------
				"path: <*>",
				// ---------------
				"random-route: <*>",
				// ---------------
				"routes:\n"+
				"- <*>",
				// ---------------
				"services:\n"+
				"- <*>",
				// ---------------
				"stack: <*>",
				// ---------------
				"timeout: <*>"
		);

		editor = new MockManifestEditor("ranro<*>");
		editor.assertCompletions(
				"random-route: <*>"
		);
	}

	@Test
	public void nestedCompletions() throws Exception {
		MockManifestEditor editor;
		editor = new MockManifestEditor(
				"applications:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				// ---------------
				"applications:\n" +
				"- buildpack: <*>",
				// ---------------
				"applications:\n" +
				"- buildpacks:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- command: <*>",
				// ---------------
				"applications:\n" +
				"- disk_quota: <*>",
				// ---------------
				"applications:\n" +
				"- domain: <*>",
				// ---------------
				"applications:\n" +
				"- domains:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- env:\n"+
				"    <*>",
				// ---------------
				"applications:\n" +
				"- health-check-http-endpoint: <*>",
				// ---------------
				"applications:\n" +
				"- health-check-type: <*>",
				// ---------------
				"applications:\n" +
				"- host: <*>",
				// ---------------
				"applications:\n" +
				"- hosts:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- instances: <*>",
				// ---------------
				"applications:\n" +
				"- memory: <*>",
				// ---------------
				"applications:\n" +
				"- name: <*>",
				// ---------------
				"applications:\n" +
				"- no-hostname: <*>",
				// ---------------
				"applications:\n" +
				"- no-route: <*>",
				// ---------------
				"applications:\n" +
				"- path: <*>",
				// ---------------
				"applications:\n" +
				"- random-route: <*>",
				// ---------------
				"applications:\n" +
				"- routes:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- services:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- stack: <*>",
				// ---------------
				"applications:\n" +
				"- timeout: <*>"
		);
	}

	@Test
	public void valueCompletions() throws Exception {
		assertCompletions("disk_quota: <*>",
				"disk_quota: 1024M<*>",
				"disk_quota: 256M<*>",
				"disk_quota: 512M<*>"
		);
		assertCompletions("memory: <*>",
				"memory: 1024M<*>",
				"memory: 256M<*>",
				"memory: 512M<*>"
		);
		assertCompletions("no-hostname: <*>",
				"no-hostname: false<*>",
				"no-hostname: true<*>"
		);
		assertCompletions("no-route: <*>",
				"no-route: false<*>",
				"no-route: true<*>"
		);
		assertCompletions("random-route: <*>",
				"random-route: false<*>",
				"random-route: true<*>"
		);
		assertCompletions("health-check-type: <*>",
				"health-check-type: http<*>",
//				"health-check-type: none<*>", Still valid, but not suggested because its deprecated
				"health-check-type: port<*>",
				"health-check-type: process<*>"
		);
	}

	@Test public void reconcileHealthCheckType() throws Exception {
		MockManifestEditor editor;
		ReconcileProblem problem;

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: none"
		);
		problem = editor.assertProblems("none|'none' is deprecated in favor of 'process'").get(0);
		assertEquals(ProblemSeverity.WARNING, problem.getType().getDefaultSeverity());

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: port"
		);
		editor.assertProblems(/*NONE*/);

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: process"
		);
		editor.assertProblems(/*NONE*/);
	}

	@Test public void reconcileHealthHttpEndPointIgnoredWarning() throws Exception {
		MockManifestEditor editor;
		ReconcileProblem problem;

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: process\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");

		editor = new MockManifestEditor(
				"health-check-type: http\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-http-endpoint: /health"
		);
		problem = editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `port`)").get(0);
		assertEquals(ProblemSeverity.WARNING, problem.getType().getDefaultSeverity());

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = new MockManifestEditor(
				"health-check-type: http\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: process\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");

		editor = new MockManifestEditor(
				"health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `port`)");

		editor = new MockManifestEditor(
				"health-check-type: process\n" +
				"health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");
	}

	@Test
	public void hoverInfos() throws Exception {
		MockManifestEditor editor = new MockManifestEditor(
			"memory: 1G\n" +
			"#comment\n" +
			"inherit: base-manifest.yml\n"+
			"applications:\n" +
			"- buildpack: zbuildpack\n" +
			"  domain: zdomain\n" +
			"  name: foo\n" +
			"  command: java main.java\n" +
			"  disk_quota: 1024M\n" +
			"  buildpacks:\n" +
			"  - zbuildpack\n" +
			"  domains:\n" +
			"  - pivotal.io\n" +
			"  - otherdomain.org\n" +
			"  env:\n" +
			"    RAILS_ENV: production\n" +
			"    RACK_ENV: production\n" +
			"  host: apppage\n" +
			"  hosts:\n" +
			"  - apppage2\n" +
			"  - appage3\n" +
			"  instances: 2\n" +
			"  no-hostname: true\n" +
			"  no-route: true\n" +
			"  path: somepath/app.jar\n" +
			"  random-route: true\n" +
			"  routes:\n" +
			"  - route: tcp-example.com:1234\n" +
			"  services:\n" +
			"  - instance_ABC\n" +
			"  - instance_XYZ\n" +
			"  stack: cflinuxfs2\n" +
			"  timeout: 80\n" +
			"  health-check-type: none\n" +
			"  health-check-http-endpoint: /health\n"
		);
		editor.assertIsHoverRegion("memory");
		editor.assertIsHoverRegion("inherit");
		editor.assertIsHoverRegion("applications");
		editor.assertIsHoverRegion("buildpack");
		editor.assertIsHoverRegion("buildpacks");
		editor.assertIsHoverRegion("domain");
		editor.assertIsHoverRegion("name");
		editor.assertIsHoverRegion("command");
		editor.assertIsHoverRegion("disk_quota");
		editor.assertIsHoverRegion("domains");
		editor.assertIsHoverRegion("env");
		editor.assertIsHoverRegion("host");
		editor.assertIsHoverRegion("hosts");
		editor.assertIsHoverRegion("instances");
		editor.assertIsHoverRegion("no-hostname");
		editor.assertIsHoverRegion("no-route");
		editor.assertIsHoverRegion("path");
		editor.assertIsHoverRegion("random-route");
		editor.assertIsHoverRegion("routes");
		editor.assertIsHoverRegion("services");
		editor.assertIsHoverRegion("stack");
		editor.assertIsHoverRegion("timeout");
		editor.assertIsHoverRegion("health-check-type");

		editor.assertHoverContains("memory", "Use the <code>memory</code> attribute to specify the memory limit");
		editor.assertHoverContains("1G", "Use the <code>memory</code> attribute to specify the memory limit");
		editor.assertHoverContains("inherit", "For example, every child of a parent manifest called <code>base-manifest.yml</code> begins like this:");
		editor.assertHoverContains("buildpack", "use the <code>buildpack</code> attribute to specify its URL or name");
		editor.assertHoverContains("buildpacks", "<strong>Multiple buildpacks</strong>: If you are using multiple buildpacks, you can add an additional value to your manifest:");
	    editor.assertHoverContains("name", "The <code>name</code> attribute is the only required attribute");
	    editor.assertHoverContains("command", "On the command line, use the <code>-c</code> option to specify the custom start command as the following example shows");
	    editor.assertHoverContains("disk_quota", "Use the <code>disk_quota</code> attribute to allocate the disk space for your app instance");
	    editor.assertHoverContains("domain", "You can use the <code>domain</code> attribute when you want your application to be served");
	    editor.assertHoverContains("domains", "Use the <code>domains</code> attribute to provide multiple domains");
	    editor.assertHoverContains("env", "The <code>env</code> block consists of a heading, then one or more environment variable/value pairs");
	    editor.assertHoverContains("host", "Use the <code>host</code> attribute to provide a hostname, or subdomain, in the form of a string");
	    editor.assertHoverContains("hosts", "Use the <code>hosts</code> attribute to provide multiple hostnames, or subdomains");
	    editor.assertHoverContains("instances", "Use the <code>instances</code> attribute to specify the number of app instances that you want to start upon push");
	    editor.assertHoverContains("no-hostname", "By default, if you do not provide a hostname, the URL for the app takes the form of <code>APP-NAME.DOMAIN</code>");
	    editor.assertHoverContains("no-route", "You can use the <code>no-route</code> attribute with a value of <code>true</code> to prevent a route from being created for your application");
	    editor.assertHoverContains("path", "You can use the <code>path</code> attribute to tell Cloud Foundry where to find your application");
	    editor.assertHoverContains("random-route", "Use the <code>random-route</code> attribute to create a URL that includes the app name");
	    editor.assertHoverContains("routes", "Each route for this app is created if it does not already exist");
	    editor.assertHoverContains("services", "The <code>services</code> block consists of a heading, then one or more service instance names");
	    editor.assertHoverContains("stack", "Use the <code>stack</code> attribute to specify which stack to deploy your application to.");
	    editor.assertHoverContains("timeout", "The <code>timeout</code> attribute defines the number of seconds Cloud Foundry allocates for starting your application");
	    editor.assertHoverContains("health-check-type", "Use the <code>health-check-type</code> attribute to");
	    editor.assertHoverContains("health-check-http-endpoint", "customize the endpoint for the <code>http</code>");
	}

	@Test
	public void reconcileMisSpelledPropertyNames() throws Exception {
		MockManifestEditor editor;

		editor = new MockManifestEditor(
				"memory: 1G\n" +
				"aplications:\n" +
				"  - buildpack: zbuildpack\n" +
				"    domain: zdomain\n" +
				"    name: foo"
		);
		editor.assertProblems("aplications|Unknown property");

		//mispelled or not allowed at toplevel
		editor = new MockManifestEditor(
				"name: foo\n" +
				"buildpeck: yahah\n" +
				"memory: 1G\n" +
				"memori: 1G\n"
		);
		editor.assertProblems(
				"name|Unknown property",
				"buildpeck|Unknown property",
				"memori|Unknown property"
		);

		//mispelled or not allowed as nested
		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: fine\n" +
				"  buildpeck: yahah\n" +
				"  memory: 1G\n" +
				"  memori: 1G\n" +
				"  applications: bad\n"
		);
		editor.assertProblems(
				"buildpeck|Unknown property",
				"memori|Unknown property",
				"applications|Unknown property"
		);
	}

	@Test
	public void reconcileStructuralProblems() throws Exception {
		MockManifestEditor editor;

		//forgot the 'applications:' heading
		editor = new MockManifestEditor(
				"- name: foo"
		);
		editor.assertProblems(
				"- name: foo|Expecting a 'Map' but found a 'Sequence'"
		);

		//forgot to make the '-' after applications
		editor = new MockManifestEditor(
				"applications:\n" +
				"  name: foo"
		);
		editor.assertProblems(
				"name: foo|Expecting a 'Sequence' but found a 'Map'"
		);

		//Using a 'composite' element where a scalar type is expected
		editor = new MockManifestEditor(
				"memory:\n"+
				"- bad sequence\n" +
				"buildpack:\n" +
				"  bad: map\n"
		);
		editor.assertProblems(
				"- bad sequence|Expecting a 'Memory' but found a 'Sequence'",
				"bad: map|Expecting a 'Buildpack' but found a 'Map'"
		);
	}

	@Test
	public void reconcileSimpleTypes() throws Exception {
		MockManifestEditor editor;

		//check for 'format' errors:
		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: not a number\n" +
				"  no-route: notBool\n"+
				"  memory: 1024\n" +
				"  disk_quota: 2048\n"
		);
		editor.assertProblems(
				"not a number|NumberFormatException",
				"notBool|boolean",
				"1024|Memory",
				"2048|Memory"
		);

		//check for 'range' errors:
		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: -3\n" +
				"  memory: -1024M\n" +
				"  disk_quota: -2048M\n"
		);
		editor.assertProblems(
				"-3|Value must be at least 1",
				"-1024M|Negative value is not allowed",
				"-2048M|Negative value is not allowed"
		);

		//check that correct values are indeed accepted
		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: true\n"+
				"  memory: 1024M\n" +
				"  disk_quota: 2048MB\n"
		);
		editor.assertProblems(/*none*/);

		//check that correct values are indeed accepted
		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: false\n" +
				"  memory: 1024m\n" +
				"  disk_quota: 2048mb\n"
		);
		editor.assertProblems(/*none*/);

		editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  memory: 1G\n" +
				"  disk_quota: 2g\n"
		);
		editor.assertProblems(/*none*/);
	}

	@Test public void reconcileRoutesWithNoHost() throws Exception {
		MockManifestEditor editor;

		editor = new MockManifestEditor(
			"applications:\n" +
			"- name: my-app\n" +
			"  no-hostname: true\n" +
			"  routes:\n" +
			"  - route: myapp.org"
		);
		//editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
			"no-hostname|Property cannot co-exist with property 'routes'",
			"routes|Property cannot co-exist with properties [no-hostname]"
		);

		editor = new MockManifestEditor(
				"no-hostname: true\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  routes:\n" +
				"  - route: myapp.org"
		);
		//editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
			"no-hostname|Property cannot co-exist with property 'routes'",
			"routes|Property cannot co-exist with properties [no-hostname]"
		);

		editor = new MockManifestEditor(
			"no-hostname: true\n" +
			"applications:\n" +
			"- name: my-app\n" +
			"  no-hostname: true\n" +
			"  routes:\n" +
			"  - route: myapp.org"
		);
		//editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
			"no-hostname|Property cannot co-exist with property 'routes'",
			"no-hostname|Property cannot co-exist with property 'routes'",
			"routes|Property cannot co-exist with properties [no-hostname]"
		);

		editor = new MockManifestEditor(
			"no-hostname: true\n" +
			"applications:\n" +
			"- name: my-app\n" +
			"  host: some-app\n" +
			"  routes:\n" +
			"  - route: myapp.org"
		);
		//editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
			"no-hostname|Property cannot co-exist with property 'routes'",
			"host|Property cannot co-exist with property 'routes'",
			"routes|Property cannot co-exist with properties [host, no-hostname]"
		);

		editor = new MockManifestEditor(
			"no-hostname: true\n" +
			"applications:\n" +
			"- name: my-app\n" +
			"  routes:\n" +
			"  - route: myapp.org\n" +
			"- name: app2\n" +
			"  routes:\n" +
			"  - route: my-route.org"
		);
		//editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
			"no-hostname|Property cannot co-exist with property 'routes'",
			"routes|Property cannot co-exist with properties [no-hostname]",
			"routes|Property cannot co-exist with properties [no-hostname]"
		);
	}

	@Test public void deprecatedHealthCheckTypeQuickfix() throws Exception {
		MockManifestEditor editor = new MockManifestEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: none"
		);
		ReconcileProblem problem = editor.assertProblems("none|'none' is deprecated in favor of 'process'").get(0);
		assertEquals(ProblemSeverity.WARNING, problem.getType().getDefaultSeverity());
// TODO: quickfix not backported.
//		CodeAction quickfix = editor.assertCodeAction(problem);
//		assertEquals("Replace deprecated value 'none' by 'process'", quickfix.getLabel());
//		quickfix.perform();
//
//		editor.assertRawText(
//				"applications:\n" +
//				"- name: foo\n" +
//				"  health-check-type: process"
//		);
	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertCompletions(String textBefore, String... textAfter) throws Exception {
		MockManifestEditor editor = new MockManifestEditor(textBefore);
		editor.assertCompletions(textAfter);
	}
}
