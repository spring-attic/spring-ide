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
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.EditorType;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.PreferencesBasedSeverityProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.SeverityProvider;

/**
 * @author Kris De Volder
 */
public class DocumentContextFinders {

	public static final DocumentContextFinder YAML_DEFAULT = defaultFor(EditorType.YAML);
	public static final  DocumentContextFinder PROPS_DEFAULT = defaultFor(EditorType.PROP);

	private static final DocumentContextFinder defaultFor(final EditorType editorType) {
		return new DocumentContextFinder() {
			@Override
			public IJavaProject getJavaProject(IDocument doc) {
				return DocumentUtil.getJavaProject(doc);
			}

			@Override
			public SeverityProvider getSeverityProvider(IDocument doc) {
				IProject p = DocumentUtil.getProject(doc);
				ScopedPreferenceStore projectPrefs = null;
				if (p!=null) {
					projectPrefs = new ScopedPreferenceStore(new ProjectScope(p), SpringPropertiesEditorPlugin.PLUGIN_ID);
				}
				return new PreferencesBasedSeverityProvider(
						projectPrefs,
						new ScopedPreferenceStore(InstanceScope.INSTANCE, SpringPropertiesEditorPlugin.PLUGIN_ID),
						editorType
				);
			}
		};
	}

}
