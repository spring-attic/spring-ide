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
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileSourceViewerConfiguration;
import org.eclipse.jdt.internal.ui.text.CompositeReconcilingStrategy;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertiesReconcileStrategy;
import org.springframework.ide.eclipse.propertiesfileeditor.util.Provider;

@SuppressWarnings("restriction")
public class SpringPropertiesFileSourceViewerConfiguration
extends PropertiesFileSourceViewerConfiguration {

	/**
	 * WE unforytunately must subclass this just to make it possible to call non
	 * public method 'forceReconcile'.
	 *
	 * We need this to trigger a reconcile when live metadata has changed.
	 */
	public class SpringPropertiesReconciler extends MonoReconciler {

		public SpringPropertiesReconciler(IReconcilingStrategy strategy) {
			super(strategy, false);
		}

		public void forceReconcile() {
			super.forceReconciling();
		}

	}

	private static final String DIALOG_SETTINGS_KEY = PropertiesFileSourceViewerConfiguration.class.getName();
	private SpringPropertiesCompletionEngine engine;
	private SpringPropertiesReconciler fReconciler;

	public SpringPropertiesFileSourceViewerConfiguration(
			IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		try {
			SpringPropertiesCompletionEngine engine = getEngine();
			if (engine!=null) {
				ContentAssistant a = new ContentAssistant();
				a.setDocumentPartitioning(IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING);
				a.setContentAssistProcessor(new SpringPropertiesProposalProcessor(getEngine()), IDocument.DEFAULT_CONTENT_TYPE);
				a.setContentAssistProcessor(new SpringPropertiesProposalProcessor(getEngine()), IPropertiesFilePartitions.PROPERTY_VALUE);
				a.enableColoredLabels(true);
				a.enableAutoActivation(true);
				a.setInformationControlCreator(new SpringPropertiesInformationControlCreator(JavaPlugin.getAdditionalInfoAffordanceString()));
				setSorter(a);
				a.setRestoreCompletionProposalSize(getDialogSettings(sourceViewer, DIALOG_SETTINGS_KEY));
				return a;
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return null;
	}

	private SpringPropertiesCompletionEngine getEngine() throws Exception {
		if (engine==null) {
			ITextEditor editor = getEditor();
			if (editor!=null) {
				IJavaProject jp = EditorUtility.getJavaProject(getEditor().getEditorInput());
				if (jp!=null) {
					engine = new SpringPropertiesCompletionEngine(jp);
				}
			}
		}
		return engine;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,String contentType) {
		return getTextHover(sourceViewer, contentType, 0);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		ITextHover delegate = super.getTextHover(sourceViewer, contentType, stateMask);
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				SpringPropertiesCompletionEngine engine = getEngine();
				if (engine!=null) {
					return new SpringPropertiesTextHover(sourceViewer, contentType, engine, delegate);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return delegate;
	}

	private IDialogSettings getDialogSettings(ISourceViewer sourceViewer, String dialogSettingsKey) {
		IDialogSettings existing = SpringPropertiesEditorPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (existing!=null) {
			return existing;
		}
		IDialogSettings created = SpringPropertiesEditorPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
		Rectangle windowBounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		int suggestW = (int)(windowBounds.width*0.35);
		int suggestH = (int)(suggestW*0.6);
		if (suggestW>300) {
			created.put(ContentAssistant.STORE_SIZE_X, suggestW);
			created.put(ContentAssistant.STORE_SIZE_Y, suggestH);
		}
		return created;
	}

	public static void setSorter(ContentAssistant a) {
		try {
			Class<?> sorterInterface = Class.forName("org.eclipse.jface.text.contentassist.ICompletionProposalSorter");
			Method m = ContentAssistant.class.getMethod("setSorter", sorterInterface);
			m.invoke(a, SpringPropertiesCompletionEngine.SORTER);
		} catch (Throwable e) {
			//ignore, sorter not supported with Eclipse 3.7 API
		}
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		IReconcilingStrategy strategy = null;
		Provider<FuzzyMap<PropertyInfo>> indexProvider = null;
		if (EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) {
			IReconcilingStrategy spellcheck = new SpellingReconcileStrategy(sourceViewer, EditorsUI.getSpellingService()) {
				@Override
				protected IContentType getContentType() {
					return SpringPropertiesFileEditor.CONTENT_TYPE;
				}
			};
			strategy = compose(strategy, spellcheck);
		}
		try {
			indexProvider = getEngine().getIndexProvider();
			IReconcilingStrategy propertyChecker = new SpringPropertiesReconcileStrategy(sourceViewer, getEngine().getIndexProvider());
			strategy = compose(strategy, propertyChecker);
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		if (strategy!=null) {
			fReconciler = new SpringPropertiesReconciler(strategy);
			fReconciler.setDelay(500);
			return fReconciler;
		}
		return null;
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		SpringPropertiesHyperlinkDetector myDetector = null;
		try {
			myDetector = new SpringPropertiesHyperlinkDetector(getEngine());
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return merge(
				super.getHyperlinkDetectors(sourceViewer),
				myDetector
		);
	}

//	@Override
//	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
//		return super.getHyperlinkPresenter(sourceViewer);
//	}

	protected java.util.Map<String,org.eclipse.core.runtime.IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> superTargets = super.getHyperlinkDetectorTargets(sourceViewer);
		superTargets.remove("org.eclipse.jdt.ui.PropertiesFileEditor"); //This just adds a 'search for' link which never seems to return anything useful
		return superTargets;
	};

	private IHyperlinkDetector[] merge(IHyperlinkDetector[] a, SpringPropertiesHyperlinkDetector b) {
		if (a==null || a.length==0) {
			return new IHyperlinkDetector[] {b};
		}
		if (b==null) {
			return a;
		}
		IHyperlinkDetector[] merged = new IHyperlinkDetector[a.length+1];
		System.arraycopy(a, 0, merged, 0, a.length);
		merged[a.length] = b;
		return merged;
	}

	private IReconcilingStrategy compose(IReconcilingStrategy s1, IReconcilingStrategy s2) {
		if (s1==null) {
			return s2;
		}
		if (s2==null) {
			return s1;
		}
		CompositeReconcilingStrategy composite = new CompositeReconcilingStrategy();
		composite.setReconcilingStrategies(new IReconcilingStrategy[] {s1, s2});
		return composite;
	}

	public void forceReconcile() {
		if (fReconciler!=null) {
			fReconciler.forceReconcile();
		}
	}

}
