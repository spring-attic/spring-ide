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

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertyProblem;

public class SpringPropertiesEditorTests extends SpringPropertiesEditorTestHarness {
	
	//TODO: List type is assignable (but paramteric),
	//  - handle this in reconciling?
	//  - add test for completion (does it add '=' as expected?)
	
	public void testServerPortCompletion() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		assertCompletion("ser<*>", "server.port=8080<*>");
		assertCompletionDisplayString("ser<*>", "server.port=8080 : int Port where server listens for http.");
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

	public void testLoggingLevelCompletion() throws Exception {
		data("logging.level", "java.util.Map<java.lang.String,java.lang.Object>", null, "Logging level per package.");
		assertCompletion("lolev<*>","logging.level.<*>");
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
	
	/**
	 * Check that a 'expectedProblems' are found by the reconciler. Expected problems are
	 * specified by string of the form "${locationSnippet}|${messageSnippet}". The locationSnippet
	 * defines the location where the error marker is expected (the first places where the
	 * text occurs in the editor) and the message snippet must be found in the corresponding
	 * error marker
	 * @param editor
	 * @param expectedProblems
	 * @throws BadLocationException
	 */
	public void assertProblems(MockEditor editor, String... expectedProblems) throws BadLocationException {
		defaultTestData();
		List<SpringPropertyProblem> actualProblems = reconcile(editor);
		String bad = null;
		if (actualProblems.size()!=expectedProblems.length) {
			bad = "Wrong number of problems (expecting "+expectedProblems.length+" but found "+actualProblems.size()+")";
		} else {
			for (int i = 0; i < expectedProblems.length; i++) {
				if (!matchProblem(editor, actualProblems.get(i), expectedProblems[i])) {
					bad = "First mismatch: "+expectedProblems[i]+"\n";
					break;
				}
			}
		}
		if (bad!=null) {
			fail(bad+problemSumary(editor, actualProblems));
		}
	}

	private String problemSumary(MockEditor editor, List<SpringPropertyProblem> actualProblems) throws BadLocationException {
		StringBuilder buf = new StringBuilder();
		for (SpringPropertyProblem p : actualProblems) {
			buf.append("----------------------\n");
			String snippet = editor.getText(p.getOffset(), p.getLength());
			buf.append("("+p.getOffset()+", "+p.getLength()+")["+snippet+"]:\n");
			buf.append("   "+p.getMessage());
		}
		return buf.toString();
	}

	private boolean matchProblem(MockEditor editor, SpringPropertyProblem actual,
			String expect) {
		String[] parts = expect.split("\\|");
		assertEquals(2, parts.length);
		String badSnippet = parts[0];
		String messageSnippet = parts[1];
		int offset = editor.getText().indexOf(badSnippet);
		assertTrue(offset>=0);
		return actual.getOffset()==offset 
				&& actual.getLength()==badSnippet.length()
				&& actual.getMessage().contains(messageSnippet);
	}

}
