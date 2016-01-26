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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertContains;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.nodes.Node;

public class MockYamlEditor extends MockEditor {

	private YamlDocument ymlDoc;
	protected final YamlASTProvider astProvider;
	private HoverInfoProvider hoverProvider;

	public MockYamlEditor(String string, YamlStructureProvider structureProvider, YamlASTProvider astProvider, HoverInfoProvider hoverProvider) {
		super(string);
		ymlDoc = new YamlDocument(document, structureProvider);
		this.astProvider = astProvider;
		this.hoverProvider = hoverProvider;
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

	public int startOf(String nodeText) {
		return document.get().indexOf(nodeText);
	}

	public int endOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start+nodeText.length();
		}
		return -1;
	}

	public int middleOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start + nodeText.length()/2;
		}
		return -1;
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

	public String textUnder(IRegion r) throws BadLocationException {
		return document.get(r.getOffset(), r.getLength());
	}

	public IRegion getHoverRegion(int offset) {
		return hoverProvider.getHoverRegion(document, offset);
	}

	public HoverInfo getHoverInfo(int offset) {
		IRegion r = getHoverRegion(offset);
		if (r!=null) {
			return hoverProvider.getHoverInfo(document, r);
		}
		return null;
	}

	public String textBetween(int start, int end) throws Exception {
		return ymlDoc.textBetween(start, end);
	}

	public void assertNoHover(String hoverOver) {
		HoverInfo info = getHoverInfo(middleOf(hoverOver));
		assertNull(info);
	}

	public void assertIsHoverRegion(String string) throws BadLocationException {
		assertHoverRegionCovers(middleOf(string), string);
		assertHoverRegionCovers(startOf(string), string);
		assertHoverRegionCovers(endOf(string)-1, string);
	}

	public void assertHoverRegionCovers(int offset, String expect) throws BadLocationException {
		IRegion r = getHoverRegion(offset);
		String actual = textUnder(r);
		assertEquals(expect, actual);
	}

	public void assertHoverContains(String hoverOver, String expect) {
		HoverInfo info = getHoverInfo(middleOf(hoverOver));
		assertNotNull("No hover info for '"+ hoverOver +"'", info);
		assertContains(expect, info.getHtml());
	}

}