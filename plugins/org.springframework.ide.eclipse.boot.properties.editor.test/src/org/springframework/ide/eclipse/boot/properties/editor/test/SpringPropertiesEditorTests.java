/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertContains;

import java.time.Duration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.StsConfigMetadataRepositoryJsonLoader;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.CachingValueProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.AptUtils;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;

import com.google.common.collect.ImmutableList;

public class SpringPropertiesEditorTests extends SpringPropertiesEditorTestHarness {

	public void testServerPortCompletion() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		if (SpringPropertiesCompletionEngine.DEFAULT_VALUE_INCLUDED) {
			assertCompletion("ser<*>", "server.port=8080<*>");
		} else {
			assertCompletion("ser<*>", "server.port=<*>");
		}
		assertCompletionDisplayString("ser<*>", "server.port : int");
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
		if (SpringPropertiesCompletionEngine.DEFAULT_VALUE_INCLUDED) {
			assertCompletion("some.d.a<*>", "some.defaulted.array=a,b,c<*>");
		} else {
			assertCompletion("some.d.a<*>", "some.defaulted.array=<*>");
		}
	}

	public void testEmptyPrefixProposalsSortedAlpabetically() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor("");
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
		assertCompletionsVariations("liquibase.enabled=<*>",
				"liquibase.enabled=false<*>",
				"liquibase.enabled=true<*>"
		);

		assertCompletionsVariations("liquibase.enabled:<*>",
				"liquibase.enabled:false<*>",
				"liquibase.enabled:true<*>"
		);

		assertCompletionsVariations("liquibase.enabled = <*>",
				"liquibase.enabled = false<*>",
				"liquibase.enabled = true<*>"
		);

		assertCompletionsVariations("liquibase.enabled   <*>",
				"liquibase.enabled   false<*>",
				"liquibase.enabled   true<*>"
		);

		assertCompletionsVariations("liquibase.enabled=f<*>",
				"liquibase.enabled=false<*>"
		);

		assertCompletionsVariations("liquibase.enabled=t<*>",
				"liquibase.enabled=true<*>"
		);

		assertCompletionsVariations("liquibase.enabled:f<*>",
				"liquibase.enabled:false<*>"
		);

		assertCompletionsVariations("liquibase.enabled:t<*>",
				"liquibase.enabled:true<*>"
		);

		assertCompletionsVariations("liquibase.enabled = f<*>",
				"liquibase.enabled = false<*>"
		);

		assertCompletionsVariations("liquibase.enabled = t<*>",
				"liquibase.enabled = true<*>"
		);

		assertCompletionsVariations("liquibase.enabled   t<*>",
				"liquibase.enabled   true<*>"
		);

		//one more... for special char like '-' in the name

		assertCompletionsVariations("liquibase.check-change-log-location=t<*>",
				"liquibase.check-change-log-location=true<*>"
		);
	}


	public void testHoverInfos() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"#foo\n" +
				"# bar\n" +
				"server.port=8080\n" +
				"logging.level.com.acme=INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		assertHoverText(editor, "server.",
				"<b>server.port</b>"
		);
		//Case 2: an object/map property has extra text after the property name
		assertHoverText(editor, "logging.", "<b>logging.level</b>");
	}

	public void testHoverInfosWithSpaces() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"#foo\n" +
				"# bar\n"+
				"\n" +
				"  server.port = 8080\n" +
				"  logging.level.com.acme = INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		assertHoverText(editor, "server.",
				"<b>server.port</b>"
		);
		//Case 2: an object/map property has extra text after the property name
		assertHoverText(editor, "logging.", "<b>logging.level</b>");
	}

	public void testHoverLongAndShort() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		data("server.port.fancy", BOOLEAN, 8080, "Whether the port is fancy.");
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"server.port=8080\n" +
				"server.port.fancy=true\n"
		);
		assertHoverText(editor, "server.", "<b>server.port</b>");
		assertHoverText(editor, "port.fa", "<b>server.port.fancy</b>");
	}


	public void testPredefinedProject() throws Exception {
		IProject p = createPredefinedMavenProject("demo");
		IType type = JavaCore.create(p).findType("demo.DemoApplication");
		assertNotNull(type);
	}

	public void testEnableApt() throws Throwable {
		IProject p = createPredefinedMavenProject("demo-live-metadata");
		IJavaProject jp = JavaCore.create(p);

		//Check some assumptions about the initial state of the test project (if these checks fail then
		// the test may be 'vacuous' since the things we are testing for already exist beforehand.
		assertTrue(AptUtils.isAptEnabled(jp));
		IFile metadataFile = JavaProjectUtil.getOutputFile(jp, StsConfigMetadataRepositoryJsonLoader.META_DATA_LOCATIONS[0]);
		assertTrue(metadataFile.exists());
		assertContains("\"name\": \"foo.counter\"", getContents(metadataFile));
	}

	public void testHyperlinkTargets() throws Exception {
		System.out.println(">>> testHyperlinkTargets");
		IProject p = createPredefinedMavenProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		MockPropertiesEditor editor = new MockPropertiesEditor(
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
		IProject p = createPredefinedMavenProject("demo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"logging.level.com.acme=INFO\n"
		);
		assertLinkTargets(editor, "level",
				"org.springframework.boot.logging.LoggingApplicationListener"
		);
		System.out.println("<<< testHyperlinkTargetsLoggingLevel");
	}

	public void testReconcile() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"server.port=8080\n" +
				"server.port.extracrap=8080\n" +
				"logging.level.com.acme=INFO\n" +
				"logging.snuggem=what?\n" +
				"bogus.no.good=true\n"
		);
		assertProblems(editor,
				".extracrap|Can't use '.' navigation",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);

	}

	public void testReconcilePojoArray() throws Exception {
		IProject p = createPredefinedMavenProject("demo-list-of-pojo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Foo"));

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"token.bad.guy=problem\n"+
				"volder.foo.list[0].name=Kris\n" +
				"volder.foo.list[0].description=Kris\n" +
				"volder.foo.list[0].roles[0]=Developer\n"+
				"volder.foo.list[0]garbage=Grable\n"+
				"volder.foo.list[0].bogus=Bad\n"
		);

		//This is the more ambitious requirement but it is not implemented yet.
		assertProblems(editor,
				"token.bad.guy|unknown property",
				//'name' is ok
				//'description' is ok
				"garbage|'.' or '['",
				"bogus|has no property"
		);
	}

	public void testPojoArrayCompletions() throws Exception {
		IProject p = createPredefinedMavenProject("demo-list-of-pojo");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Foo"));

		assertCompletionsVariations("volder.foo.l<*>", "volder.foo.list[<*>");
		assertCompletionsDisplayString("volder.foo.list[0].<*>",
				"name : String",
				"description : String",
				"roles : List<String>");

		assertCompletionsVariations("volder.foo.list[0].na<*>",
				"volder.foo.list[0].name=<*>"
		);
		assertCompletionsVariations("volder.foo.list[0].d<*>",
				"volder.foo.list[0].description=<*>"
		);
		assertCompletionsVariations("volder.foo.list[0].rl<*>",
				"volder.foo.list[0].roles=<*>"
		);
	}

	public void testReconcileArrayNotation() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"borked=bad+\n" + //token problem, to make sure reconciler is working
				"security.user.role[0]=foo\n" +
				"security.user.role[${one}]=foo"
		);
		assertProblems(editor,
				"orked|unknown property"
				//no other problems
		);
	}

	public void testReconcileArrayNotationError() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"security.user.role[bork]=foo\n" +
				"security.user.role[1=foo\n" +
				"security.user.role[1]crap=foo\n" +
				"server.port[0]=8888\n" +
				"spring.thymeleaf.view-names[1]=hello" //This is okay now. Boot handles this notation for arrays
		);
		assertProblems(editor,
				"bork|Integer",
				"[|matching ']'",
				"crap|'.' or '['",
				"[0]|Can't use '[..]'"
				//no other problems
		);
	}

	public void testRelaxedNameReconciling() throws Exception {
		data("connection.remote-host", "java.lang.String", "service.net", null);
		data("foo-bar.name", "java.lang.String", null, null);
		MockPropertiesEditor editor = new MockPropertiesEditor(
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
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"fooBarZor.enabled=notBoolean\n" +
				"fooBarZor.enabled.subprop=true\n"
		);
		assertProblems(editor,
				"notBoolean|boolean",
				".subprop|Can't use '.' navigation"
		);
	}

	public void testRelaxedNameContentAssist() throws Exception {
		data("foo-bar-zor.enabled", "java.lang.Boolean", null, null);
		assertCompletion("fooBar<*>", "foo-bar-zor.enabled=<*>");
	}

	public void testReconcileValues() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"server.port=badPort\n" +
				"liquibase.enabled=nuggels"
		);
		assertProblems(editor,
				"badPort|'int'",
				"nuggels|'boolean'"
		);
	}

	public void testNoReconcileInterpolatedValues() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"server.port=${port}\n" +
				"liquibase.enabled=nuggels"
		);
		assertProblems(editor,
				//no problem should be reported for ${port}
				"nuggels|'boolean'"
		);
	}

	public void testReconcileValuesWithSpaces() throws Exception {
		defaultTestData();
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"server.port  =   badPort\n" +
				"liquibase.enabled   nuggels  \n" +
				"liquibase.enabled   : snikkers"
		);
		assertProblems(editor,
				"badPort|'int'",
				"nuggels|'boolean'",
				"snikkers|'boolean'"
		);
	}


	public void testReconcileWithExtraSpaces() throws Exception {
		defaultTestData();
		//Same test as previous but with extra spaces to make things more confusing
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"   server.port   =  8080  \n" +
				"\n" +
				"  server.port.extracrap = 8080\n" +
				" logging.level.com.acme  : INFO\n" +
				"logging.snuggem = what?\n" +
				"bogus.no.good=  true\n"
		);
		assertProblems(editor,
				".extracrap|Can't use '.' navigation",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);
	}

	public void testEnumPropertyCompletionInsideCommaSeparateList() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		data("foo.colors", "java.util.List<demo.Color>", null, "A foonky list");

		//Completion requested right after '=' sign:
		assertCompletionsDisplayString("foo.colors=<*>", "red", "green", "blue");
		assertCompletionWithLabel("foo.colors=<*>", "red", "foo.colors=red<*>");
		assertCompletion("foo.colors=R<*>", "foo.colors=RED<*>");
		assertCompletion("foo.colors=g<*>", "foo.colors=green<*>");
		assertCompletion("foo.colors=B<*>", "foo.colors=BLUE<*>");

		//Completion requested after ','
		assertCompletionsDisplayString("foo.colors=red,<*>", "red", "green", "blue");
		assertCompletionWithLabel("foo.colors=red,<*>", "green", "foo.colors=red,green<*>");
		assertCompletion("foo.colors=RED,R<*>", "foo.colors=RED,RED<*>");
		assertCompletion("foo.colors=RED,G<*>", "foo.colors=RED,GREEN<*>");
		assertCompletion("foo.colors=RED,B<*>", "foo.colors=RED,BLUE<*>");
	}

	public void testEnumPropertyCompletion() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");

		assertCompletion("foo.c<*>", "foo.color=<*>"); //Should add the '=' because enums are 'simple' values.

		assertCompletion("foo.color=R<*>", "foo.color=RED<*>");
		assertCompletion("foo.color=G<*>", "foo.color=GREEN<*>");
		assertCompletion("foo.color=B<*>", "foo.color=BLUE<*>");
		assertCompletionsDisplayString("foo.color=<*>",
				"red", "green", "blue"
		);
	}

	public void testEnumPropertyReconciling() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"foo.color=BLUE\n"+
				"foo.color=RED\n"+
				"foo.color=GREEN\n"+
				"foo.color.bad=BLUE\n"+
				"foo.color=Bogus\n"
		);

		assertProblems(editor,
				".bad|Can't use '.' navigation",
				"Bogus|Color"
		);
	}

	public void testEnumMapValueCompletion() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		assertCompletionsVariations("foo.nam<*>",
				"foo.name-colors.<*>",
				"foo.color-names.<*>"
		);

		assertCompletionsDisplayString("foo.name-colors.something=<*>",
				"red", "green", "blue"
		);
		assertCompletionsVariations("foo.name-colors.something=G<*>", "foo.name-colors.something=GREEN<*>");
	}

	public void testEnumMapValueReconciling() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		data("foo.name-colors", "java.util.Map<java.lang.String,demo.Color>", null, "Map with colors in its values");

		assertNotNull(jp.findType("demo.Color"));

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"foo.name-colors.jacket=BLUE\n" +
				"foo.name-colors.hat=RED\n" +
				"foo.name-colors.pants=GREEN\n" +
				"foo.name-colors.wrong=NOT_A_COLOR\n"
		);
		assertProblems(editor,
				"NOT_A_COLOR|Color"
		);
	}

	public void testEnumMapKeyCompletion() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		data("foo.color-names", "java.util.Map<demo.Color,java.lang.String>", null, "Map with colors in its keys");
		data("foo.color-data", "java.util.Map<demo.Color,demo.ColorData>", null, "Map with colors in its keys, and pojo in values");
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		//Map Enum -> String:
		assertCompletionsVariations("foo.colnam<*>", "foo.color-names.<*>");
		assertCompletionsVariations("foo.color-names.<*>",
				"foo.color-names.blue=<*>",
				"foo.color-names.green=<*>",
				"foo.color-names.red=<*>"
		);
		assertCompletionsDisplayString("foo.color-names.<*>",
				"red : String", "green : String", "blue : String"
		);
		assertCompletionsVariations("foo.color-names.B<*>",
				"foo.color-names.BLUE=<*>"
		);

		//Map Enum -> Pojo:
		assertCompletionsVariations("foo.coldat<*>", "foo.color-data.<*>");
		assertCompletionsVariations("foo.color-data.<*>",
				"foo.color-data.blue.<*>",
				"foo.color-data.green.<*>",
				"foo.color-data.red.<*>"
		);
		assertCompletionsVariations("foo.color-data.B<*>",
				"foo.color-data.BLUE.<*>"
		);
		assertCompletionsDisplayString("foo.color-data.<*>",
				"blue : demo.ColorData", "green : demo.ColorData", "red : demo.ColorData"
		);
	}

	public void testEnumMapKeyReconciling() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"foo.color-names.RED=Rood\n"+
				"foo.color-names.GREEN=Groen\n"+
				"foo.color-names.BLUE=Blauw\n" +
				"foo.color-names.NOT_A_COLOR=Wrong\n" +
				"foo.color-names.BLUE.bad=Blauw\n"
		);
		assertProblems(editor,
				"NOT_A_COLOR|Color",
				"BLUE.bad|Color" //because value type is not dotable the dots will be taken to be part of map key
		);
	}

	public void testPojoCompletions() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		assertCompletion("foo.dat<*>", "foo.data.<*>");

		assertCompletionsDisplayString("foo.data.",
				"wavelen : double",
				"name : String",
				"next : demo.Color[RED, GREEN, BLUE]",
				"nested : demo.ColorData",
				"children : List<demo.ColorData>",
				"mapped-children : Map<String, demo.ColorData>",
				"color-children : Map<demo.Color[RED, GREEN, BLUE], demo.ColorData>",
				"tags : List<String>",
				"funky : boolean"
		);

		assertCompletionsVariations("foo.data.wav<*>", "foo.data.wavelen=<*>");
		assertCompletionsVariations("foo.data.nam<*>", "foo.data.name=<*>");
		assertCompletionsVariations("foo.data.nex<*>", "foo.data.next=<*>");
		assertCompletionsVariations("foo.data.nes<*>", "foo.data.nested.<*>");
		assertCompletionsVariations("foo.data.chi<*>",
				"foo.data.children[<*>",
				"foo.data.color-children.<*>", //fuzzy
				"foo.data.mapped-children.<*>" //fuzzy
		);
		assertCompletionsVariations("foo.data.tag<*>", "foo.data.tags=<*>");
		assertCompletionsVariations("foo.data.map<*>", "foo.data.mapped-children.<*>");
		assertCompletionsVariations("foo.data.col<*>", "foo.data.color-children.<*>");
	}

	public void testPojoReconciling() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		MockPropertiesEditor editor = new MockPropertiesEditor(
			"foo.data.bogus=Something\n" +
			"foo.data.wavelen=3.0\n" +
			"foo.data.wavelen=not a double\n" +
			"foo.data.wavelen.more=3.0\n" +
			"foo.data.wavelen[0]=3.0\n"
		);
		assertProblems(editor,
				"bogus|no property",
				"not a double|'double'",
				".more|Can't use '.' navigation",
				"[0]|Can't use '[..]' navigation"
		);
	}

	public void testListOfAtomicCompletions() throws Exception {
		data("foo.slist", "java.util.List<java.lang.String>", null, "list of strings");
		data("foo.ulist", "java.util.List<Unknown>", null, "list of strings");
		data("foo.dlist", "java.util.List<java.lang.Double>", null, "list of doubles");
		assertCompletionsVariations("foo.u<*>", "foo.ulist[<*>");
		assertCompletionsVariations("foo.d<*>", "foo.dlist=<*>");
		assertCompletionsVariations("foo.sl<*>", "foo.slist=<*>");
	}

	public void testMapKeyDotInterpretation() throws Exception {
		//Interpretation of '.' changes depending on the domain type (i.e. when domain type is
		//is a simple type got which '.' navigation is invalid then the '.' is 'eaten' by the key.

		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		data("atommap", "java.util.Map<java.lang.String,java.lang.Integer>", null, "map of atomic data");
		data("objectmap", "java.util.Map<java.lang.String,java.lang.Object>", null, "map of atomic object (recursive map)");
		data("enummap", "java.util.Map<java.lang.String,demo.Color>", null, "map of enums");
		data("pojomap", "java.util.Map<java.lang.String,demo.ColorData>", null, "map of pojos");

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"atommap.something.with.dots=Vaporize\n" +
				"atommap.something.with.bracket[0]=Brackelate\n" +
				"objectmap.other.with.dots=Objectify\n" +
				"enummap.more.dots=Enumerate\n" +
				"pojomap.do.some.dots=Pojodot\n" +
				"pojomap.bracket.and.dots[1]=lala\n" +
				"pojomap.zozo[2]=lala\n"
		);
		assertProblems(editor,
				"Vaporize|'int'",
				"[0]|Can't use '[..]'",
				//objectmap okay
				"Enumerate|Color",
				"some|no property",
				"and|no property",
				"[2]|Can't use '[..]'"
		);

		assertCompletionsVariations("enummap.more.dots=R<*>",
				"enummap.more.dots=RED<*>",
				"enummap.more.dots=GREEN<*>" //fuzzy match: G(R)EEN
		);
	}

	public void testMapKeyDotInterpretationInPojo() throws Exception {
		//Similar to testMapKeyDotInterpretation but this time maps are not attached to property
		// directly but via a pojo property

		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"foo.color-names.BLUE.dot=Blauw\n"+
				"foo.color-data.RED.name=Good\n"+
				"foo.color-data.GREEN.bad=Bad\n"+
				"foo.color-data.GREEN.wrong[1]=Wrong\n"
		);
		assertProblems(editor,
				"BLUE.dot|Color", //dot is eaten so this is an error
				"bad|no property", //dot not eaten so '.bad' is accessing a property
				"wrong|no property"
		);

		assertCompletionsVariations("foo.color-data.RED.ch<*>",
				"foo.color-data.RED.children[<*>",
				"foo.color-data.RED.color-children.<*>",
				"foo.color-data.RED.mapped-children.<*>"
		);
	}

	public void testEnumsInLowerCaseReconciling() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"simple.pants.size=NOT_A_SIZE\n"+
				"simple.pants.size=EXTRA_SMALL\n"+
				"simple.pants.size=extra-small\n"+
				"simple.pants.size=small\n"+
				"simple.pants.size=SMALL\n"
		);
		assertProblems(editor,
				"NOT_A_SIZE|ClothingSize"
		);

		editor = new MockPropertiesEditor(
				"foo.color-names.red=Rood\n"+
				"foo.color-names.green=Groen\n"+
				"foo.color-names.blue=Blauw\n" +
				"foo.color-names.not-a-color=Wrong\n" +
				"foo.color-names.blue.bad=Blauw\n"
		);
		assertProblems(editor,
				"not-a-color|Color",
				"blue.bad|Color" //because value type is not dotable the dots will be taken to be part of map key
		);

		editor = new MockPropertiesEditor(
				"foo.color-data.red.next=green\n" +
				"foo.color-data.red.next=not a color\n" +
				"foo.color-data.red.bogus=green\n" +
				"foo.color-data.red.name=Rood\n"
		);
		assertProblems(editor,
				"not a color|Color",
				"bogus|no property"
		);
	}

	public void testEnumsInLowerCaseContentAssist() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		assertCompletionsVariations("simple.pants.size=S<*>",
				"simple.pants.size=SMALL<*>",
				"simple.pants.size=EXTRA_SMALL<*>"
		);
		assertCompletionsVariations("simple.pants.size=s<*>",
				"simple.pants.size=small<*>",
				"simple.pants.size=extra-small<*>"
		);
		assertCompletionsVariations("simple.pants.size=ex<*>",
				"simple.pants.size=extra-large<*>",
				"simple.pants.size=extra-small<*>"
		);
		assertCompletionsVariations("simple.pants.size=EX<*>",
				"simple.pants.size=EXTRA_LARGE<*>",
				"simple.pants.size=EXTRA_SMALL<*>"
		);
		assertCompletionsDisplayString("foo.color=<*>", "red", "green", "blue");

		assertCompletionsVariations("foo.color-data.R<*>",
				"foo.color-data.RED.<*>",
				"foo.color-data.GREEN.<*>"
		);
		assertCompletionsVariations("foo.color-data.r<*>",
				"foo.color-data.red.<*>",
				"foo.color-data.green.<*>"
		);
		assertCompletionsVariations("foo.color-data.<*>",
				"foo.color-data.blue.<*>",
				"foo.color-data.green.<*>",
				"foo.color-data.red.<*>"
		);

		assertCompletionsVariations("foo.color-data.red.na<*>", "foo.color-data.red.name=<*>");
	}

	public void testNavigationProposalAfterRelaxedPropertyName() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		assertCompletionsVariations("foo.colorData.b<*>", "foo.colorData.blue.<*>");
		assertCompletionsVariations("foo.colorData.red.na<*>", "foo.colorData.red.name=<*>");
	}

	public void testValueProposalAssignedToRelaxedPropertyName() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		data("relaxed-color", "demo.Color", null, "A soothing color");

		assertCompletion("relaxed-color=b<*>", "relaxed-color=blue<*>");
		assertCompletion("relaxedColor=b<*>", "relaxedColor=blue<*>");
	}

	public void testReconcileDeprecatedProperty() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"# a comment\n"+
				"error.path=foo\n"
		);

		deprecate("error.path", "server.error.path", null);
		assertProblems(editor,
				"error.path|Deprecated: Use 'server.error.path'"
				//no other problems
		);

		deprecate("error.path", "server.error.path", "This is old.");
		assertProblems(editor,
				"error.path|Deprecated: Use 'server.error.path' instead. Reason: This is old."
				//no other problems
		);

		deprecate("error.path", null, "This is old.");
		assertProblems(editor,
				"error.path|Deprecated: This is old."
				//no other problems
		);

		deprecate("error.path", null, null);
		assertProblems(editor,
				"error.path|Deprecated!"
				//no other problems
		);

	}

	public void testDeprecatedPropertyCompletion() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		data("server.error.path", "java.lang.String", null, "Path of the error controller.");
		deprecate("error.path", "server.error.path", "This is old.");
		assertCompletionsDisplayString("error.pa<*>",
				"server.error.path : String", // should be first because it is not deprecated, even though it is not as good a pattern match
				"error.path : String"
		);
		//TODO: could we check that 'deprecated' completions are formatted with 'strikethrough font?
		assertStyledCompletions("error.pa<*>",
				StyledStringMatcher.plainFont("server.error.path : String"),
				StyledStringMatcher.strikeout("error.path")
		);
	}

	public void testDeprecatedPropertyHoverInfo() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		MockPropertiesEditor editor = new MockPropertiesEditor(
				"# a comment\n"+
				"error.path=foo\n"
		);

		deprecate("error.path", "server.error.path", null);
		assertHoverText(editor, "path", "<s>error.path</s> -&gt; server.error.path");
		assertHoverText(editor, "path", "<b>Deprecated!</b>");

		deprecate("error.path", "server.error.path", "This is old.");
		assertHoverText(editor, "path", "<s>error.path</s> -&gt; server.error.path");
		assertHoverText(editor, "path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, "This is old.");
		assertHoverText(editor, "path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, null);
		assertHoverText(editor, "path", "<b>Deprecated!</b>");
	}

	public void testDeprecatedBeanPropertyReconcile() throws Exception {
		IProject jp = createPredefinedMavenProject("demo");
		useProject(jp);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"# comment\n" +
				"foo.name=Old faithfull\n" +
				"foo.new-name=New and fancy\n" +
				"foo.alt-name=alternate\n"
		);
		assertProblems(editor,
				"name|Deprecated",
				"alt-name|Property 'alt-name' of type 'demo.Deprecater' is Deprecated: Use 'something.else' instead. Reason: No good anymore"
		);
	}

	public void testDeprecatedBeanPropertyCompletions() throws Exception {
		IProject jp = createPredefinedMavenProject("demo");
		useProject(jp);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		assertStyledCompletions("foo.nam<*>",
				StyledStringMatcher.plainFont("new-name : String"),
				StyledStringMatcher.strikeout("name"),
				StyledStringMatcher.strikeout("alt-name")
		);
	}

	public void testCharsetCompletions() throws Exception {
		data("foobar.encoding", "java.nio.charset.Charset", null, "The charset-encoding to use for foobars");

		assertCompletions(
				"foobar.enco<*>"
				, // ==>
				"foobar.encoding=<*>"
		);

		assertCompletionWithLabel(
				"foobar.encoding=UT<*>"
				,
				"UTF-8"
				,
				"foobar.encoding=UTF-8<*>"
		);
	}

	public void testLocaleCompletions() throws Exception {
		data("foobar.locale", "java.util.Locale", null, "Yada yada");

		assertCompletions(
				"foobar.loca<*>"
				, // ==>
				"foobar.locale=<*>"
		);

		assertCompletionWithLabel(
				"foobar.locale=en<*>"
				,
				"en_CA"
				,
				"foobar.locale=en_CA<*>"
		);
	}

	public void testPropertyValueHintCompletions() throws Exception {
		//Test that 'value hints' work when property name is associated with 'value' hints.
		// via boot metadata.

		//TODO: this should also work when hints associated with a
		//  map property key
		//  map property value
		//  list property value

		useProject(createPredefinedMavenProject("boot13"));
		assertCompletionsDisplayString(
				"spring.http.converters.preferred-json-mapper=<*>\n"
				, //=>
				"gson",
				"jackson"
		);
	}

	public void testPropertyListHintCompletions() throws Exception {
		useProject(createPredefinedMavenProject("boot13"));

		assertCompletion(
				"management.health.status.ord<*>"
				, //=>
				"management.health.status.order=<*>"
		);

		assertCompletionsDisplayString(
				"management.health.status.order=<*>"
				, //=>
				"DOWN",
				"OUT_OF_SERVICE",
				"UNKNOWN",
				"UP"
		);

		assertCompletionsDisplayString(
				"management.health.status.order=DOWN,<*>"
				, //=>
				"DOWN",
				"OUT_OF_SERVICE",
				"UNKNOWN",
				"UP"
		);
	}

	public void testPropertyMapValueCompletions() throws Exception {
		useProject(createPredefinedMavenProject("boot13"));

		assertCompletionsDisplayString(
				"logging.level.some: <*>"
				, // =>
				"trace",
				"debug",
				"info",
				"warn",
				"error",
				"fatal",
				"off"
		);

		assertCompletionsDisplayString(
				"logging.level.some.package: <*>"
				, // =>
				"trace",
				"debug",
				"info",
				"warn",
				"error",
				"fatal",
				"off"
		);
	}

	public void testPropertyMapKeyCompletions() throws Exception {
		useProject(createPredefinedMavenProject("boot13"));
		assertCompletionWithLabel(
				"logging.level.<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);

		assertCompletionWithLabel(
				"logging.level.r<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);

		assertCompletionWithLabel(
				"logging.level.ot<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);
	}


	public void test_STS_3335_reconcile_list_nested_in_Map_of_String() throws Exception {
		MockPropertiesEditor editor;
		useProject(createPredefinedMavenProject("demo-sts-4335"));

		editor = new MockPropertiesEditor(
				"test-map.test-list-object.color-list[0]=not-a-color\n"+
				"test-map.test-list-object.color-list[1]=RED\n"+
				"test-map.test-list-object.color-list[2]=GREEN\n"
		);
		assertProblems(editor,
				"not-a-color|Expecting 'com.wellsfargo.lendingplatform.web.config.Color"
		);

		editor = new MockPropertiesEditor(
				"test-map.test-list-object.string-list[0]=not-a-color\n"+
				"test-map.test-list-object.string-list[1]=RED\n"+
				"test-map.test-list-object.string-list[2]=GREEN\n"
		);
		assertProblems(editor /*NONE*/);
	}


	public void test_STS_3335_completions_list_nested_in_Map_of_String() throws Exception {
		useProject(createPredefinedMavenProject("demo-sts-4335"));

		assertCompletions(
				"test-map.some-string-key.col<*>"
				, // =>
				"test-map.some-string-key.color-list=<*>"
		);

		assertCompletionsDisplayString(
				"test-map.some-string-key.color-list[0]=<*>\n"
				, // =>
				"red", "green", "blue"
		);

		assertCompletionsDisplayString(
				"test-map.some-string-key.color-list[0]=<*>\n"
				, // =>
				"red", "green", "blue"
		);
	}

	public void testSimpleResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("boot13"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");

		assertCompletion(
				"nicer<*>\n"
				,// =>
				"my.nice.resource=<*>\n"
		);

		assertCompletionsDisplayString(
				"my.nice.resource=<*>\n"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);
	}

	public void testClasspathResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("boot13"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");

		assertCompletionsDisplayString(
				"my.nice.resource=classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		//Test 'list item' context:

		assertCompletionsDisplayString(
				"my.nice.list[0]=<*>"
				,// =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);

		assertCompletionsDisplayString(
				"my.nice.list[0]=classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=classpath:app<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=classpath:application.yml<*>\n"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=  classpath:app<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=  classpath:application.yml<*>\n"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=classpath:<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=classpath:application.yml<*>\n"
		);

		//Test 'raw node' context

		// do we find resources in sub-folders too?
		assertCompletionWithLabel(
				"my.nice.resource=classpath:word<*>\n"
				,//===============
				"classpath:stuff/wordlist.txt"
				,// =>
				"my.nice.resource=classpath:stuff/wordlist.txt<*>\n"
		);
	}

	public void testClasspathResourceCompletionInCommaList() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("boot13"));
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");
		data("my.nice.array", "org.springframework.core.io.Resource[]", null, "A nice array of resources.");

		for (String kind : ImmutableList.of("list", "array")) {
			assertCompletionWithLabel(
				"my.nice."+kind+"=classpath:<*>"
				,//===========
				"classpath:stuff/wordlist.txt"
				,//=>
				"my.nice."+kind+"=classpath:stuff/wordlist.txt<*>"
			);

			assertCompletionsDisplayString(
				"my.nice."+kind+"=<*>"
				,// =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
			);

			assertCompletionWithLabel(
				"my.nice."+kind+"=classpath:stuff/wordlist.txt,classpath:app<*>"
				,//===========
				"classpath:application.yml"
				,//=>
				"my.nice."+kind+"=classpath:stuff/wordlist.txt,classpath:application.yml<*>"
			);
		}
	}

	public void testCommaListReconcile() throws Exception {
		IProject p = createPredefinedMavenProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));

		data("my.colors", "java.util.List<demo.Color>", null, "Ooh! nice colors!");

		MockPropertiesEditor editor = new MockPropertiesEditor(
				"#comment\n" +
				"my.colors=RED, green, not-a-color , BLUE"
		);
		assertProblems(editor,
				"not-a-color|demo.Color"
		);

		//TODO: commalist spans multiple lines, using \ at end of line

		/*
test case:
my.welcome.colors=\
	red,\
	green,\
	blue

		 */
	}

//	public void testContentAssistAfterRBrack() throws Exception {
//		//TODO: content assist after ] (auto insert leading '.' if necessary)
//	}

	//public void


	//TODO: relaxed names for navigation operations (i.e. object properties and enum map keys)



}
