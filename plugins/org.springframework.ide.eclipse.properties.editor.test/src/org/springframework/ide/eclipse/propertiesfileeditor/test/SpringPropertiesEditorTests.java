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
import org.springframework.ide.eclipse.propertiesfileeditor.StsConfigMetadataRepositoryJsonLoader;
import org.springframework.ide.eclipse.propertiesfileeditor.util.AptUtils;
import org.springframework.ide.eclipse.propertiesfileeditor.util.JavaProjectUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class SpringPropertiesEditorTests extends SpringPropertiesEditorTestHarness {
	
	//TODO: List type is assignable (but paramteric),
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
		StsTestUtil.buildProject(jp);
		
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
				"spring.datasource.login-timeout=1000\n"
		);
		
		assertLinkTargets(editor, "server", 
				"org.springframework.boot.autoconfigure.web.ServerProperties"
		);
		assertLinkTargets(editor, "data", 
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.hikariDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.tomcatDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.dbcpDataSource()"
		);
		System.out.println("<<< testHyperlinkTargets");
	}

	public void testReconcile() throws Exception {
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
	
	public void testReconcileValues() throws Exception {
		MockEditor editor = new MockEditor(
				"server.port=badPort\n" + 
				"liquibase.enabled=nuggels"
		);
		assertProblems(editor,
				"badPort|Integer",
				"nuggels|Boolean"
		);
	}
	
	public void testReconcileValuesWithSpaces() throws Exception {
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
