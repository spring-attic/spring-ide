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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

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

		//TODO: these provide no hovers now, but maybe (some of them) should if we index proprty sources and not just the
		// properties themselves.
		assertNoHover(editor, "spring");
		assertNoHover(editor, "application");
		assertNoHover(editor, "server");
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
				"extracrap: 8080|Expecting a 'Integer' but got a 'Mapping' node",
				"snuggem|Unknown property",
				"bogus|Unknown property"
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
				"not-a-number|Expecting a 'Integer'"
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
					"one|Expecting a 'Integer' but got 'one'",
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
				"- element|Expecting a 'Boolean' but got a 'Sequence' node"
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

	public void DISABLED_testContentAssistInsertCompletionElsewhereThatAlreadyExists() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "String", "localhost", "Server host address");

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
	}


	public void testContentAssistPropertyWithMapType() throws Exception {
		data("foo.mapping", "java.util.Map<java.lang.String,java.lang.String>", null, "Nice little map");

		//Try in-pace completion
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

		//Try in-pace completion
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

		//Try in-pace completion
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

		//Try in-pace completion
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


//		assertCompletionsDisplayString(
//				"#This is a commment, and it shouldn't be erased\n" +
//				"server:\n" +
//				"  <*>",
//
//				"port",
//				"address"
//		);

		//TODO: content assist cases
		/*
		 foo:
		   <*>
		 more

		 */

	private void assertNoCompletions(String text) throws Exception {
		MockEditor editor = new MockEditor(text);
		assertEquals(0, getCompletions(editor).length);
	}

	private void assertCompletion(String before, String after) throws Exception {
		MockEditor editor = new MockEditor(before);
		ICompletionProposal completion = getFirstCompletion(editor);
		editor.apply(completion);
		String actual = editor.getText();
		assertEquals(after, actual);
	}

}
