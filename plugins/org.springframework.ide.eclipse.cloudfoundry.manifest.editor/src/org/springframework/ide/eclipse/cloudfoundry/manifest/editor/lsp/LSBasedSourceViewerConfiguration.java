/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp;

import javax.inject.Provider;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestEditorActivator;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Martin Lippert
 */
public class LSBasedSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration {
	
	public LSBasedSourceViewerConfiguration(Provider<Shell> shellProvider) {
		super(shellProvider);
	}

	@Override
	protected HoverInfoProvider getHoverProvider(ISourceViewer viewer) {
		return new LSBasedHoverProvider(viewer);
	}
	
	@Override
	public ICompletionEngine getCompletionEngine(ISourceViewer viewer) {
		return new LSBasedCompletionEngine(viewer);
	}

	@Override
	protected YamlStructureProvider getStructureProvider() {
		return YamlStructureProvider.DEFAULT;
	}

	@Override
	protected IDialogSettings getPluginDialogSettings() {
		return ManifestEditorActivator.getDefault().getDialogSettings();
	}

	@Override
	protected IPreferenceStore getPreferencesStore() {
		return ManifestEditorActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected String getPluginId() {
		return ManifestEditorActivator.PLUGIN_ID;
	}

	@Override
	protected YamlAssistContextProvider getAssistContextProvider(ISourceViewer viewer) {
		return null;
	}

	@Override
	protected ITextHover getTextAnnotationHover(ISourceViewer sourceViewer) {
		return new LSBasedAnnotationHover(sourceViewer, getQuickfixContext(sourceViewer));
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover() {
			@Override
			protected boolean isIncluded(Annotation annotation) {
				return LSBasedAnnotationHover.isLspAnnotation(annotation);
			}
		};
	}
	
}
