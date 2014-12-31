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
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertiesReconcileStrategy;

@SuppressWarnings("restriction")
public class SpringPropertiesFileSourceViewerConfiguration 
extends PropertiesFileSourceViewerConfiguration {

	private static final String DIALOG_SETTINGS_KEY = null;
	private SpringPropertiesCompletionEngine engine;

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
				a.setRestoreCompletionProposalSize(getDialogSettings(DIALOG_SETTINGS_KEY));
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
	
	private FuzzyMap<PropertyInfo> getIndex() throws Exception {
		return getEngine().getIndex();
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
	
	private static IDialogSettings getDialogSettings(String dialogSettingsKey) {
		IDialogSettings existing = SpringPropertiesEditorPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (existing!=null) {
			return existing;
		}
		return SpringPropertiesEditorPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
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
			IReconcilingStrategy propertyChecker = new SpringPropertiesReconcileStrategy(sourceViewer, getIndex());
			strategy = compose(strategy, propertyChecker);
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		if (strategy!=null) {
			MonoReconciler reconciler = new MonoReconciler(strategy, false);
			reconciler.setDelay(500);
			return reconciler;
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
	
}
