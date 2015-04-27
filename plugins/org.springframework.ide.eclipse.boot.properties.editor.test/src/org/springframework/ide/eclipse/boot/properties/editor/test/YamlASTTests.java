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

import java.util.List;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.NodeRef;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.PathUtil;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.Node;

/**
 * @author Kris De Volder
 */
public class YamlASTTests extends YamlEditorTestHarness {

	/**
	 * Check that node at given offset exactly covers the expected text
	 * snippet in the document.
	 */
	public void assertNodeTextAt(YamlEditor input, int offset, String expectText) throws Exception {
		YamlFileAST ast = input.parse();
		Node node = ast.findNode(offset);
		String actualText = input.textUnder(node);
		assertEquals(expectText, actualText);
	}


	@Test
	public void testFindNode() throws Exception {
		YamlEditor input = new YamlEditor(
				"spring:\n" +
				"  application:\n" +
				"    name: foofoo\n" +
				"    \n" +
				"server:\n" +
				"  port: 8888"
		);
		doFindNodeTest(input, "foofoo");
		doFindNodeTest(input, "application");
		doFindNodeTest(input, "spring");
		doFindNodeTest(input, "port");
		doFindNodeTest(input, "server");
	}

	private void doFindNodeTest(YamlEditor input, String nodeText) throws Exception {
		assertNodeTextAt(input, input.startOf(nodeText), nodeText);
		assertNodeTextAt(input, input.endOf(nodeText)-1, nodeText);
		assertNodeTextAt(input, input.middleOf(nodeText), nodeText);
	}

	public void testFindPath() throws Exception {
		YamlEditor input = new YamlEditor(
				"foo:\n" +
				"  bar:\n" +
				"    first: foofoo\n" +
				"    second: barbar\n" +
				"    third: lalala\n" +
				"server:\n" +
				"  port: 8888"
		);

		assertPath(input, "barbar",
				"ROOT[0]@val['foo']@val['bar']@val['second']");

		assertPath(input, "8888",
				"ROOT[0]@val['server']@val['port']");
	}

	public void testPathToPropertyString() {
		YamlEditor input = new YamlEditor(
				"spring:\n" +
				"  application:\n" +
				"    name: foofoo\n" +
				"    \n" +
				"server:\n" +
				"  port: 8888"
		);

		assertPropertyString(input, "8888", "server.port");
		assertPropertyString(input, "port", "server.port");
		assertPropertyString(input, "server", "server");

		assertPropertyString(input, "name", "spring.application.name");

		assertPropertyString(input, "plica", "spring.application");
	}

	public void testPathToPropertyWithSequence() {
		YamlEditor input = new YamlEditor(
				"services:\n" +
				"  - name: Foo Service\n"+
				"    type: Great\n"+
				"  - name: Bar Service\n" +
				"    type: Best\n"
		);

		assertPropertyString(input, "Foo",  "services[0].name");
		assertPropertyString(input, "Great", "services[0].type");
		assertPropertyString(input, "type", "services[0].type");

		assertPropertyString(input, "Bar", "services[1].name");
	}


	protected void assertPropertyString(YamlEditor input, String nodeText,
			String expected) {
		YamlFileAST ast = input.parse();
		List<NodeRef<?>> path = ast.findPath(input.middleOf(nodeText));
		assertEquals(expected,
				PathUtil.toPropertyPrefixString(path));
	}

	@Test
	public void testMultiDocsPath() {
		YamlEditor input = new YamlEditor(
				"theFirst\n" +
				"---\n" +
				"second\n" +
				"...\n"
		);
		assertPath(input, "First",
				"ROOT[0]"
		);
		assertPath(input, "second",
				"ROOT[1]"
		);
	}

	public void testSequencePath() {
		YamlEditor input = new YamlEditor(
				"fooList:\n"+
				"  - a\n" +
				"  - b\n" +
				"  - c\n"
		);

		assertPath(input, "a",
				"ROOT[0]@val['fooList'][0]"
		);
		assertPath(input, "b",
				"ROOT[0]@val['fooList'][1]"
		);
		assertPath(input, "c",
				"ROOT[0]@val['fooList'][2]"
		);
	}

	protected void assertPath(YamlEditor input, String nodeText, String expected) {
		YamlFileAST ast = input.parse();
		String path = pathString(ast.findPath(input.middleOf(nodeText)));
		assertEquals(expected,  path);
	}


	private String pathString(List<NodeRef<?>> findPath) {
		StringBuilder buf = new StringBuilder();
		for (NodeRef<?> n : findPath) {
			buf.append(n);
		}
		return buf.toString();
	}

}
