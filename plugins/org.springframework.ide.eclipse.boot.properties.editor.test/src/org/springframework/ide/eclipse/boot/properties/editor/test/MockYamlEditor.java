/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.nodes.Node;

public class MockYamlEditor extends MockEditor {

	YamlDocument ymlDoc;
	protected final YamlASTProvider astProvider;
	public MockYamlEditor(String string, YamlStructureProvider structureProvider, YamlASTProvider astProvider, HoverInfoProvider hoverProvider) {
		super(string, hoverProvider);
		ymlDoc = new YamlDocument(document, structureProvider);
		this.astProvider = astProvider;
	}

	public YamlFileAST parse() {
		return getParser().getAST(this.document);
	}

	public YamlASTProvider getParser() {
		return astProvider;
	}

	public SRootNode parseStructure() throws Exception {
		return ymlDoc.getStructure();
	}

	public String textUnder(Node node) throws Exception {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return document.get(start, end-start);
	}

	public String textUnder(SNode node) throws Exception {
		int start = node.getStart();
		int end = node.getTreeEnd();
		return document.get(start, end-start);
	}

}