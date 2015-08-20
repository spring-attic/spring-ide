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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.Listener;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertiesIndexManager;

@SuppressWarnings("restriction")
public class SpringPropertiesFileEditor extends PropertiesFileEditor implements Listener<SpringPropertiesIndexManager>, IPropertyChangeListener {

	/**
	 * Content Type ID this editor is registered to open for.
	 */
	public static final IContentType CONTENT_TYPE = Platform.getContentTypeManager().getContentType("org.springframework.ide.eclipse.applicationProperties");
	private SpringPropertiesFileSourceViewerConfiguration fSourceViewerConf;

	public SpringPropertiesFileEditor() {
		super();
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 * @since 3.4
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		//Override SourceViewerConfiguration with our own
		IPreferenceStore store= JavaPlugin.getDefault().getCombinedPreferenceStore();
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		setSourceViewerConfiguration(fSourceViewerConf = new SpringPropertiesFileSourceViewerConfiguration(textTools.getColorManager(), store, this, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING));
		SpringPropertiesEditorPlugin.getIndexManager().addListener(this);
		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}


	@Override
	public void dispose() {
		super.dispose();
		SpringPropertiesEditorPlugin.getIndexManager().removeListener(this);
		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	/**
	 * Called when property index manager was changed.
	 * TODO: could we make this more precise? We only care about a particular index (for same project this editor is on).
	 */
	@Override
	public void changed(SpringPropertiesIndexManager index) {
		fSourceViewerConf.forceReconcile();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith(ProblemSeverityPreferencesUtil.PREFERENCE_PREFIX)) {
			if (fSourceViewerConf!=null) {
				fSourceViewerConf.forceReconcile();
			}
		}
	}

}
