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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil.*;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil.getPreferenceName;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil.getSeverity;
import static org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity.*;

import java.util.EnumSet;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.EditorType;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.IgnoreProblemTypeInProjectQuickfix;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.QuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.boot.test.MockPrefsStore;

import junit.framework.TestCase;

public class IgnoreProblemTypeInWorkspaceQuickfixTests extends TestCase {

	private static final EnumSet<SpringPropertiesProblemType> UNKNOWN_PROPERTY_PROBLEMS = EnumSet.of(SpringPropertiesProblemType.YAML_UNKNOWN_PROPERTY, SpringPropertiesProblemType.PROP_UNKNOWN_PROPERTY);

	public void testApplyWhenProjectPrefsEnabled() throws Exception {
		for (SpringPropertiesProblemType problemType : UNKNOWN_PROPERTY_PROBLEMS) {
			EditorType editorType = problemType.getEditorType();

			//stuff we need:
			MockPrefsStore projectPrefs = new MockPrefsStore();
			MockPrefsStore workspacePrefs = new MockPrefsStore();
			IDocument document = mock(IDocument.class);

			//Check situation before test (if these check fail, test may be vacuous)
			assertNotEquals(IGNORE, getSeverity(projectPrefs, problemType));
			enableProjectPrefs(projectPrefs, editorType, true);

			// Enable spies (do this only now to not spy on test setup itself)
			projectPrefs = spy(projectPrefs);
			workspacePrefs = spy(workspacePrefs);

			QuickfixContext context = mock(QuickfixContext.class);
			when(context.getProjectPreferences()).thenReturn(projectPrefs);
			when(context.getWorkspacePreferences()).thenReturn(workspacePrefs);

			//The thing under test
			IgnoreProblemTypeInProjectQuickfix quickfix = new IgnoreProblemTypeInProjectQuickfix(context, problemType);

			//The test
			quickfix.apply(document);

			//Verify expectations
			assertEquals(IGNORE, getSeverity(projectPrefs, problemType));
			for (SpringPropertiesProblemType pt : SpringPropertiesProblemType.FOR(editorType)) {
				if (pt==problemType) {
					assertEquals(IGNORE, getSeverity(projectPrefs, pt));
				} else {
					assertEquals(pt.getDefaultSeverity(), getSeverity(projectPrefs, pt));
				}
			}

			verify(projectPrefs).save();
			verifyZeroInteractions(document, workspacePrefs);

		}
	}

	public void testApplyWhenProjectPrefsDisabled() throws Exception {
		for (SpringPropertiesProblemType problemType : UNKNOWN_PROPERTY_PROBLEMS) {
			EditorType editorType = problemType.getEditorType();

			//stuff we need:
			MockPrefsStore projectPrefs = new MockPrefsStore();
			MockPrefsStore workspacePrefs = new MockPrefsStore();

			// The thing that matters is... if workspacePrefs are no longer at default.
			for (SpringPropertiesProblemType pt : SpringPropertiesProblemType.FOR(editorType)) {
				setSeverity(workspacePrefs, pt, WARNING);
			}

			IDocument document = mock(IDocument.class);

			//Check situation before test (if these check fail, test may be vacuous)
			assertNotEquals(IGNORE, getSeverity(projectPrefs, problemType));
			assertFalse(projectPreferencesEnabled(projectPrefs, editorType));

			// Enable spies (do this only now to not spy on test setup itself)
			projectPrefs = spy(projectPrefs);
			workspacePrefs = spy(workspacePrefs);

			QuickfixContext context = mock(QuickfixContext.class);
			when(context.getProjectPreferences()).thenReturn(projectPrefs);
			when(context.getWorkspacePreferences()).thenReturn(workspacePrefs);

			//The thing under test
			IgnoreProblemTypeInProjectQuickfix quickfix = new IgnoreProblemTypeInProjectQuickfix(context, problemType);

			//The test
			quickfix.apply(document);

			//Verify expectations
			assertTrue(projectPreferencesEnabled(projectPrefs, editorType));
			assertEquals(IGNORE, getSeverity(projectPrefs, problemType));
			for (SpringPropertiesProblemType pt : SpringPropertiesProblemType.FOR(editorType)) {
				if (pt==problemType) {
					assertEquals(IGNORE, getSeverity(projectPrefs, pt));
				} else {
					assertEquals(getSeverity(workspacePrefs, pt), getSeverity(projectPrefs, pt));
				}
			}

			verify(projectPrefs).save();
			verifyZeroInteractions(document);

		}
	}

}
