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

import static org.springframework.ide.eclipse.boot.util.StringUtil.trimEnd;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.RelaxedNameConfig;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyIndex;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ApplicationYamlStructureProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.completions.ApplicationYamlAssistContextProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Kris De Volder
 */
public class ApplicationYamlEditorTestHarness extends YamlOrPropertyEditorTestHarness {

	private static final RelaxedNameConfig relaxedNameConfig = RelaxedNameConfig.COMPLETION_DEFAULTS;
	protected YamlStructureProvider structureProvider = ApplicationYamlStructureProvider.INSTANCE;
	protected Yaml yaml = new Yaml();
	protected YamlASTProvider parser = new YamlASTProvider(yaml);

	private TypeUtilProvider typeUtilProvider = new TypeUtilProvider() {
		public TypeUtil getTypeUtil(IDocument doc) {
			return new TypeUtil(javaProject);
		}
	};

	private YamlAssistContextProvider assistContextProvider = new ApplicationYamlAssistContextProvider(
			indexProvider, typeUtilProvider, relaxedNameConfig, documentContextFinder
	);
	HoverInfoProvider hoverProvider = new YamlHoverInfoProvider(parser, structureProvider, assistContextProvider);
	private ICompletionEngine completionEngine = new YamlCompletionEngine(structureProvider, assistContextProvider);

	protected ApplicationYamlReconcileEngine createReconcileEngine() {
		return new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
	}

	//TODO: the link targets bits are almost dupiclates from the SpringProperties editor test harness.
	//  should be able to pull up with some reworking of the SpringProperties harness (i.e. add required
	//  abstract methods to MockPropertiesEditor and make a subclass for SpringProperties harness.
	protected List<IJavaElement> getLinkTargets(MockYamlEditor editor, int pos) {
		HoverInfo info = editor.getHoverInfo(pos);
		if (info!=null) {
			return info.getJavaElements();
		}
		return Collections.emptyList();
	}

	public void assertLinkTargets(MockYamlEditor editor, String hoverOver, String... expecteds) {
		int pos = editor.middleOf(hoverOver);
		assertTrue("Not found in editor: '"+hoverOver+"'", pos>=0);

//		List<PropertySource> rawTargets = getRawLinkTargets(editor, pos);
//		assertEquals(expecteds.length, rawTargets.size());

		List<IJavaElement> targets = getLinkTargets(editor, pos);
		assertEquals(expecteds.length, targets.size());
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals(expecteds[i], JavaElementLabels.getElementLabel(targets.get(i), JavaElementLabels.DEFAULT_QUALIFIED | JavaElementLabels.M_PARAMETER_TYPES));
		}
	}

//	private List<PropertySource> getRawLinkTargets(YamlEditor editor, int pos) {
//		HoverInfo hover = editor.getHoverInfo(pos);
//		if (hover!=null && hover instanceof SpringPropertyHoverInfo) {
//			return ((SpringPropertyHoverInfo)hover).getSources();
//		}
//		return Collections.emptyList();
//	}

	@Override
	public ICompletionProposal[] getCompletions(MockEditor editor) throws Exception {
		Collection<ICompletionProposal> _completions = completionEngine.getCompletions(editor.document, editor.selectionStart);
		ICompletionProposal[] completions = _completions.toArray(new ICompletionProposal[_completions.size()]);
		Arrays.sort(completions, COMPARATOR);
		return completions;
	}

	public void assertCompletionsDisplayString(String editorText, String... completionsLabels) throws Exception {
		MockPropertiesEditor editor = new MockPropertiesEditor(editorText);
		ICompletionProposal[] completions = getCompletions(editor);
		String[] actualLabels = new String[completions.length];
		for (int i = 0; i < actualLabels.length; i++) {
			actualLabels[i] = completions[i].getDisplayString();
		}
		assertElements(actualLabels, completionsLabels);
	}

	public void assertCompletionCount(int expected, String editorText)
			throws Exception {
				YamlEditor editor = new YamlEditor(editorText);
				assertEquals(expected, getCompletions(editor).length);
			}

	public void assertNoCompletions(String text) throws Exception {
		MockPropertiesEditor editor = new MockPropertiesEditor(text);
		assertEquals(0, getCompletions(editor).length);
	}

	public void assertCompletion(String before, String after) throws Exception {
		MockPropertiesEditor editor = new MockPropertiesEditor(before);
		ICompletionProposal completion = getFirstCompletion(editor);
		editor.apply(completion);
		String actual = editor.getText();
		assertEquals(trimEnd(after), trimEnd(actual));
	}

	public class YamlEditor extends MockYamlEditor {
		public YamlEditor(String string) {
			super(string, structureProvider, parser, hoverProvider);
		}
	}

	@Override
	protected HoverInfoProvider getHoverProvider() {
		fail("HoverInfoProvider missing");
		return null;
	}



}
