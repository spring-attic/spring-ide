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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil.getSeverity;
import static org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity.IGNORE;

import java.util.EnumSet;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.IgnoreProblemTypeInWorkspaceQuickfix;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType;
import org.springframework.ide.eclipse.boot.test.MockPrefsStore;

import junit.framework.TestCase;

public class IgnoreProblemTypeInProjectQuickfixTests extends TestCase {

	private static final EnumSet<SpringPropertiesProblemType> UNKNOWN_PROPERTY_PROBLEMS = EnumSet.of(SpringPropertiesProblemType.YAML_UNKNOWN_PROPERTY, SpringPropertiesProblemType.PROP_UNKNOWN_PROPERTY);

	public void testApply() throws Exception {
		for (SpringPropertiesProblemType problemType : UNKNOWN_PROPERTY_PROBLEMS) {
			//stuff we need:
			MockPrefsStore prefs = spy(new MockPrefsStore());
			IDocument document = mock(IDocument.class);

			//The thing under test
			IgnoreProblemTypeInWorkspaceQuickfix quickfix = new IgnoreProblemTypeInWorkspaceQuickfix(prefs, problemType);

			//Check situation before test (if these check fail, test may be vacuous)
			assertNotEquals(IGNORE, getSeverity(prefs, problemType));

			//The test
			quickfix.apply(document);

			//Verify expectations
			assertEquals(IGNORE, getSeverity(prefs, problemType));
			verify(prefs).save(); // save was called ...
			assertFalse(prefs.needsSaving()); // ... after storing stuffs (not before :-)

			verifyZeroInteractions(document);
		}
	}

}
