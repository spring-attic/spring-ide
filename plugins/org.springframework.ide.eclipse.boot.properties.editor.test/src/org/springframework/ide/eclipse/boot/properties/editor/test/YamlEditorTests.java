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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

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
				"extracrap: 8080|Expecting Integer for 'server.port'",
				"snuggem|Unknown property",
				"bogus|Unknown property"
		);

	}

}
