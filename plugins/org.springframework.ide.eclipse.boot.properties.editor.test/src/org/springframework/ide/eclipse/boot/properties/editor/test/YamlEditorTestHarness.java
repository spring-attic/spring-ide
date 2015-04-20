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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.HoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.ICompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo.PropertySource;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.yaml.editor.YamlHoverInfoProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlASTProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlFileAST;
import org.springframework.ide.eclipse.yaml.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlCompletionEngine;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlDocument;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SRootNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureProvider;
import org.springframework.ide.eclipse.yaml.editor.reconcile.SpringYamlReconcileEngine;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

/**
 * @author Kris De Volder
 */
public class YamlEditorTestHarness extends YamlOrPropertyEditorTestHarness {

	protected YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
	protected Yaml yaml = new Yaml();
	protected YamlASTProvider parser = new YamlASTProvider(yaml);
	private SpringPropertyIndexProvider indexProvider = new SpringPropertyIndexProvider() {
		public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
			return index;
		}
	};

	private TypeUtilProvider typeUtilProvider = new TypeUtilProvider() {
		public TypeUtil getTypeUtil(IDocument doc) {
			return new TypeUtil(javaProject);
		}
	};

	private IPropertyHoverInfoProvider hoverProvider = new YamlHoverInfoProvider(parser, indexProvider, documentContextFinder);
	private ICompletionEngine completionEngine = new YamlCompletionEngine(yaml, indexProvider, documentContextFinder, structureProvider);

	protected SpringYamlReconcileEngine createReconcileEngine() {
		return new SpringYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
	}

	public class YamlEditor extends MockEditor {
		private YamlDocument ymlDoc;

		public YamlEditor(String string) {
			super(string);
			ymlDoc = new YamlDocument(document, structureProvider);
		}

		public YamlFileAST parse() {
			return parser.getAST(this.document);
		}

		public SRootNode parseStructure() throws Exception {
			return new YamlStructureParser(ymlDoc).parse();
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
	}


	public void assertNoHover(YamlEditor editor, String hoverOver) {
		HoverInfo info = editor.getHoverInfo(editor.middleOf(hoverOver));
		assertNull(info);
	}

	public void assertIsHoverRegion(YamlEditor editor, String string) throws BadLocationException {
		assertHoverRegionCovers(editor, editor.middleOf(string), string);
		assertHoverRegionCovers(editor, editor.startOf(string), string);
		assertHoverRegionCovers(editor, editor.endOf(string)-1, string);
	}

	public void assertHoverRegionCovers(YamlEditor editor, int offset, String expect) throws BadLocationException {
		IRegion r = editor.getHoverRegion(offset);
		String actual = editor.textUnder(r);
		assertEquals(expect, actual);
	}

	public void assertHoverContains(YamlEditor editor, String hoverOver, String expect) {
		HoverInfo info = editor.getHoverInfo(editor.middleOf(hoverOver));
		assertNotNull("No hover info for '"+ hoverOver +"'", info);
		assertContains(expect, info.getHtml());
	}

	//TODO: the link targets bits are almost dupiclates from the SpringProperties editor test harness.
	//  should be able to pull up with some reworking of the SpringProperties harness (i.e. add required
	//  abstract methods to MockEditor and make a subclass for SpringProperties harness.
	protected List<IJavaElement> getLinkTargets(YamlEditor editor, int pos) {
		HoverInfo info = editor.getHoverInfo(pos);
		if (info!=null && info instanceof SpringPropertyHoverInfo) {
			return info.getJavaElements();
		}
		return Collections.emptyList();
	}

	public void assertLinkTargets(YamlEditor editor, String hoverOver, String... expecteds) {
		int pos = editor.middleOf(hoverOver);
		assertTrue("Not found in editor: '"+hoverOver+"'", pos>=0);

		List<PropertySource> rawTargets = getRawLinkTargets(editor, pos);
		assertEquals(expecteds.length, rawTargets.size());

		List<IJavaElement> targets = getLinkTargets(editor, pos);
		assertEquals(expecteds.length, targets.size());
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals(expecteds[i], JavaElementLabels.getElementLabel(targets.get(i), JavaElementLabels.DEFAULT_QUALIFIED | JavaElementLabels.M_PARAMETER_TYPES));
		}
	}

	private List<PropertySource> getRawLinkTargets(YamlEditor editor, int pos) {
		HoverInfo hover = editor.getHoverInfo(pos);
		if (hover!=null && hover instanceof SpringPropertyHoverInfo) {
			return ((SpringPropertyHoverInfo)hover).getSources();
		}
		return Collections.emptyList();
	}

	public ICompletionProposal getFirstCompletion(MockEditor editor)
			throws Exception {
		return getCompletions(editor)[0];
	}

	public ICompletionProposal[] getCompletions(MockEditor editor)
			throws Exception {
		Collection<ICompletionProposal> _completions = completionEngine.getCompletions(editor.document, editor.selectionStart);
		ICompletionProposal[] completions = _completions.toArray(new ICompletionProposal[_completions.size()]);
		Arrays.sort(completions, COMPARATOR);
		return completions;
	}

	private static final Comparator<? super ICompletionProposal> COMPARATOR = new Comparator<ICompletionProposal>() {
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			return PropertyCompletionFactory.SORTER.compare(p1, p2);
		}
	};

	public void assertCompletionsDisplayString(String editorText, String... completionsLabels) throws Exception {
		MockEditor editor = new MockEditor(editorText);
		ICompletionProposal[] completions = getCompletions(editor);
		String[] actualLabels = new String[completions.length];
		for (int i = 0; i < actualLabels.length; i++) {
			actualLabels[i] = completions[i].getDisplayString();
		}
		assertElements(actualLabels, completionsLabels);
	}

}
