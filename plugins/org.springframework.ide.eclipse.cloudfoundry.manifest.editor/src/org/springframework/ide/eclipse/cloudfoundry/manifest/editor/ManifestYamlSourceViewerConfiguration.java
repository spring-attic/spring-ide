/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.completions.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlReconcileEngine;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class ManifestYamlSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration {

	private ManifestYmlSchema schema = new ManifestYmlSchema();
	private YamlAssistContextProvider assistContextProvider = new SchemaBasedYamlAssistContextProvider(schema);

	public ManifestYamlSourceViewerConfiguration() {
	}

	@Override
	protected YamlAssistContextProvider getAssistContextProvider() {
		return assistContextProvider;
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
	protected ITextHover getTextAnnotationHover(ISourceViewer sourceViewer) {
		return null;
	}

	@Override
	protected ForceableReconciler createReconciler(ISourceViewer sourceViewer) {
//		IReconcilingStrategy strategy = null;
//		if (!DISABLE_SPELL_CHECKER && EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) {
//			IReconcilingStrategy spellcheck = new SpellingReconcileStrategy(sourceViewer, EditorsUI.getSpellingService()) {
//				@Override
//				protected IContentType getContentType() {
//					return SpringPropertiesFileEditor.CONTENT_TYPE;
//				}
//			};
//			strategy = ReconcilingUtil.compose(strategy, spellcheck);
//		}
//		try {
//			IReconcileEngine reconcileEngine = createEngine();
//			IReconcilingStrategy propertyChecker = new SpringPropertiesReconcileStrategy(sourceViewer, reconcileEngine, documentContextFinder, reconcileTigger);
//			IReconcilingStrategy strategy = ReconcilingUtil.compose(strategy, propertyChecker);
//		} catch (Exception e) {
//			SpringPropertiesEditorPlugin.log(e);
//		}
//		if (strategy!=null) {
//			ForceableReconciler reconciler = new ForceableReconciler(strategy);
//			reconciler.setDelay(500);
//			return reconciler;
//		}
		return null;
	}

}
