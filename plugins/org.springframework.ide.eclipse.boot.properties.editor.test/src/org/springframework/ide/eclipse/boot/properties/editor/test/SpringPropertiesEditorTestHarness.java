/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo.PropertySource;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public abstract class SpringPropertiesEditorTestHarness extends YamlOrPropertyEditorTestHarness {

	public static final String INTEGER = Integer.class.getName();
	public static final String BOOLEAN = Boolean.class.getName();
	public static final String STRING = String.class.getName();
	protected SpringPropertiesCompletionEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		engine = new SpringPropertiesCompletionEngine();
		engine.setDocumentContextFinder(documentContextFinder);
		engine.setIndexProvider(new Provider<FuzzyMap<PropertyInfo>>() {
			public FuzzyMap<PropertyInfo> get() {
				return index;
			}
		});
		engine.setTypeUtil(new TypeUtil(javaProject));
	}

	protected SpringPropertiesReconcileEngine createReconcileEngine() {
		return new SpringPropertiesReconcileEngine(engine.getIndexProvider(), engine.getTypeUtil());
	}


	/**
	 * Compute hover text when mouse hovers at the end of the first occurence of
	 * a given String in the editor contents.
	 */
	public String getHoverText(MockPropertiesEditor editor, String atString) {
		int pos = editor.getText().indexOf(atString);
		if (pos>=0) {
			pos += atString.length();
		}
		IRegion region = engine.getHoverRegion(editor.document, pos);
		if (region!=null) {
			return engine.getHoverInfo(editor.document, region).getHtml();
		}
		return null;
	}

	@Override
	public ICompletionProposal[] getCompletions(MockEditor editor)
			throws BadLocationException {
		Collection<ICompletionProposal> _completions = engine.getCompletions(editor.document, editor.selectionStart);
		ICompletionProposal[] completions = _completions.toArray(new ICompletionProposal[_completions.size()]);
		Arrays.sort(completions, COMPARATOR);
		return completions;
	}

	/**
	 * Verifies an expected textSnippet is contained in the hovertext that is
	 * computed when hovering mouse at position at the end of first occurence of
	 * a given string in the editor.
	 */
	public void assertHoverText(MockPropertiesEditor editor, String afterString, String expectSnippet) {
		String hoverText = getHoverText(editor, afterString);
		assertContains(expectSnippet, hoverText);
	}

	public void assertCompletionDisplayString(String editorContents, String expected) throws Exception {
		MockPropertiesEditor editor = new MockPropertiesEditor(editorContents);
		ICompletionProposal completion = getFirstCompletion(editor);
		assertEquals(expected, completion.getDisplayString());
	}

	/**
	 * Like 'assertCompletionsBasic' but places the 'textBefore' in a context
	 * with other text around it... trying several different variations of
	 * text before and after the 'interesting' line.
	 */
	public void assertCompletionsVariations(String textBefore, String... expectTextAfter) throws Exception {
		//Variation 1: by itself
		assertCompletions(textBefore, expectTextAfter);
		//Variation 2: comment text before and after
		assertCompletions("#comment\n"+textBefore+"\n#comment", wrap("#comment\n", expectTextAfter, "\n#comment"));
		//Variation 3: empty lines of text before and after
		assertCompletions("\n"+textBefore+"\n\n", wrap("\n", expectTextAfter, "\n\n"));
		//Variation 3.b: empty lines of text before and single newline after
		assertCompletions("\n"+textBefore+"\n", wrap("\n", expectTextAfter, "\n"));
		//Variation 4: property assignment before and after
		assertCompletions("foo=bar\n"+textBefore+"\nnol=brol", wrap("foo=bar\n", expectTextAfter, "\nnol=brol"));
	}

	private String[] wrap(String before, String[] middle, String after) {
		//"\n"+expectTextAfter+"\n\n"
		String[] result = new String[middle.length];
		for (int i = 0; i < result.length; i++) {
			result[i] =  before+middle[i]+after;
		}
		return result;
	}

	/**
	 * Uses the given IJavaProject as the 'context' for the editor and populates engine test-data
	 * from this project's classpath as well.
	 */
	public void useProject(final IJavaProject jp) throws Exception {
		super.useProject(jp);
		this.engine.setTypeUtil(new TypeUtil(jp));
	}


	public void assertLinkTargets(MockPropertiesEditor editor, String hoverAtEndOf, String... expecteds) {
		int pos = editor.getText().indexOf(hoverAtEndOf);
		assertTrue("Not found in editor: '"+hoverAtEndOf+"'", pos>=0);
		pos += hoverAtEndOf.length();

		List<PropertySource> rawTargets = getRawLinkTargets(editor, pos);
		assertEquals(expecteds.length, rawTargets.size());

		List<IJavaElement> targets = getLinkTargets(editor, pos);
		assertEquals(expecteds.length, targets.size());
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals(expecteds[i], JavaElementLabels.getElementLabel(targets.get(i), JavaElementLabels.DEFAULT_QUALIFIED | JavaElementLabels.M_PARAMETER_TYPES));
		}
	}

	private List<PropertySource> getRawLinkTargets(MockPropertiesEditor editor, int pos) {
		IRegion region = engine.getHoverRegion(editor.document, pos);
		if (region!=null) {
			SpringPropertyHoverInfo hover = engine.getHoverInfo(editor.document, region);
			if (hover!=null) {
				return hover.getSources();
			}
		}
		return Collections.emptyList();
	}

	protected List<IJavaElement> getLinkTargets(MockPropertiesEditor editor, int pos) {
		IRegion region = engine.getHoverRegion(editor.document, pos);
		if (region!=null) {
			HoverInfo info = engine.getHoverInfo(editor.document, region);
			if (info!=null) {
				return info.getJavaElements();
			}
		}
		return Collections.emptyList();
	}

	public static String getContents(IFile file) throws Exception {
		InputStream in = file.getContents();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtil.pipe(in, out);
			String encoding = file.getCharset();
			try {
				return out.toString(encoding);
			} catch (UnsupportedEncodingException e) {
				return out.toString("utf8");
			}
		} finally {
			in.close();
		}
	}

	public static void buildProject(final IJavaProject jp) throws Exception {
		StsTestUtil.buildProject(jp);
// This doesn't seem needed anymore
//		new ACondition("Project build without errors: "+jp.getElementName()) {
//			@Override
//			public boolean test() throws Exception {
//				StsTestUtil.buildProject(jp);
//				return true;
//			}
//		}.waitFor(PROJECT_BUILD_TIMEOUT);
	}

}
