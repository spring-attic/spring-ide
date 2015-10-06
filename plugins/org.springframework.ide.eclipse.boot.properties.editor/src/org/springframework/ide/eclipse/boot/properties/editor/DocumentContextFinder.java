/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferencesBasedSeverityProvider;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SeverityProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;

/**
 * Provides a method to find context information for IDocument instances.
 * <p>
 * In production there's only one instance, but unit testing it is
 * convenient to be able to mock it up rather than have to
 * instantiate a lot of eclipse editor UI machinery.
 *
 * @author Kris De Volder
 */
public interface DocumentContextFinder {

	IJavaProject getJavaProject(IDocument doc);
	SeverityProvider getSeverityProvider(IDocument doc);

	DocumentContextFinder DEFAULT = new DocumentContextFinder() {
		@Override
		public IJavaProject getJavaProject(IDocument doc) {
			return DocumentUtil.getJavaProject(doc);
		}

		@Override
		public SeverityProvider getSeverityProvider(IDocument doc) {
			IProject p = DocumentUtil.getProject(doc);
			ScopedPreferenceStore projectPrefs = null;
			if (p!=null) {
			}
			return new PreferencesBasedSeverityProvider(
					projectPrefs,
					new ScopedPreferenceStore(InstanceScope.INSTANCE, SpringPropertiesEditorPlugin.PLUGIN_ID)
			);
		}
	};

}
