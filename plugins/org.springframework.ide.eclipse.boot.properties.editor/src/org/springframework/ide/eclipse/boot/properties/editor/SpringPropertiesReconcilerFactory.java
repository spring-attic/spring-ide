/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.ReconcilingUtil;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;

public abstract class SpringPropertiesReconcilerFactory {

	/**
	 * Forces the spell checker in properties and yaml editor to be disabled, regardless of
	 * preferences for general text editors.
	 */
	private boolean DISABLE_SPELL_CHECKER = true;

	public ForceableReconciler createReconciler(ISourceViewer sourceViewer, DocumentContextFinder documentContextFinder, IReconcileTrigger reconcileTigger) {
		IReconcilingStrategy strategy = null;
		if (!DISABLE_SPELL_CHECKER && EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) {
			IReconcilingStrategy spellcheck = new SpellingReconcileStrategy(sourceViewer, EditorsUI.getSpellingService()) {
				@Override
				protected IContentType getContentType() {
					return SpringPropertiesFileEditor.CONTENT_TYPE;
				}
			};
			strategy = ReconcilingUtil.compose(strategy, spellcheck);
		}
		try {
			IReconcileEngine reconcileEngine = createEngine();
			IReconcilingStrategy propertyChecker = new SpringPropertiesReconcileStrategy(sourceViewer, reconcileEngine, documentContextFinder, reconcileTigger);
			strategy = ReconcilingUtil.compose(strategy, propertyChecker);
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		if (strategy!=null) {
			ForceableReconciler reconciler = new ForceableReconciler(strategy);
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}

	protected abstract IReconcileEngine createEngine() throws Exception;

}
