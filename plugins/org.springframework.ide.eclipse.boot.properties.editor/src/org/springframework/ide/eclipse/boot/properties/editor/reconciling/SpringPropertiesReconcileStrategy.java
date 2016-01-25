/*******************************************************************************
 * Copyright (c) 2006, 2008, 2014, 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - copied from org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy and modified
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.IReconcileTrigger;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileStrategy;
import org.springframework.ide.eclipse.editor.support.reconcile.SeverityProvider;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesReconcileStrategy extends ReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, IPropertyChangeListener {

	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);

	private final IReconcileTrigger fReconcileTrigger;
	private final DocumentContextFinder fDocumentContextFinder;

	private SeverityProvider fSeverities;
	private IPreferenceStore fProjectPreferences;

	public SpringPropertiesReconcileStrategy(ISourceViewer viewer, IReconcileEngine engine, DocumentContextFinder documentContextFinder, IReconcileTrigger reconcileTrigger) {
		super(viewer, engine);
		Assert.isNotNull(viewer);
		fDocumentContextFinder = documentContextFinder;
		fReconcileTrigger = reconcileTrigger;
	}


	/**
	 * Returns the content type of the underlying editor input.
	 *
	 * @return the content type of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	protected IContentType getContentType() {
		return TEXT_CONTENT_TYPE;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		super.setDocument(document);
		fSeverities = fDocumentContextFinder.getSeverityProvider(document);
		if (fProjectPreferences!=null) {
			fProjectPreferences.removePropertyChangeListener(this);
		}
		IProject project = getProject();
		if (project!=null) {
			fProjectPreferences = new ScopedPreferenceStore(new ProjectScope(project), SpringPropertiesEditorPlugin.PLUGIN_ID);
			fProjectPreferences.addPropertyChangeListener(this);
		}
	}

	private IProject getProject() {
		IDocument document = getDocument();
		if (document!=null) {
			IJavaProject jp = fDocumentContextFinder.getJavaProject(document);
			if (jp!=null) {
				return jp.getProject();
			}
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith(ProblemSeverityPreferencesUtil.PREFERENCE_PREFIX)) {
			fReconcileTrigger.forceReconcile();
		}
	}

	@Override
	protected SeverityProvider getSeverities() {
		if (fSeverities==null) {
			return super.getSeverities();
		}
		return fSeverities;
	}

}
