/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.propertiesfileeditor.StsConfigMetadataRepositoryJsonLoader;
import org.springframework.ide.eclipse.propertiesfileeditor.util.AptUtils;

import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;

public class SpringPropertiesEditorTests extends SpringPropertiesEditorTestHarness {

	//TODO: List type is assignable (but parametric),
	//  - handle this in reconciling?

	public void testServerPortCompletion() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		assertCompletion("ser<*>", "server.port=8080<*>");
		assertCompletionDisplayString("ser<*>", "server.port=8080 : int Port where server listens for http.");
	}

	public void testLoggingLevelCompletion() throws Exception {
		data("logging.level", "java.util.Map<java.lang.String,java.lang.Object>", null, "Logging level per package.");
		assertCompletion("lolev<*>","logging.level.<*>");
	}

	public void testListCompletion() throws Exception {
		data("foo.bars", "java.util.List<java.lang.String>", null, "List of bars in foo.");
		assertCompletion("foba<*>","foo.bars=<*>");
	}

	public void testInetAddresCompletion() throws Exception {
		defaultTestData();
		assertCompletion("server.add<*>", "server.address=<*>");
	}

	public void testStringArrayCompletion() throws Exception {
		data("spring.freemarker.view-names", "java.lang.String[]", null, "White list of view names that can be resolved.");
		data("some.defaulted.array", "java.lang.String[]", new String[] {"a", "b", "c"} , "Stuff.");

		assertCompletion("spring.freemarker.vn<*>", "spring.freemarker.view-names=<*>");
		assertCompletion("some.d.a<*>", "some.defaulted.array=a,b,c<*>");
	}

	public void testEmptyPrefixProposalsSortedAlpabetically() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor("");
		ICompletionProposal[] completions = getCompletions(editor);
		assertTrue(completions.length>100); //should be many proposals
		String previous = null;
		for (ICompletionProposal c : completions) {
			String current = c.getDisplayString();
			if (previous!=null) {
				assertTrue("Incorrect order: \n   "+previous+"\n   "+current, previous.compareTo(current)<=0);
			}
			previous = current;
		}
	}

	public void testValueCompletion() throws Exception {
		defaultTestData();
		assertCompletions("liquibase.enabled=<*>",
				"liquibase.enabled=true<*>",
				"liquibase.enabled=false<*>"
		);

		assertCompletions("liquibase.enabled:<*>",
				"liquibase.enabled:true<*>",
				"liquibase.enabled:false<*>"
		);

		assertCompletions("liquibase.enabled = <*>",
				"liquibase.enabled = true<*>",
				"liquibase.enabled = false<*>"
		);

		assertCompletions("liquibase.enabled   <*>",
				"liquibase.enabled   true<*>",
				"liquibase.enabled   false<*>"
		);

		assertCompletions("liquibase.enabled=f<*>",
				"liquibase.enabled=false<*>"
		);

		assertCompletions("liquibase.enabled=t<*>",
				"liquibase.enabled=true<*>"
		);

		assertCompletions("liquibase.enabled:f<*>",
				"liquibase.enabled:false<*>"
		);

		assertCompletions("liquibase.enabled:t<*>",
				"liquibase.enabled:true<*>"
		);

		assertCompletions("liquibase.enabled = f<*>",
				"liquibase.enabled = false<*>"
		);

		assertCompletions("liquibase.enabled = t<*>",
				"liquibase.enabled = true<*>"
		);

		assertCompletions("liquibase.enabled   t<*>",
				"liquibase.enabled   true<*>"
		);

		//one more... for special char like '-' in the name

		assertCompletions("liquibase.check-change-log-location=t<*>",
				"liquibase.check-change-log-location=true<*>"
		);
	}


	public void testHoverInfos() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"#foo\n" +
				"# bar\n" +
				"server.port=8080\n" +
				"logging.level.com.acme=INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		assertHoverText(editor, "server.",
				"<b>server.port</b>"+
				"<br><a href=\"type%2Fjava.lang.Integer\">java.lang.Integer</a>"+
				"<br><br>Server HTTP port"
		);
		//Case 2: an object/map property has extra text after the property name
		assertHoverText(editor, "logging.", "<b>logging.level</b>");
	}

	public void testHoverInfosWithSpaces() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"#foo\n" +
				"# bar\n"+
				"\n" +
				"  server.port = 8080\n" +
				"  logging.level.com.acme = INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		assertHoverText(editor, "server.",
				"<b>server.port</b>"+
				"<br><a href=\"type%2Fjava.lang.Integer\">java.lang.Integer</a>"+
				"<br><br>Server HTTP port"
		);
		//Case 2: an object/map property has extra text after the property name
		assertHoverText(editor, "logging.", "<b>logging.level</b>");
	}

	public void testHoverLongAndShort() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		data("server.port.fancy", BOOLEAN, 8080, "Whether the port is fancy.");
		MockEditor editor = new MockEditor(
				"server.port=8080\n" +
				"server.port.fancy=true\n"
		);
		assertHoverText(editor, "server.", "<b>server.port</b>");
		assertHoverText(editor, "port.fa", "<b>server.port.fancy</b>");
	}


	public void testPredefinedProject() throws Exception {
		IProject p = createPredefinedProject("demo");
		IType type = JavaCore.create(p).findType("demo.DemoApplication");
		assertNotNull(type);
	}

	public void testEnableApt() throws Throwable {
		IProject p = createPredefinedProject("demo-live-metadata");
		IJavaProject jp = JavaCore.create(p);

		//Check some assumptions about the initial state of the test project (if these checks fail then
		// the test may be 'vacuous' since the things we are testing for already exist beforehand.
		assertFalse(AptUtils.isAptEnabled(jp));
		IFile metadataFile = JavaProjectUtil.getOutputFile(jp, StsConfigMetadataRepositoryJsonLoader.META_DATA_LOCATIONS[0]);
		assertFalse(metadataFile.exists());

		AptUtils.enableApt(jp);
		buildProject(jp);

		assertTrue(AptUtils.isAptEnabled(jp));
		assertTrue(metadataFile.exists()); //apt should create the json metadata file during project build.
		assertContains("\"name\": \"foo.counter\"", getContents(metadataFile));
	}

	public void testHyperlinkTargets() throws Exception {
		System.out.println(">>> testHyperlinkTargets");
		IProject p = createPredefinedProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		MockEditor editor = new MockEditor(
				"server.port=888\n" +
				"spring.datasource.login-timeout=1000\n" +
				"flyway.init-sqls=a,b,c\n"
		);

		assertLinkTargets(editor, "server",
				"org.springframework.boot.autoconfigure.web.ServerProperties.setPort(Integer)"
		);
		assertLinkTargets(editor, "data",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.hikariDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.tomcatDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.dbcpDataSource()"
		);
		assertLinkTargets(editor, "flyway",
				"org.springframework.boot.autoconfigure.flyway.FlywayProperties.setInitSqls(List<String>)");
		System.out.println("<<< testHyperlinkTargets");
	}

	public void testHyperlinkTargetsLoggingLevel() throws Exception {
		System.out.println(">>> testHyperlinkTargetsLoggingLevel");
		IProject p = createPredefinedProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		MockEditor editor = new MockEditor(
				"logging.level.com.acme=INFO\n"
		);
		assertLinkTargets(editor, "level",
				"org.springframework.boot.logging.LoggingApplicationListener"
		);
		System.out.println("<<< testHyperlinkTargetsLoggingLevel");
	}

	public void testReconcile() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server.port=8080\n" +
				"server.port.extracrap=8080\n" +
				"logging.level.com.acme=INFO\n" +
				"logging.snuggem=what?\n" +
				"bogus.no.good=true\n"
		);
		assertProblems(editor,
				".extracrap|Supbproperties are invalid",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);

	}

	public void testReconcileArrayNotation() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"borked=bad+\n" + //token problem, to make sure reconciler is working
				"server.ssl.ciphers[0]=foo\n" +
				"server.ssl.ciphers[${one}]=foo"
		);
		assertProblems(editor,
				"orked|unknown property"
				//no other problems
		);
	}

	public void testReconcileArrayNotationError() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server.ssl.ciphers[bork]=foo\n" +
				"server.ssl.ciphers[1=foo\n" +
				"server.ssl.ciphers[1]crap=foo\n" +
				"server.port[0]=8888"
		);
		assertProblems(editor,
				"bork|Integer",
				"[|matching ']'",
				"crap|extra text",
				"[0]|[...] notation is invalid"
				//no other problems
		);
	}

	public void testRelaxedNameReconciling() throws Exception {
		data("connection.remote-host", "java.lang.String", "service.net", null);
		data("foo-bar.name", "java.lang.String", null, null);
		MockEditor editor = new MockEditor(
				"bork=foo\n" +
				"connection.remote-host=alternate.net\n" +
				"connection.remoteHost=alternate.net\n" +
				"foo-bar.name=Charlie\n" +
				"fooBar.name=Charlie\n"
		);
		assertProblems(editor,
				"bork|unknown property"
				//no other problems
		);
	}

	public void testRelaxedNameReconcilingErrors() throws Exception {
		//Tricky with relaxec names: the error positions have to be moved
		// around because the relaxed names aren't same length as the
		// canonical ids.
		data("foo-bar-zor.enabled", "java.lang.Boolean", null, null);
		MockEditor editor = new MockEditor(
				"fooBarZor.enabled=notBoolean\n" +
				"fooBarZor.enabled.subprop=true\n"
		);
		assertProblems(editor,
				"notBoolean|Boolean",
				".subprop|Supbproperties are invalid"
		);
	}

	public void testRelaxedNameContentAssist() throws Exception {
		data("foo-bar-zor.enabled", "java.lang.Boolean", null, null);
		assertCompletion("fooBar<*>", "foo-bar-zor.enabled=<*>");
	}

	public void testReconcileValues() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server.port=badPort\n" +
				"liquibase.enabled=nuggels"
		);
		assertProblems(editor,
				"badPort|Integer",
				"nuggels|Boolean"
		);
	}

	public void testNoReconcileInterpolatedValues() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server.port=${port}\n" +
				"liquibase.enabled=nuggels"
		);
		assertProblems(editor,
				//no problem should be reported for ${port}
				"nuggels|Boolean"
		);
	}

	public void testReconcileValuesWithSpaces() throws Exception {
		defaultTestData();
		MockEditor editor = new MockEditor(
				"server.port  =   badPort\n" +
				"liquibase.enabled   nuggels  \n" +
				"liquibase.enabled   : snikkers"
		);
		assertProblems(editor,
				"badPort|Integer",
				"nuggels|Boolean",
				"snikkers|Boolean"
		);
	}


	public void testReconcileWithExtraSpaces() throws Exception {
		defaultTestData();
		//Same test as previous but with extra spaces to make things more confusing
		MockEditor editor = new MockEditor(
				"   server.port   =  8080  \n" +
				"\n" +
				"  server.port.extracrap = 8080\n" +
				" logging.level.com.acme  : INFO\n" +
				"logging.snuggem = what?\n" +
				"bogus.no.good=  true\n"
		);
		assertProblems(editor,
				".extracrap|Supbproperties are invalid",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);
	}

}
