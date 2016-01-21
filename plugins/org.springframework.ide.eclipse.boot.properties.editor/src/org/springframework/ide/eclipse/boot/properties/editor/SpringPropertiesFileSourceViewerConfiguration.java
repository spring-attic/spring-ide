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
package org.springframework.ide.eclipse.boot.properties.editor;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.DefaultQuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.QuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.SpringPropertyProblemQuickAssistProcessor;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.ui.DefaultUserInteractions;
import org.springframework.ide.eclipse.boot.properties.editor.util.HyperlinkDetectorUtil;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.ProposalProcessor;
import org.springframework.ide.eclipse.editor.support.hover.HoverInformationControlCreator;
import org.springframework.ide.eclipse.editor.support.hover.SpringPropertiesTextHover;

@SuppressWarnings("restriction")
public class SpringPropertiesFileSourceViewerConfiguration
extends PropertiesFileSourceViewerConfiguration implements IReconcileTrigger {

	private static final String DIALOG_SETTINGS_KEY = PropertiesFileSourceViewerConfiguration.class.getName();
	private static final DocumentContextFinder documentContextFinder = DocumentContextFinders.PROPS_DEFAULT;
	private SpringPropertiesCompletionEngine engine;
	private SpringPropertiesReconciler fReconciler;
	private SpringPropertiesReconcilerFactory fReconcilerFactory = new SpringPropertiesReconcilerFactory() {
		@Override
		protected IReconcileEngine createEngine() throws Exception {
			return new SpringPropertiesReconcileEngine(getEngine().getIndexProvider(), getEngine().getTypeUtil());
		}
	};
	private ITextEditor editor;

	public SpringPropertiesFileSourceViewerConfiguration(
			IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
		this.editor = editor;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		try {
			SpringPropertiesCompletionEngine engine = getEngine();
			if (engine!=null) {
				ContentAssistant a = new ContentAssistant();
				a.setDocumentPartitioning(IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING);
				a.setContentAssistProcessor(new ProposalProcessor(getEngine()), IDocument.DEFAULT_CONTENT_TYPE);
				a.setContentAssistProcessor(new ProposalProcessor(getEngine()), IPropertiesFilePartitions.PROPERTY_VALUE);
				a.enableColoredLabels(true);
				a.enableAutoActivation(true);
				a.setInformationControlCreator(new HoverInformationControlCreator(JavaPlugin.getAdditionalInfoAffordanceString()));
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
		ITextHover delegate = new SpringPropertiesAnnotationHover(sourceViewer, getQuickfixContext(sourceViewer));
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				SpringPropertiesCompletionEngine engine = getEngine();
				if (engine!=null) {
					return new SpringPropertiesTextHover(sourceViewer, engine, delegate);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return delegate;
	}

	protected QuickfixContext getQuickfixContext(ISourceViewer sourceViewer) {
		return new DefaultQuickfixContext(getPreferencesStore(), sourceViewer,
				new DefaultUserInteractions(getShell()));
	}

	protected Shell getShell() {
		return editor.getSite().getShell();
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
			m.invoke(a, CompletionFactory.SORTER);
		} catch (Throwable e) {
			//ignore, sorter not supported with Eclipse 3.7 API
		}
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fReconciler==null) {
			fReconciler = fReconcilerFactory.createReconciler(sourceViewer, documentContextFinder, this);
		}
		return fReconciler;
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		SpringPropertiesHyperlinkDetector myDetector = null;
		try {
			myDetector = new SpringPropertiesHyperlinkDetector(getEngine());
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return HyperlinkDetectorUtil.merge(
				super.getHyperlinkDetectors(sourceViewer),
				myDetector
		);
	}

//	@Override
//	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
//		return super.getHyperlinkPresenter(sourceViewer);
//	}

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		QuickAssistAssistant assistant= new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new SpringPropertyProblemQuickAssistProcessor(getPreferencesStore(), new DefaultUserInteractions(getShell())));
		assistant.setRestoreCompletionProposalSize(EditorsPlugin.getDefault().getDialogSettingsSection("quick_assist_proposal_size")); //$NON-NLS-1$
		assistant.setInformationControlCreator(getQuickAssistAssistantInformationControlCreator());
		return assistant;
	}

	protected IPreferenceStore getPreferencesStore() {
		return SpringPropertiesEditorPlugin.getDefault().getPreferenceStore();
	}

	private IInformationControlCreator getQuickAssistAssistantInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsPlugin.getAdditionalInfoAffordanceString());
			}
		};
	}

	protected Map<String,IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> superTargets = super.getHyperlinkDetectorTargets(sourceViewer);
		superTargets.remove("org.eclipse.jdt.ui.PropertiesFileEditor"); //This just adds a 'search for' link which never seems to return anything useful
		return superTargets;
	};

	public void forceReconcile() {
		if (fReconciler!=null) {
			fReconciler.forceReconcile();
		}
	}

}
