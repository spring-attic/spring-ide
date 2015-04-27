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

import java.util.ArrayList;

import org.springframework.ide.eclipse.yaml.editor.path.YamlPath;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPathSegment.AtKey;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.structure.YamlStructureParser.SRootNode;


public class YamlStructureParserTest extends YamlEditorTestHarness {

	public void testSimple() throws Exception {
		YamlEditor editor = new YamlEditor(
				"hello:\n"+
				"  world:\n" +
				"    message\n"
		);

		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): hello:",
				"    KEY(2): world:",
				"      RAW(4): message",
				"      RAW(-1): "
		);
	}

	public void assertParse(YamlEditor editor, String... expectDumpLines) throws Exception {
		StringBuilder expected = new StringBuilder();
		for (String line : expectDumpLines) {
			expected.append(line);
			expected.append("\n");
		}
		assertEquals(expected.toString().trim(),  editor.parseStructure().toString().trim());
	}

	public void testComments() throws Exception {
		YamlEditor editor = new YamlEditor(
				"#A comment\n" +
				"hello:\n"+
				"  #Another comment\n" +
				"  world:\n" +
				"    message\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  RAW(-1): #A comment",
				"  KEY(0): hello:",
				"    RAW(-1):   #Another comment",
				"    KEY(2): world:",
				"      RAW(4): message",
				"      RAW(-1): "
		);
	}

	public void testSiblings() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): world:",
				"    KEY(2): europe:",
				"      KEY(4): france:",
				"        RAW(6): cheese",
				"      KEY(4): belgium:",
				"        RAW(4): beer",
				"    KEY(2): canada:",
				"      KEY(4): montreal: poutine",
				"      KEY(4): vancouver:",
				"        RAW(6): salmon",
				"  KEY(0): moon:",
				"    KEY(2): moonbase-alfa:",
				"      RAW(4): moonstone",
				"      RAW(-1): "
		);
	}

	public void testSequenceBasic() throws Exception {
		YamlEditor editor;

		//Sequence at root level
		editor = new YamlEditor(
				"- foo\n" +
				"- bar\n" +
				"- zor"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  SEQ(0): - foo",
				"  SEQ(0): - bar",
				"  SEQ(0): - zor"
		);

		//Sequences nested in map without indent
		editor = new YamlEditor(
				"something:\n" +
				"- foo\n" +
				"- bar\n" +
				"- zor\n" +
				"else:\n" +
				"- a\n" +
				"- def"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): something:" ,
				"    SEQ(0): - foo",
				"    SEQ(0): - bar",
				"    SEQ(0): - zor",
				"  KEY(0): else:",
				"    SEQ(0): - a",
				"    SEQ(0): - def"
		);

		//Sequences nested in map without indent
		editor = new YamlEditor(
				"higher:\n" +
				"  something:\n" +
				"  - foo\n" +
				"  - bar\n" +
				"  - zor\n" +
				"  else:\n" +
				"  - a\n" +
				"  - def"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): higher:",
				"    KEY(2): something:" ,
				"      SEQ(2): - foo",
				"      SEQ(2): - bar",
				"      SEQ(2): - zor",
				"    KEY(2): else:",
				"      SEQ(2): - a",
				"      SEQ(2): - def"
		);

		//Sequences nested in map with indent
		editor = new YamlEditor(
				"something:\n" +
				"  - foo\n" +
				"  - bar\n" +
				"  - zor\n" +
				"else:\n" +
				"  - a\n" +
				"  - def"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): something:" ,
				"    SEQ(2): - foo",
				"    SEQ(2): - bar",
				"    SEQ(2): - zor",
				"  KEY(0): else:",
				"    SEQ(2): - a",
				"    SEQ(2): - def"
		);

	}

	public void testSequenceWithNestedSequence() throws Exception {
		YamlEditor editor;

		editor = new YamlEditor(
				"- - a\n" +
				"  - b\n" +
				"- - c\n" +
				"  - d\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  SEQ(0): - - a",
				"    SEQ(2): - a",
				"    SEQ(2): - b",
				"  SEQ(0): - - c",
				"    SEQ(2): - c",
				"    SEQ(2): - d",
				"      RAW(-1): "
		);

		editor = new YamlEditor(
				"foo:\n" +
				"- - a\n" +
				"  - b\n" +
				"- - c\n" +
				"  - d"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): - - a",
				"      SEQ(2): - a",
				"      SEQ(2): - b",
				"    SEQ(0): - - c",
				"      SEQ(2): - c",
				"      SEQ(2): - d"
		);

		editor = new YamlEditor(
				"foo:\n" +
				"- - a\n" +
				"  - b\n" +
				"bar:\n" +
				"- - c\n" +
				"  - d\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): - - a",
				"      SEQ(2): - a",
				"      SEQ(2): - b",
				"  KEY(0): bar:",
				"    SEQ(0): - - c",
				"      SEQ(2): - c",
				"      SEQ(2): - d",
				"        RAW(-1): "
		);

		editor = new YamlEditor(
				"foo:\n" +
				"- \n" +
				"  - a\n" +
				"  - b\n" +
				"-\n" +
				"  - c\n" +
				"  - d"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): - ",
				"      SEQ(2): - a",
				"      SEQ(2): - b",
				"    SEQ(0): -",
				"      SEQ(2): - c",
				"      SEQ(2): - d"
		);

		editor = new YamlEditor(
				"foo:\n" +
				"- - - - a\n" +
				"      - b\n" +
				"    - c\n" +
				"  - d\n" +
				"- e\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): - - - - a",
				"      SEQ(2): - - - a",
				"        SEQ(4): - - a",
				"          SEQ(6): - a",
				"          SEQ(6): - b",
				"        SEQ(4): - c",
				"      SEQ(2): - d",
				"    SEQ(0): - e",
				"      RAW(-1): "
		);

		editor = new YamlEditor(
				"foo:\n" +
				"- - - - a\n" +
				"    - c\n" +
				"- e\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): - - - - a",
				"      SEQ(2): - - - a",
				"        SEQ(4): - - a",
				"          SEQ(6): - a",
				"        SEQ(4): - c",
				"    SEQ(0): - e",
				"      RAW(-1): "
		);

	}

	public void testSequenceWithNestedMap() throws Exception {
		YamlEditor editor;

		// A map nested in a sequence may start on the same line
		editor = new YamlEditor(
				"- foo: is foo\n" +
				"  bar: is bar\n" +
				"    junk\n" +
				"- a: aaa\n" +
				"  b: bbb\n"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  SEQ(0): - foo: is foo",
				"    KEY(2): foo: is foo",
				"    KEY(2): bar: is bar",
				"      RAW(4): junk",
				"  SEQ(0): - a: aaa",
				"    KEY(2): a: aaa",
				"    KEY(2): b: bbb",
				"      RAW(-1): "
		);

		//A map nested in a sequence may start on a new line
		editor = new YamlEditor(
				"-\n"+  //without space
				"  foo: is foo\n" +
				"  bar: is bar\n" +
				"    junk\n" +
				"- \n" +  //with space
				"  a: aaa\n" +
				"  b: bbb"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  SEQ(0): -",
				"    KEY(2): foo: is foo",
				"    KEY(2): bar: is bar",
				"      RAW(4): junk",
				"  SEQ(0): - ", //with space
				"    KEY(2): a: aaa",
				"    KEY(2): b: bbb"
		);

		editor = new YamlEditor(
				"foo:\n" +
				"-\n"+  //without space
				"  foo: is foo\n" +
				"  bar: is bar\n" +
				"    junk\n" +
				"- \n" +  //with space
				"  a: aaa\n" +
				"  b: bbb"
		);
		assertParse(editor,
				"ROOT(0): ",
				"  KEY(0): foo:",
				"    SEQ(0): -",
				"      KEY(2): foo: is foo",
				"      KEY(2): bar: is bar",
				"        RAW(4): junk",
				"    SEQ(0): - ", //with space
				"      KEY(2): a: aaa",
				"      KEY(2): b: bbb"
		);
	}

	public void testTraverseSeq() throws Exception {
		YamlEditor editor = new YamlEditor(
				"foo:\n" +
				"- - - - a\n" +
				"    - c\n" +
				"- e"
		);
		SRootNode root = editor.parseStructure();
		YamlPath path;

		path = pathWith("foo", 0, 0, 0, 0);
		assertEquals(
				"SEQ(6): - a\n",
				path.traverse((SNode)root).toString());

		path = pathWith("foo", -1);
		assertNull(path.traverse((SNode)root));

		path = pathWith("foo", 1);
		assertEquals(
				"SEQ(0): - e\n",
				path.traverse((SNode)root).toString());

		path = pathWith("foo", 2);
		assertNull(path.traverse((SNode)root));
	}

	public void testTraverseSeqKey() throws Exception {
		YamlEditor editor = new YamlEditor(
				"foo:\n" +
				"- bar:\n" +
				"  - a\n" +
				"  - key: lol\n" +
				"- e\n"
		);
		SRootNode root = editor.parseStructure();
		YamlPath path;

		path = pathWith("foo", 0, "bar", 1, "key");
		assertEquals(
				"KEY(4): key: lol\n",
				path.traverse((SNode)root).toString());
	}


	public void testTreeEnd() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		SRootNode root = editor.parseStructure();
		SNode node = getNodeAtPath(root, 0, 1);
		assertTreeText(editor, node,
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n"
		);

		node = getNodeAtPath(root, 0, 0, 1, 0);
		assertTreeText(editor, node,
				"beer"
		);
	}

	public void testTreeEndKeyNodeNoChildren() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		SRootNode root = editor.parseStructure();
		SNode node = getNodeAtPath(root, 0, 0);
		assertTreeText(editor, node,
				"  europe:"
		);
	}

	public void testFind() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		SRootNode root = editor.parseStructure();
		assertFind(editor, root, "world:", 					0);
		assertFind(editor, root,   "europe:", 				0, 0);
		assertFind(editor, root,     "france:",				0, 0, 0);
		assertFind(editor, root,       "cheese",			0, 0, 0, 0);
		assertFind(editor, root,     "belgium:",			0, 0, 1);
		assertFind(editor, root,     "beer",				0, 0, 1, 0);
		assertFind(editor, root,   "canada:",				0, 1);
		assertFind(editor, root,     "montreal: poutine",	0, 1, 0);
		assertFind(editor, root,     "vancouver:",			0, 1, 1);
		assertFind(editor, root,        "salmon",			0, 1, 1, 0);
		assertFind(editor, root, "moon:",					1);
		assertFind(editor, root,   "moonbase-alfa:",		1, 0);
		assertFind(editor, root,     "moonstone",			1, 0, 0);

		assertFindStart(editor, root, " europe:", 0);
	}

	public void testFindInSequence() throws Exception {
		YamlEditor editor = new YamlEditor(
				"foo:\n" +
				"- alchemy\n" +
				"- bistro\n" +
				"bar:\n" +
				"- - - nice: text\n"+
				"zor:\n" +
				"  - - - very: good\n" +
				"end: END"
		);
		SRootNode root = editor.parseStructure();

		assertFind     (editor, root, "foo:",				0);
		assertFind     (editor, root, "- alchemy",			0, 0);
		assertFind     (editor, root, "- bistro",			0, 1);
		assertFind     (editor, root, "bar:",				1);
		assertFindStart(editor, root, "- - - nice: text",	1, 0);
		assertFindStart(editor, root,  " - - nice: text",	1, 0);
		assertFindStart(editor, root,   "- - nice: text",	1, 0, 0);
		assertFindStart(editor, root,    " - nice: text",	1, 0, 0);
		assertFindStart(editor, root,     "- nice: text",	1, 0, 0, 0);
		assertFindStart(editor, root,      " nice: text",	1, 0, 0, 0);
		assertFind     (editor, root,       "nice: text",	1, 0, 0, 0, 0);
		assertFind     (editor, root, "zor:",				2);
		assertFindStart(editor, root, "  - - - very: good",	2);
		assertFindStart(editor, root,  " - - - very: good",	2);
		assertFindStart(editor, root,   "- - - very: good",	2, 0);
		assertFindStart(editor, root,    " - - very: good",	2, 0);
		assertFindStart(editor, root,     "- - very: good",	2, 0, 0);
		assertFindStart(editor, root,      " - very: good",	2, 0, 0);
		assertFindStart(editor, root,       "- very: good",	2, 0, 0, 0);
		assertFindStart(editor, root,        " very: good",	2, 0, 0, 0);
		assertFind     (editor, root,         "very: good",	2, 0, 0, 0, 0);
	}

	public void testGetKey() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		SRootNode root = editor.parseStructure();
		assertKey(editor, root, "world:", 				"world");
		assertKey(editor, root, "europe:", 				"europe");
		assertKey(editor, root, "montreal: poutine",	"montreal");
	}

	public void testIsInValue() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"foo:\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);
		SRootNode root = editor.parseStructure();
		assertValueRange(editor, root, "montreal: poutine",	" poutine");
		assertValueRange(editor, root, "europe:", "\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer");
		assertValueRange(editor, root, "foo:", null);
	}

	private void assertValueRange(YamlEditor editor, SRootNode root, String nodeText, String expectedValue) throws Exception {
		int start = editor.getText().indexOf(nodeText);
		SKeyNode node = (SKeyNode) root.find(start);
		int valueRangeStart;
		int valueRangeEnd;
		if (expectedValue==null) {
			valueRangeStart = valueRangeEnd = start+nodeText.length();
		} else {
			valueRangeStart = editor.getRawText().lastIndexOf(expectedValue);
			valueRangeEnd = valueRangeStart+expectedValue.length();
			assertEquals(expectedValue, editor.textBetween(valueRangeStart, valueRangeEnd));
		}

		assertTrue(node.isInValue(valueRangeStart));
		assertFalse(node.isInValue(valueRangeStart-1));
		assertTrue(node.isInValue(valueRangeEnd));
		assertFalse(node.isInValue(valueRangeEnd+1));
	}

	public void testTraverse() throws Exception {
		YamlEditor editor = new YamlEditor(
				"world:\n" +
				"  europe:\n" +
				"    france:\n" +
				"      cheese\n" +
				"    belgium:\n" +
				"    beer\n" + //At same level as key, technically this is a syntax error but we tolerate it
				"  canada:\n" +
				"    montreal: poutine\n" +
				"    vancouver:\n" +
				"      salmon\n" +
				"moon:\n" +
				"  moonbase-alfa:\n" +
				"    moonstone\n"
		);


		SRootNode root = editor.parseStructure();
		YamlPath pathToFrance = pathWith(
				"world", "europe", "france"
		);
		assertEquals(
				"KEY(4): france:\n"+
				"  RAW(6): cheese\n",
				pathToFrance.traverse((SNode)root).toString());

		assertNull(pathWith("world", "europe", "bogus").traverse((SNode)root));
	}

	public void testGetFirstRealChild() throws Exception {
		YamlEditor editor = new YamlEditor(
				"no-children:\n" +
				"unreal-children:\n" +
				"  #Unreal\n" +
				"\n" +
				"  #comment only\n" +
				"real-child:\n" +
				"  abc\n" +
				"mixed-children:\n" +
				"\n" +
				"#comment\n" +
				"  def"
		);

		assertFirstRealChild(editor, "no-children", null);
		assertFirstRealChild(editor, "unreal-children", null);
		assertFirstRealChild(editor, "real-child", "abc");
		assertFirstRealChild(editor, "mixed-children", "def");

	}

	private void assertFirstRealChild(YamlEditor editor, String testNodeName, String expectedNodeSnippet) throws Exception {
		SRootNode root = editor.parseStructure();
		SKeyNode testNode = root.getChildWithKey(testNodeName);
		assertNotNull(testNode);
		SNode expected = null;
		if (expectedNodeSnippet!=null) {
			int offset = editor.getRawText().indexOf(expectedNodeSnippet);
			expected = root.find(offset);
			assertTrue(editor.textUnder(expected).contains(expectedNodeSnippet));
		}

		assertEquals(expected, testNode.getFirstRealChild());
	}

	private YamlPath pathWith(Object... keysOrIndexes) {
		ArrayList<YamlPathSegment> segments = new ArrayList<YamlPathSegment>();
		for (Object keyOrIndex : keysOrIndexes) {
			if (keyOrIndex instanceof String) {
				segments.add(YamlPathSegment.valueAt((String)keyOrIndex));
			} else if (keyOrIndex instanceof Integer) {
				segments.add(YamlPathSegment.valueAt((Integer)keyOrIndex));
			} else {
				fail("Unknown type of path element: "+keyOrIndex);
			}
		}
		return new YamlPath(segments);
	}

	private void assertKey(YamlEditor editor, SRootNode root, String nodeText, String expectedKey) throws Exception {
		int start = editor.getText().indexOf(nodeText);
		SKeyNode node = (SKeyNode) root.find(start);
		String key = node.getKey();
		assertEquals(expectedKey, key);

		//test the key range as well
		int startOfKeyRange = node.getStart();
		int keyRangeLen = key.length();
		int endOfKeyRange = startOfKeyRange + keyRangeLen;
		assertTrue(node.isInKey(startOfKeyRange));
		assertFalse(node.isInKey(startOfKeyRange-1));
		assertTrue(node.isInKey(endOfKeyRange));
		assertFalse(node.isInKey(endOfKeyRange+1));
	}

	private void assertFind(YamlEditor editor, SRootNode root, String snippet, int... expectPath) {
		int start = editor.getRawText().indexOf(snippet);
		int end = start+snippet.length();
		int middle = (start+end) / 2;

		SNode expectNode = getNodeAtPath(root, expectPath);

		assertEquals(expectNode, root.find(start));
		assertEquals(expectNode, root.find(middle));
		assertEquals(expectNode, root.find(end));
	}

	private void assertFindStart(YamlEditor editor, SRootNode root, String snippet, int... expectPath) {
		int start = editor.getRawText().indexOf(snippet);
		SNode expectNode = getNodeAtPath(root, expectPath);
		assertEquals(expectNode, root.find(start));
	}


	private void assertTreeText(YamlEditor editor, SNode node, String expected) throws Exception {
		String actual = editor.textBetween(node.getStart(), node.getTreeEnd());
		assertEquals(expected.trim(), actual.trim());
	}

	private SNode getNodeAtPath(SNode node, int... childindices) {
		int i = 0;
		while (i<childindices.length) {
			int child = childindices[i];
			node = ((SChildBearingNode)node).getChildren().get(child);
			i++;
		}
		return node;
	}

}
