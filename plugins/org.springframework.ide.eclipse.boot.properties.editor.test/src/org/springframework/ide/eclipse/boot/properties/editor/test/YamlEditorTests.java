/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.util.StringUtil;

import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.*;

import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;

/**
 * @author Kris De Volder
 */
public class YamlEditorTests extends YamlEditorTestHarness {

	public void testHovers() throws Exception {
		defaultTestData();
		YamlEditor editor = new YamlEditor(
				"spring:\n" +
				"  application:\n" +
				"    name: foofoo\n" +
				"  beyond:\n" +
				"    the-valid: range\n" +
				"    \n" +
				"server:\n" +
				"  port: 8888"
		);

		assertIsHoverRegion(editor, "spring");
		assertIsHoverRegion(editor, "application");
		assertIsHoverRegion(editor, "name");

		assertIsHoverRegion(editor, "server");
		assertIsHoverRegion(editor, "port");

		assertHoverContains(editor, "name", "<b>spring.application.name</b><br><a href=\"type%2Fjava.lang.String\">java.lang.String</a><br><br>Application name.</body>");
		assertHoverContains(editor, "port", "<b>server.port</b>");
		assertHoverContains(editor, "8888", "<b>server.port</b>"); // hover over value show info about corresponding key. Is this logical?

		assertNoHover(editor, "beyond");
		assertNoHover(editor, "the-valid");
		assertNoHover(editor, "range");

		//TODO: these provide no hovers now, but maybe (some of them) should if we index proprty sources and not just the
		// properties themselves.
		assertNoHover(editor, "spring");
		assertNoHover(editor, "application");
		assertNoHover(editor, "server");


		//Test for the case where we can't produc an AST for editor text
		editor = new YamlEditor(
				"- syntax\n" +
				"error:\n"
		);
		assertNoHover(editor, "syntax");
		assertNoHover(editor, "error");
	}

	public void testUserDefinedHoversandLinkTargets() throws Exception {
		useProject(createPredefinedProject("demo-enum"));
		data("foo.link-tester", "demo.LinkTestSubject", null, "for testing different Pojo link cases");
		YamlEditor editor = new YamlEditor(
				"#A comment at the start\n" +
				"foo:\n" +
				"  data:\n" +
				"    wavelen: 666\n" +
				"    name: foo\n" +
				"    next: green\n" +
				"  link-tester:\n" +
				"    has-it-all: nice\n" +
				"    strange: weird\n" +
				"    getter-only: getme\n"
		);

		assertHoverContains(editor, "data", "Pojo"); // description from json metadata
		assertHoverContains(editor, "wavelen", "JavaDoc from field"); // javadoc from field
		assertHoverContains(editor, "name", "Set the name"); // javadoc from setter
		assertHoverContains(editor, "next", "Get the next"); // javadoc from getter

		assertLinkTargets(editor, "data", "demo.FooProperties.setData(ColorData)");
		assertLinkTargets(editor, "wavelen", "demo.ColorData.setWavelen(double)");

	}

	public void testHyperlinkTargets() throws Exception {
		System.out.println(">>> testHyperlinkTargets");
		IProject p = createPredefinedProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		YamlEditor editor = new YamlEditor(
				"server:\n"+
				"  port: 888\n" +
				"spring:\n" +
				"  datasource:\n" +
				"    login-timeout: 1000\n" +
				"flyway:\n" +
				"  init-sqls: a,b,c\n"
		);

		assertLinkTargets(editor, "port",
				"org.springframework.boot.autoconfigure.web.ServerProperties.setPort(Integer)"
		);
		assertLinkTargets(editor, "login-",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.hikariDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.tomcatDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.dbcpDataSource()"
		);
		assertLinkTargets(editor, "init-sql",
				"org.springframework.boot.autoconfigure.flyway.FlywayProperties.setInitSqls(List<String>)");
		System.out.println("<<< testHyperlinkTargets");
	}

	public void testReconcile() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server:\n" +
				"  port: \n" +
				"    extracrap: 8080\n" +
				"logging:\n"+
				"  level:\n" +
				"    com.acme: INFO\n" +
				"  snuggem: what?\n" +
				"bogus:\n" +
				"  no: \n" +
				"    good: true\n"
		);
		assertProblems(editor,
				"extracrap: 8080|Expecting a 'int' but got a 'Mapping' node",
				"snuggem|Unknown property",
				"bogus|Unknown property"
		);
	}

	public void test_STS_4140_StringArrayReconciling() throws Exception {
		defaultTestData();

		MockEditor editor;

		//Case 0: very focussed test for easy debugging
		editor = new MockEditor(
			"flyway:\n" +
			"  schemas: [MEMBER_VERSION, MEMBER]"
		);
		assertProblems(editor /*none*/);

		//Case 1: String[] as a flow sequence
		editor = new MockEditor(
			"something-bad: wrong\n"+
			"flyway:\n" +
			"  url: jdbc:h2:file:~/localdb;IGNORECASE=TRUE;mv_store=false\n" +
			"  user: admin\n" +
			"  password: admin\n" +
			"  schemas: [MEMBER_VERSION, MEMBER]"
		);
		assertProblems(editor,
				"something-bad|Unknown property"
				//No other problems should be reported
		);

		//Case 1: String[] as a block sequence
		editor = new MockEditor(
			"something-bad: wrong\n"+
			"flyway:\n" +
			"  url: jdbc:h2:file:~/localdb;IGNORECASE=TRUE;mv_store=false\n" +
			"  user: admin\n" +
			"  password: admin\n" +
			"  schemas:\n" +
			"    - MEMBER_VERSION\n" +
			"    - MEMBER"
		);
		assertProblems(editor,
				"something-bad|Unknown property"
				//No other problems should be reported
		);
	}

	public void test_STS_4140_StringArrayCompletion() throws Exception {
		defaultTestData();

		assertCompletion(
				"#some comment\n" +
				"flyway:\n" +
				"  sch<*>\n"
				, //=>
				"#some comment\n" +
				"flyway:\n" +
				"  schemas:\n" +
				"    - <*>\n"
		);
	}

	public void testReconcileIntegerScalar() throws Exception {
		data("server.port", "java.lang.Integer", null, "Port of server");
		data("server.threads", "java.lang.Integer", null, "Number of threads for server threadpool");
		MockEditor editor = new MockEditor(
				"server:\n" +
				"  port: \n" +
				"    8888\n" +
				"  threads:\n" +
				"    not-a-number\n"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'"
		);
	}

	public void testReconcileExpectMapping() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server:\n" +
				"  - a\n" +
				"  - b\n"
		);
		assertProblems(editor,
				"- a\n  - b|Expecting a 'Mapping' node but got a 'Sequence' node"
		);
	}

	public void testReconcileExpectScalar() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server:\n" +
				"  ? - a\n" +
				"    - b\n" +
				"  : c"
		);
		assertProblems(editor,
				"- a\n    - b|Expecting a 'Scalar' node but got a 'Sequence' node"
		);
	}

	public void testReconcileCamelCaseBasic() throws Exception {
		MockEditor editor;
		data("something.with-many-parts", "java.lang.Integer", "For testing tolerance of camelCase", null);
		data("something.with-parts.and-more", "java.lang.Integer", "For testing tolerance of camelCase", null);

		//Nothing special... not using camel case
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  with-many-parts: not-a-number"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'"
		);

		//Now check that reconcile also tolerates camel case and reports no error for it
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withManyParts: 123"
		);
		assertProblems(editor
				/*no problems*/
		);

		//Now check that reconciler traverses camelCase and reports errors assigning to camelCase names
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withManyParts: not-a-number"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'"
		);

		//Now check also that camelcase tolerance works if its not in the end of the path
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withParts:\n" +
				"    andMore: not-a-number\n" +
				"    bad: wrong\n" +
				"    something-bad: also wrong\n"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'",
				"bad|Unknown property",
				"something-bad|Unknown property"
		);
	}

	public void testContentAssistCamelCaseBasic() throws Exception {
		data("something.with-many-parts", "java.lang.Integer", "For testing tolerance of camelCase", null);
		data("something.with-parts.and-more", "java.lang.Integer", "For testing tolerance of camelCase", null);

		assertCompletion(
				"something:\n" +
				"  withParts:\n" +
				"    <*>",
				// =>
				"something:\n" +
				"  withParts:\n" +
				"    and-more: <*>"
		);
	}

	public void testReconcileCamelCaseBeanProp() throws Exception {
		MockEditor editor;

		IProject p = createPredefinedProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		data("demo.bean", "demo.CamelCaser", "For testing tolerance of camelCase", null);

		//Nothing special... not using camel case
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    the-answer-to-everything: not-a-number\n"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'"
		);

		//Now check that reconcile also tolerates camel case and reports no error for it
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: 42\n"
		);
		assertProblems(editor
				/*no problems*/
		);

		//Now check that reconciler traverses camelCase and reports errors assigning to camelCase names
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: not-a-number\n"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'"
		);

		//Now check also that camelcase tolerance works if its not in the end of the path
		editor = new MockEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: not-a-number\n"+
				"    theLeft:\n"+
				"      bad: wrong\n"+
				"      theAnswerToEverything: not-this\n"
		);
		assertProblems(editor,
				"not-a-number|Expecting a 'int'",
				"bad|Unknown property",
				"not-this|Expecting a 'int'"
		);
	}

	public void testContentAssistCamelCaseBeanProp() throws Exception {
		IProject p = createPredefinedProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		data("demo.bean", "demo.CamelCaser", "For testing tolerance of camelCase", null);

		assertCompletion(
				"demo:\n" +
				"  bean:\n" +
				"    theLeft:\n" +
				"      answer<*>\n",
				// =>
				"demo:\n" +
				"  bean:\n" +
				"    theLeft:\n" +
				"      the-answer-to-everything: <*>\n"
		);
	}

	public void testContentAssistCamelCaseInsertInExistingContext() throws Exception {
		data("demo-bean.first", "java.lang.String", "For testing tolerance of camelCase", null);
		data("demo-bean.second", "java.lang.String", "For testing tolerance of camelCase", null);

		//Confirm first it works correctly without camelizing complications
//		assertCompletion(
//				"demo-bean:\n" +
//				"  first: numero uno\n" +
//				"something-else: separator\n"+
//				"sec<*>",
//				// =>
//				"demo-bean:\n" +
//				"  first: numero uno\n" +
//				"  second: <*>\n" +
//				"something-else: separator\n"
//		);

		//Now with camels
		assertCompletion(
				"demoBean:\n" +
				"  first: numero uno\n" +
				"something-else: separator\n"+
				"sec<*>",
				// =>
				"demoBean:\n" +
				"  first: numero uno\n" +
				"  second: <*>\n" +
				"something-else: separator\n"
		);
	}


	public void testReconcileBeanPropName() throws Exception {
		IProject p = createPredefinedProject("demo-list-of-pojo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Foo"));
		data("some-foo", "demo.Foo", null, "some Foo pojo property");
		MockEditor editor = new MockEditor(
				"some-foo:\n" +
				"  name: Good\n" +
				"  bogus: Bad\n" +
				"  ? - a\n"+
				"    - b\n"+
				"  : Weird\n"
		);
		assertProblems(editor,
				"bogus|Unknown property 'bogus' for type 'demo.Foo'",
				"- a\n    - b|Expecting a bean-property name for object of type 'demo.Foo' "
							+ "but got a 'Sequence' node"
		);
	}

	public void testIgnoreSpringExpression() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server:\n" +
				"  port: ${random.int}\n" + //should not be an error
				"  bad: wrong\n"
		);
		assertProblems(editor,
				"bad|Unknown property"
		);
	}

	public void testReconcilePojoArray() throws Exception {
		IProject p = createPredefinedProject("demo-list-of-pojo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Foo"));

		{
			MockEditor editor = new MockEditor(
					"token-bad-guy: problem\n"+
					"volder:\n" +
					"  foo:\n" +
					"    list:\n"+
					"      - name: Kris\n" +
					"        description: Kris\n" +
					"        roles:\n" +
					"          - Developer\n" +
					"          - Admin\n" +
					"        bogus: Bad\n"
			);

			assertProblems(editor,
					"token-bad-guy|Unknown property",
					//'name' is ok
					//'description' is ok
					"bogus|Unknown property 'bogus' for type 'demo.Foo'"
			);
		}

		{ //Pojo array can also be entered as a map with integer keys

			MockEditor editor = new MockEditor(
					"token-bad-guy: problem\n"+
					"volder:\n" +
					"  foo:\n" +
					"    list:\n"+
					"      0:\n"+
					"        name: Kris\n" +
					"        description: Kris\n" +
					"        roles:\n" +
					"          0: Developer\n" +
					"          one: Admin\n" +
					"        bogus: Bad\n"
			);

			assertProblems(editor,
					"token-bad-guy|Unknown property",
					"one|Expecting a 'int' but got 'one'",
					"bogus|Unknown property 'bogus' for type 'demo.Foo'"
			);

		}

	}

	public void testReconcileSequenceGotAtomicType() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"liquibase:\n" +
				"  enabled:\n" +
				"    - element\n"
		);
		assertProblems(editor,
				"- element|Expecting a 'boolean' but got a 'Sequence' node"
		);
	}

	public void testReconcileSequenceGotMapType() throws Exception {
		data("the-map", "java.util.Map<java.lang.String,java.lang.String>", null, "Nice mappy");
		MockEditor editor = new MockEditor(
				"the-map:\n" +
				"  - a\n" +
				"  - b\n"
		);
		assertProblems(editor,
				"- a\n  - b|Expecting a 'Map<String, String>' but got a 'Sequence' node"
		);
	}

	public void testEnumPropertyReconciling() throws Exception {
		IProject p = createPredefinedProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");
		MockEditor editor = new MockEditor(
				"foo:\n"+
				"  color: BLUE\n" +
				"  color: RED\n" + //technically not allowed to bind same key twice but we don' check this
				"  color: GREEN\n" +
				"  color:\n" +
				"    bad: BLUE\n" +
				"  color: Bogus\n"
		);

		assertProblems(editor,
				"bad: BLUE|Expecting a 'demo.Color[RED, GREEN, BLUE]' but got a 'Mapping' node",
				"Bogus|Expecting a 'demo.Color[RED, GREEN, BLUE]' but got 'Bogus'"
		);
	}

	public void testReconcileSkipIfNoMetadata() throws Exception {
		MockEditor editor = new MockEditor(
				"foo:\n"+
				"  color: BLUE\n" +
				"  color: RED\n" + //technically not allowed to bind same key twice but we don' check this
				"  color: GREEN\n" +
				"  color:\n" +
				"    bad: BLUE\n" +
				"  color: Bogus\n"
		);
		assertTrue(index.isEmpty());
		assertProblems(editor
				//nothing
		);
	}

	public void testReconcileCatchesParseError() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"somemap: val\n"+
				"- sequence"
		);
		assertProblems(editor,
				"-|expected <block end>"
		);
	}

	public void testReconcileCatchesScannerError() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"somemap: \"quotes not closed\n"
		);
		assertProblems(editor,
				"|unexpected end of stream"
		);
	}

	public void testContentAssistSimple() throws Exception {
		defaultTestData();
		assertCompletion("port<*>",
				"server:\n"+
				"  port: <*>");
		assertCompletion(
				"#A comment\n" +
				"port<*>",
				"#A comment\n" +
				"server:\n"+
				"  port: <*>");
		assertCompletions(
				"server:\n" +
				"  nonsense<*>\n"
				//=> nothing
		);
	}

	public void testContentAssistNullContext() throws Exception {
		defaultTestData();
		assertCompletions(
				"#A comment\n" +
				"foo:\n" +
				"  data:\n" +
				"    bogus:\n" +
				"      <*>"
				// => nothing
		);
	}

	public void testContentAssistNested() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "java.lang.String", "localhost", "Server host address");

		assertCompletion(
				"server:\n"+
				"  port: 8888\n" +
				"  <*>"
				,
				"server:\n"+
				"  port: 8888\n" +
				"  address: <*>"
		);

		assertCompletion(
					"server:\n"+
					"  <*>"
					,
					"server:\n"+
					"  address: <*>"
		);

		assertCompletion(
				"server:\n"+
				"  a<*>"
				,
				"server:\n"+
				"  address: <*>"
		);

		assertCompletion(
				"server:\n"+
				"  <*>\n" +
				"  port: 8888"
				,
				"server:\n"+
				"  address: <*>\n" +
				"  port: 8888"
		);

		assertCompletion(
				"server:\n"+
				"  a<*>\n" +
				"  port: 8888"
				,
				"server:\n"+
				"  address: <*>\n" +
				"  port: 8888"
		);

	}

	public void testContentAssistNestedSameLine() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");

		assertCompletion(
				"server: <*>"
				,
				"server: \n" +
				"  port: <*>"
		);

		assertCompletion(
				"#something before this stuff\n" +
				"server: <*>"
				,
				"#something before this stuff\n" +
				"server: \n" +
				"  port: <*>"
		);
	}

	public void testContentAssistInsertCompletionElsewhere() throws Exception {
		defaultTestData();

		assertCompletion(
				"server:\n" +
				"  port: 8888\n" +
				"  address: \n" +
				"  servlet-path: \n" +
				"spring:\n" +
				"  activemq:\n" +
				"something-else: great\n" +
				"aopauto<*>"
			,
				"server:\n" +
				"  port: 8888\n" +
				"  address: \n" +
				"  servlet-path: \n" +
				"spring:\n" +
				"  activemq:\n" +
				"  aop:\n" +
				"    auto: <*>\n" +
				"something-else: great\n"
		);

		assertCompletion(
					"server:\n"+
					"  address: localhost\n"+
					"something: nice\n"+
					"po<*>"
					,
					"server:\n"+
					"  address: localhost\n"+
					"  port: <*>\n" +
					"something: nice\n"
		);
	}

	public void testContentAssistInsertCompletionElsewhereInEmptyParent() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "String", "localhost", "Server host address");

		assertCompletion(
				"#comment\n" +
				"server:\n" +
				"something:\n" +
				"  more\n" +
				"po<*>"
				,
				"#comment\n" +
				"server:\n" +
				"  port: <*>\n" +
				"something:\n" +
				"  more\n"
		);
	}

	public void testContentAssistInetAddress() throws Exception {
		//Test that InetAddress is treated as atomic w.r.t indentation

		defaultTestData();
		assertCompletion(
				"#set address of server\n" +
				"servadd<*>"
				, //=>
				"#set address of server\n" +
				"server:\n"+
				"  address: <*>"
		);

		assertCompletion(
				"#set address of server\n" +
				"server:\n"+
				"  port: 888\n" +
				"more: stuff\n" +
				"servadd<*>"
				, //=>
				"#set address of server\n" +
				"server:\n"+
				"  port: 888\n" +
				"  address: <*>\n" +
				"more: stuff\n"
		);

	}

	public void testContentAssistInsertCompletionElsewhereThatAlreadyExists() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "String", "localhost", "Server host address");

		//inserting something that already exists should just move the cursor to existing node

		assertCompletion(
				"server:\n"+
				"  port:\n" +
				"    8888\n"+
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port:\n"+
				"    <*>8888\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);

		assertCompletion(
				"server:\n"+
				"  port: 8888\n" +
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port: <*>8888\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);

		assertCompletion(
				"server:\n"+
				"  port:\n"+
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port:<*>\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);

		assertCompletion(
				"server:\n"+
				"  port:8888\n"+
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port:<*>8888\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);
	}


	public void testContentAssistPropertyWithMapType() throws Exception {
		data("foo.mapping", "java.util.Map<java.lang.String,java.lang.String>", null, "Nice little map");

		//Try in-place completion
		assertCompletion(
				"map<*>"
				,
				"foo:\n"+
				"  mapping:\n"+
				"    <*>"
		);

		//Try 'elswhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"map<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  mapping:\n" +
				"    <*>\n" +
				"more: stuff\n"
		);
	}

	public void testContentAssistPropertyWithArrayType() throws Exception {
		data("foo.list", "java.util.List<java.lang.String>", null, "Nice little list");

		//Try in-place completion
		assertCompletion(
				"lis<*>"
				,
				"foo:\n"+
				"  list:\n"+
				"    - <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"lis<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  list:\n" +
				"    - <*>\n" +
				"more: stuff\n"
		);
	}

	public void testContentAssistPropertyWithPojoType() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		//Try in-place completion
		assertCompletion(
				"foo.d<*>"
				,
				"foo:\n" +
				"  data:\n" +
				"    <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"foo.d<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  data:\n" +
				"    <*>\n" +
				"more: stuff\n"
		);
	}

	public void testContentAssistPropertyWithEnumType() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		//Try in-place completion
		assertCompletion(
				"foo.co<*>"
				,
				"foo:\n" +
				"  color: <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"foo.co<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  color: <*>\n" +
				"more: stuff\n"
		);
	}

	public void testCompletionForExistingGlobalPropertiesAreDemoted() throws Exception {
		data("foo.bar", "java.lang.String", null, null);
		data("foo.buttar", "java.lang.String", null, null);
		data("foo.baracks", "java.lang.String", null, null);
		data("foo.zamfir", "java.lang.String", null, null);
		assertCompletions(
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: full\n" +
				"something:\n" +
				"  in: between\n"+
				"bar<*>",
				//=>
				// buttar
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: full\n" +
				"  buttar: <*>\n" +
				"something:\n" +
				"  in: between",
				// bar (already existed so becomes navigation)
				"foo:\n" +
				"  bar: <*>nice\n" +
				"  baracks: full\n" +
				"something:\n" +
				"  in: between",
				// baracks (already existed so becomes navigation)
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: <*>full\n" +
				"something:\n" +
				"  in: between"
		);
	}

	public void testCompletionForExistingBeanPropertiesAreDemoted() throws Exception {
		useProject(createPredefinedProject("demo-enum"));
		assertCompletions(
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    <*>",
				///// non-existing ///
				//color-children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    color-children:\n"+
				"      <*>",
				//mapped-children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    mapped-children:\n"+
				"      <*>",
				//nested
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    nested:\n"+
				"      <*>",
				//next
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    next: <*>",
				//tags
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    tags:\n" +
				"      - <*>",
				//wavelen
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    wavelen: <*>",
				///// existing ////
				//children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      <*>-\n" +
				"    name: foo",
				//name
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: <*>foo"
		);
	}


	public void testNoCompletionsInsideComments() throws Exception {
		defaultTestData();

		//Ensure this test is not trivially passing because of missing test data
		assertCompletion(
				"po<*>"
				,
				"server:\n"+
				"  port: <*>"
		);

		assertNoCompletions(
				"#po<*>"
		);
	}

	public void testCompletionsFromDeeplyNestedNode() throws Exception {
		String[] names = {"foo", "nested", "bar"};
		int levels = 4;
		generateNestedProperties(levels, names, "");

		assertCompletionCount(81, // 3^4
				"<*>"
		);

		assertCompletionCount(27, // 3^3
				"#comment\n" +
				"foo:\n" +
				"  <*>"
		);

		assertCompletionCount( 9, // 3^2
				"#comment\n" +
				"foo:\n" +
				"  bar: <*>"
		);

		assertCompletionCount( 3,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      <*>"
		);

		assertCompletionCount( 9,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    <*>"
		);

		assertCompletionCount(27,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"  <*>"
		);

		assertCompletionCount(81,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"<*>"
		);


		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    bar:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"  <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    bar:\n" +
				"      bar: <*>\n" +
				"  "
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"<*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"bar:\n" +
				"  bar:\n" +
				"    bar:\n" +
				"      bar: <*>"
		);
	}

	public void testInsertCompletionIntoDeeplyNestedNode() throws Exception {
		String[] names = {"foo", "nested", "bar"};
		int levels = 4;
		generateNestedProperties(levels, names, "");

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"bar.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"bar:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n" +
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.nested.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n"+
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.nested.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.nested.bar.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"      bar: <*>\n" +
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.nested.bar.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"      bar: <*>\n"
		);

	}

	public void testBooleanValueCompletion() throws Exception {
		defaultTestData();
		assertCompletions(
				"liquibase:\n" +
				"  enabled: <*>",
				"liquibase:\n" +
				"  enabled: false<*>",
				"liquibase:\n" +
				"  enabled: true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:<*>",
				"liquibase:\n" +
				"  enabled:false<*>",
				"liquibase:\n" +
				"  enabled:true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    <*>",
				"liquibase:\n" +
				"  enabled:\n" +
				"    false<*>",
				"liquibase:\n" +
				"  enabled:\n" +
				"    true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled: f<*>\n",
				"liquibase:\n" +
				"  enabled: false<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled: t<*>\n",
				"liquibase:\n" +
				"  enabled: true<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:f<*>\n",
				"liquibase:\n" +
				"  enabled:false<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:t<*>\n",
				"liquibase:\n" +
				"  enabled:true<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    f<*>\n",
				"liquibase:\n" +
				"  enabled:\n"+
				"    false<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    t<*>\n",
				"liquibase:\n" +
				"  enabled:\n"+
				"    true<*>\n"
		);

		// booleans can also be completed in upper case?
		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    T<*>\n",
				"liquibase:\n" +
				"  enabled:\n" +
				"    TRUE<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    F<*>\n",
				"liquibase:\n" +
				"  enabled:\n" +
				"    FALSE<*>\n"
		);

		//Dont suggest completion for something that's already complete. Causes odd
		// and annoying behavior, like a completion popup after you stopped typing 'true'
		assertNoCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    true<*>"
		);

		//one more... for special char like '-' in the name

		assertCompletions(
				"liquibase:\n" +
				"  check-change-log-location: t<*>",
				"liquibase:\n" +
				"  check-change-log-location: true<*>"
		);
	}

	public void testEnumValueCompletion() throws Exception {
		useProject(createPredefinedProject("demo-enum"));
		data("foo.color", "demo.Color", null, "A foonky colour");

		assertCompletion("foo.c<*>",
				"foo:\n" +
				"  color: <*>" //Should complete on same line because enums are 'simple' values.
		);

		assertCompletion("foo:\n  color: R<*>", "foo:\n  color: RED<*>");
		assertCompletion("foo:\n  color: G<*>", "foo:\n  color: GREEN<*>");
		assertCompletion("foo:\n  color: B<*>", "foo:\n  color: BLUE<*>");

		assertCompletion("foo:\n  color: r<*>", "foo:\n  color: red<*>");
		assertCompletion("foo:\n  color: g<*>", "foo:\n  color: green<*>");
		assertCompletion("foo:\n  color: b<*>", "foo:\n  color: blue<*>");

		assertCompletionsDisplayString("foo:\n  color: <*>",
				"red", "green", "blue"
		);
	}

	public void testEnumMapValueCompletion() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		assertCompletions(
				"foo:\n" +
				"  nam<*>",
				//==>
				"foo:\n" +
				"  name-colors:\n"+
				"    <*>",
				// or
				"foo:\n" +
				"  color-names:\n"+
				"    <*>"
		);
		assertCompletionsDisplayString(
				"foo:\n"+
				"  name-colors:\n" +
				"    something: <*>",
				//=>
				"red", "green", "blue"
		);
		assertCompletions(
				"foo:\n"+
				"  name-colors:\n" +
				"    something: G<*>",
				// =>
				"foo:\n"+
				"  name-colors:\n" +
				"    something: GREEN<*>"
		);
	}

	public void testEnumMapValueReconciling() throws Exception {
		useProject(createPredefinedProject("demo-enum"));
		data("foo.name-colors", "java.util.Map<java.lang.String,demo.Color>", null, "Map with colors in its values");

		MockEditor editor;

		editor = new MockEditor(
				"foo:\n"+
				"  name-colors:\n" +
				"    jacket: BLUE\n" +
				"    hat: RED\n" +
				"    pants: GREEN\n" +
				"    wrong: NOT_A_COLOR\n"
		);
		assertProblems(editor,
				"NOT_A_COLOR|Color"
		);

		//lowercase enums should work too
		editor = new MockEditor(
				"foo:\n"+
				"  name-colors:\n" +
				"    jacket: blue\n" +
				"    hat: red\n" +
				"    pants: green\n" +
				"    wrong: NOT_A_COLOR\n"
		);
		assertProblems(editor,
				"NOT_A_COLOR|Color"
		);
	}

	public void testEnumMapKeyCompletion() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		data("foo.color-names", "java.util.Map<demo.Color,java.lang.String>", null, "Map with colors in its keys");
		data("foo.color-data", "java.util.Map<demo.Color,demo.ColorData>", null, "Map with colors in its keys, and pojo in values");

		//Map Enum -> String:
		assertCompletions("foo:\n  colnam<*>",
				"foo:\n" +
				"  color-names:\n" +
				"    <*>");
		assertCompletions(
				"foo:\n" +
				"  color-names:\n" +
				"    <*>",
				//=>
				"foo:\n" +
				"  color-names:\n" +
				"    blue: <*>",
				"foo:\n" +
				"  color-names:\n" +
				"    green: <*>",
				"foo:\n" +
				"  color-names:\n" +
				"    red: <*>"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-names:\n" +
				"    <*>",
				//=>
				"blue : String", "green : String", "red : String"
		);

		assertCompletions(
				"foo:\n" +
				"  color-names:\n" +
				"    B<*>",
				"foo:\n" +
				"  color-names:\n" +
				"    BLUE: <*>"
		);

		//Map Enum -> Pojo:
		assertCompletions("foo.coldat<*>",
				"foo:\n" +
				"  color-data:\n" +
				"    <*>");
		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    <*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    blue:\n" +
				"      <*>",
				"foo:\n" +
				"  color-data:\n" +
				"    green:\n" +
				"      <*>",
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      <*>"
		);
		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    B<*>",
				//=>
				"foo:\n" +
				"  color-data:\n" +
				"    BLUE:\n" +
				"      <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    b<*>",
				//=>
				"foo:\n" +
				"  color-data:\n" +
				"    blue:\n" +
				"      <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data: b<*>",
				//=>
				"foo:\n" +
				"  color-data: \n" +
				"    blue:\n" +
				"      <*>"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-data:\n" +
				"    <*>",
				"red : demo.ColorData", "green : demo.ColorData", "blue : demo.ColorData"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-data: <*>\n",
				"red : demo.ColorData", "green : demo.ColorData", "blue : demo.ColorData"
		);

	}

	public void testPojoReconciling() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		MockEditor editor = new MockEditor(
			"foo:\n" +
			"  data:\n" +
			"    bogus: Something\n" +
			"    wavelen: 3.0\n" +
			"    wavelen: not a double\n" +
			"    wavelen:\n"+
			"      more: 3.0\n"+
			"    wavelen:\n" +
			"      - 3.0\n"
		);
		assertProblems(editor,
				"bogus|Unknown property",
				"not a double|'double'",
				"more: 3.0|Expecting a 'double' but got a 'Mapping' node",
				"- 3.0|Expecting a 'double' but got a 'Sequence' node"
		);
	}


	public void testListOfAtomicCompletions() throws Exception {
		data("foo.slist", "java.util.List<java.lang.String>", null, "list of strings");
		data("foo.ulist", "java.util.List<Unknown>", null, "list of strings");
		data("foo.dlist", "java.util.List<java.lang.Double>", null, "list of doubles");
		assertCompletions("foo:\n  u<*>", "foo:\n  ulist:\n    - <*>");
		assertCompletions("foo:\n  d<*>", "foo:\n  dlist:\n    - <*>");
		assertCompletions("foo:\n  sl<*>", "foo:\n  slist:\n    - <*>");
	}

	public void testEnumsInLowerCaseReconciling() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		MockEditor editor = new MockEditor(
				"simple:\n" +
				"  pants:\n"+
				"    size: NOT_A_SIZE\n"+
				"    size: EXTRA_SMALL\n"+
				"    size: extra-small\n"+
				"    size: small\n"+
				"    size: SMALL\n"
		);
		assertProblems(editor,
				"NOT_A_SIZE|ClothingSize"
		);

		editor = new MockEditor(
				"foo:\n" +
				"  color-names:\n"+
				"    red: Rood\n"+
				"    green: Groen\n"+
				"    blue: Blauw\n" +
				"    not-a-color: Wrong\n" +
				"    blue.bad: Blauw\n" +
				"    blue:\n" +
				"      bad: Blauw"
		);
		assertProblems(editor,
				"not-a-color|Color",
				"blue.bad|Color",
				"bad: Blauw|Expecting a 'String' but got a 'Mapping'"
		);

		editor = new MockEditor(
				"foo:\n" +
				"  color-data:\n"+
				"    red:\n" +
				"      next: green\n" +
				"      next: not a color\n" +
				"      bogus: green\n" +
				"      name: Rood\n"
		);
		assertProblems(editor,
				"not a color|Color",
				"bogus|Unknown property"
		);
	}

	public void testEnumsInLowerCaseContentAssist() throws Exception {
		IProject p = createPredefinedProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: S<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: SMALL<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_SMALL<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: s<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: small<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-small<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: ex<*>",
				// =>
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-large<*>",
				// or
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-small<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: EX<*>",
				// =>
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_LARGE<*>",
				// or
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_SMALL<*>"
		);

		assertCompletionsDisplayString("foo:\n  color: <*>", "red", "green", "blue");

		assertCompletions("foo:\n  color-data: B<*>", "foo:\n  color-data: \n    BLUE:\n      <*>");
		assertCompletions("foo:\n  color-data: b<*>", "foo:\n  color-data: \n    blue:\n      <*>");
		assertCompletions("foo:\n  color-data: <*>",
				"foo:\n  color-data: \n    blue:\n      <*>",
				"foo:\n  color-data: \n    green:\n      <*>",
				"foo:\n  color-data: \n    red:\n      <*>"
		);

		assertCompletions(
				"foo:\n"+
				"  color-data:\n"+
				"    red: na<*>",
				"foo:\n"+
				"  color-data:\n"+
				"    red: \n"+
				"      name: <*>"
		);
	}

	public void testPojoInListCompletion() throws Exception {
		useProject(createPredefinedProject("demo-enum"));

		assertCompletion(
				"foo:\n" +
				"  color-data:\n" +
				"    red: chi<*>"
				,// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red: \n" +
				"      children:\n" +
				"        - <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nex<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nes<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nested:\n"+
				"            <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nex<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nes<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nested:\n"+
				"          <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: RED\n" +
				"          wav<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: RED\n" +
				"          wavelen: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: RED\n" +
				"        wav<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: RED\n" +
				"        wavelen: <*>"
		);
	}

	public void test_STS4111_NoEmptyLinesGapBeforeInsertedCompletion() throws Exception {
		data("spring.application.name", "java.lang.String", null, "The name of the application");
		data("spring.application.index", "java.lang.Integer", true, "App instance index");
		data("spring.considerable.fun", "java.lang.Boolean", true, "Whether the spring fun is considerable");

		assertCompletions(
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"\n" +
				"server:\n" +
				"  port: 8888\n" +
				"appname<*>"
				, //=>
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"    name: <*>\n" +
				"\n" +
				"server:\n" +
				"  port: 8888"

		);

		//Also test that:
		// - Fully commented lines also count as gaps
		// - Gaps of more than one line are also handled correctly
		assertCompletions(
				"# spring stuff\n" +
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"\n" +
				"#server stuff\n" +
				"server:\n" +
				"  port: 8888\n" +
				"cfun<*>"
				, //=>
				"# spring stuff\n" +
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"  considerable:\n" +
				"    fun: <*>\n" +
				"\n" +
				"#server stuff\n" +
				"server:\n" +
				"  port: 8888"

		);

	}

	public void testDocumentSeparator() throws Exception {
		defaultTestData();

		assertCompletion(
				"flyway:\n" +
				"  encoding: utf8\n" +
				"---\n" +
				"flyena<*>",
				// =>
				"flyway:\n" +
				"  encoding: utf8\n" +
				"---\n" +
				"flyway:\n" +
				"  enabled: <*>"
		);
	}

	public void testMultiProfileYamlReconcile() throws Exception {
		MockEditor editor;
		//See https://issuetracker.springsource.com/browse/STS-4144
		defaultTestData();

		//Narrowly focussed test-case for easier debugging
		editor = new MockEditor(
				"spring:\n" +
				"  profiles: seven\n"
		);
		assertProblems(editor
				/*none*/
		);

		//More complete test
		editor = new MockEditor(
				"spring:\n" +
				"  profiles: seven\n" +
				"server:\n" +
				"  port: 7777\n" +
				"---\n" +
				"spring:\n" +
				"  profiles: eight\n" +
				"server:\n" +
				"  port: 8888\n"+
				"bogus: bad"
		);
		assertProblems(editor,
				"bogus|Unknown property"
		);
	}

	public void testReconcileArrayOfPrimitiveType() throws Exception {
		data("my.array", "int[]", null, "A primitive array");
		data("my.boxarray", "int[]", null, "An array of boxed primitives");
		data("my.list", "java.util.List<java.lang.Integer>", null, "A list of boxed types");

		MockEditor editor = new MockEditor(
				"my:\n" +
				"  array:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		assertProblems(editor,
				"bad|Expecting a 'int'"
		);

		editor = new MockEditor(
				"my:\n" +
				"  boxarray:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		assertProblems(editor,
				"bad|Expecting a 'int'"
		);

		editor = new MockEditor(
				"my:\n" +
				"  list:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		assertProblems(editor,
				"bad|Expecting a 'int'"
		);
	}

	public void test_STS4231() throws Exception {
		//Should the 'predefined' project need to be recreated... use the commented code below:
//		BootProjectTestHarness projectHarness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
//		IProject project = projectHarness.createBootProject("sts-4231",
//				bootVersionAtLeast("1.3.0"),
//				withStarters("web", "cloud-config-server")
//		);

		//For more robust test use predefined project which is not so much a moving target:
		IProject project = createPredefinedProject("sts-4231");
		useProject(project);

		assertCompletionsDisplayString(
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              <*>\n",
				// ==>
				"name : String",
				"pattern : String[]"
		);

		assertCompletion(
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              p<*>\n",
				// ==>
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              pattern:\n" +
				"                - <*>\n"
		);

	}

	///////////////// cruft ////////////////////////////////////////////////////////

	private void generateNestedProperties(int levels, String[] names, String prefix) {
		if (levels==0) {
			data(prefix, "java.lang.String", null, "Property "+prefix);
		} else if (levels > 0) {
			for (int i = 0; i < names.length; i++) {
				generateNestedProperties(levels-1, names, join(prefix, names[i]));
			}
		}
	}

	private String join(String prefix, String string) {
		if (StringUtil.hasText(prefix)) {
			return prefix +"." + string;
		}
		return string;
	}

}
