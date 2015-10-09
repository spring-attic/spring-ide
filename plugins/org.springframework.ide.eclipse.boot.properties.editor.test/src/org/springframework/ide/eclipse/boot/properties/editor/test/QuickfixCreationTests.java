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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem.problem;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.EditorType;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.QuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.ui.UserInteractions;
import org.springframework.ide.eclipse.boot.test.MockPrefsStore;

import junit.framework.TestCase;

/**
 * Tests that the logic for creating quickfixes from problems is sound.
 * <p>
 * These tests do not check anything about how/what the quickfixes actually do
 * when applied.
 *
 * @author Kris De Volder
 */
public class QuickfixCreationTests extends TestCase {

	private static final EnumSet<ProblemType> UNKNOWN_PROPERTY_PROBLEMS = EnumSet.of(ProblemType.YAML_UNKNOWN_PROPERTY, ProblemType.PROP_UNKNOWN_PROPERTY);

	/**
	 * Checks that an 'unknown property' problem returns the expected quickfixes.
	 */
	public void testUnknownPropertyQuickfixes() throws Exception {
		for (ProblemType problemType : UNKNOWN_PROPERTY_PROBLEMS) {
			doTestUnkownPropertyQuickfixes(problemType);
		}
	}

	public void testOtherProblemQuickfixes() throws Exception {
		for (ProblemType problemType : EnumSet.complementOf(UNKNOWN_PROPERTY_PROBLEMS)) {
			doTestOtherProblemQuickfixes(problemType);
		}
	}

	public void doTestOtherProblemQuickfixes(ProblemType problemType) {
		SpringPropertyProblem problem = problem(problemType, "Some kind of a message", 15, 9);
		IPreferenceStore workspacePrefs = new MockPrefsStore();
		IPreferenceStore projectPrefs = new MockPrefsStore();
		UserInteractions ui = mock(UserInteractions.class);
		QuickfixContext context = mockQuickFixContext("foo", workspacePrefs, projectPrefs, ui);
		List<ICompletionProposal> fixes = problem.getQuickfixes(context);

		String label = problemType.getLabel();
		assertLabels(fixes,
				"Ignore '"+label+"' in project.",
				"Ignore '"+label+"' in workspace."
		);

		verifyZeroInteractions(ui);
	}

	public void doTestUnkownPropertyQuickfixes(ProblemType problemType) {
		SpringPropertyProblem problem = problem(problemType, "The property 'yada.yada' is unknown", 15, 9);
		problem.setPropertyName("yada.yada");
		IPreferenceStore workspacePrefs = new MockPrefsStore();
		IPreferenceStore projectPrefs = new MockPrefsStore();
		UserInteractions ui = mock(UserInteractions.class);
		QuickfixContext context = mockQuickFixContext("foo", workspacePrefs, projectPrefs, ui);
		List<ICompletionProposal> fixes = problem.getQuickfixes(context);

		assertLabels(fixes,
				"Create metadata for 'yada.yada'.",
				"Ignore 'Unknown property' in project.",
				"Ignore 'Unknown property' in workspace."
		);

		verifyZeroInteractions(ui);
	}

	/**
	 * Test that workspace ignore quickfix is not suggested when project preferences have
	 * already been enabled.
	 */
	public void testWorkspaceIgnoreDisabledWhenProjectSettingsEnabled() throws Exception {
		for (ProblemType problemType : UNKNOWN_PROPERTY_PROBLEMS) {
			doTestWorkspaceIgnoreDisabledWhenProjectSettingsEnabled(problemType);
		}
	}

	public void doTestWorkspaceIgnoreDisabledWhenProjectSettingsEnabled(ProblemType problemType) throws Exception {
		EditorType editorType = problemType.getEditorType();
		SpringPropertyProblem problem = problem(problemType, "The property 'yada.yada' is unknown", 15, 9);
		problem.setPropertyName("yada.yada");
		IPreferenceStore workspacePrefs = new MockPrefsStore();
		IPreferenceStore projectPrefs = new MockPrefsStore();
		UserInteractions ui = mock(UserInteractions.class);
		QuickfixContext context = mockQuickFixContext("foo", workspacePrefs, projectPrefs, ui);

		ProblemSeverityPreferencesUtil.enableProjectPrefs(projectPrefs, editorType, true);

		List<ICompletionProposal> fixes = problem.getQuickfixes(context);

		assertLabels(fixes,
				"Create metadata for 'yada.yada'.",
				"Ignore 'Unknown property' in project."
				// SHOULD BE MISSING: "Ignore 'Unknown property' in workspace."
		);

		verifyZeroInteractions(ui);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Code below is 'harness' and 'helper' stuff.

	private void assertLabels(List<ICompletionProposal> fixes, String... expecteds) {
		assertEquals(expecteds.length, fixes.size());
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals(expecteds[i],fixes.get(i).getDisplayString());
		}
	}

	private QuickfixContext mockQuickFixContext(String projectName, IPreferenceStore workspacePrefs, IPreferenceStore projectPrefs, UserInteractions ui) {
		QuickfixContext context = mock(QuickfixContext.class);

		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(projectName);

		IJavaProject javaProject = mock(IJavaProject.class);
		when(javaProject.getProject()).thenReturn(project);

		when(context.getProject()).thenReturn(project);
		when(context.getJavaProject()).thenReturn(javaProject);
		when(context.getProjectPreferences()).thenReturn(projectPrefs);
		when(context.getWorkspacePreferences()).thenReturn(workspacePrefs);
		when(context.getUI()).thenReturn(ui);

		return context;
	}

}
