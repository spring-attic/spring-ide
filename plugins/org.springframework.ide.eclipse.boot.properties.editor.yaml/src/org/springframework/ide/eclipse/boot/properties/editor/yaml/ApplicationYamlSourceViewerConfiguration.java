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
package org.springframework.ide.eclipse.boot.properties.editor.yaml;

import static org.springframework.ide.eclipse.boot.properties.editor.util.HyperlinkDetectorUtil.merge;

import java.util.HashSet;
import java.util.Set;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinders;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.IReconcileTrigger;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.RelaxedNameConfig;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesAnnotationHover;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesHyperlinkDetector;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesReconciler;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesReconcilerFactory;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesTextHover;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.DefaultQuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.QuickfixContext;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.SpringPropertyProblemQuickAssistProcessor;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.ui.DefaultUserInteractions;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.completions.ApplicationYamlCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile.SpringYamlReconcileEngine;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.completions.ProposalProcessor;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("restriction")
public class ApplicationYamlSourceViewerConfiguration extends YEditSourceViewerConfiguration implements IReconcileTrigger {

	private static final String DIALOG_SETTINGS_KEY = ApplicationYamlSourceViewerConfiguration.class.getName();

	private static final DocumentContextFinder documentContextFinder = DocumentContextFinders.YAML_DEFAULT;
	private static final Set<String> ANNOTIONS_SHOWN_IN_TEXT = new HashSet<String>();
	static {
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.warning");
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.error");
	}
	private static final Set<String> ANNOTIONS_SHOWN_IN_OVERVIEW_BAR = ANNOTIONS_SHOWN_IN_TEXT;

	//TODO: the ANNOTIONS_SHOWN_IN_TEXT and ANNOTIONS_SHOWN_IN_OVERVIEW_BAR should be replaced with
	// properly using preferences. An example of how to set this up can be found in the code
	// of the Java properties file editor. Roughly these things need to happen:
	//   1) use methods like 'isShownIntext' and 'isShownInOverviewRuler' which are defined in
	//     our super class.
	//   2) initialize the super class with a preference store (simialr to how java properties file does it)
	//   3) To be able to do 2) it is necessary to add a constructor to YEditSourceViewerConfiguration which
	//      accepts preference store and passes it to its super class. So this requires a patch to
	//      YEdit source code.

	public static void debug(String string) {
		System.out.println(string);
	}

	private ITextEditor editor;

	public ApplicationYamlSourceViewerConfiguration(ITextEditor editor) {
		super();
		this.editor = editor;
	}

	private IDialogSettings getDialogSettings(ISourceViewer sourceViewer, String dialogSettingsKey) {
		IDialogSettings dialogSettings = YamlEditorPlugin.getDefault().getDialogSettings();
		IDialogSettings existing = dialogSettings.getSection(DIALOG_SETTINGS_KEY);
		if (existing!=null) {
			return existing;
		}
		IDialogSettings created = dialogSettings.addNewSection(DIALOG_SETTINGS_KEY);
		Rectangle windowBounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		int suggestW = (int)(windowBounds.width*0.35);
		int suggestH = (int)(suggestW*0.6);
		if (suggestW>300) {
			created.put(ContentAssistant.STORE_SIZE_X, suggestW);
			created.put(ContentAssistant.STORE_SIZE_Y, suggestH);
		}
		return created;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,String contentType) {
		return getTextHover(sourceViewer, contentType, 0);
	}

	private Yaml yaml = new Yaml();
	private YamlASTProvider astProvider = new YamlASTProvider(yaml);
	private SpringPropertyIndexProvider indexProvider = new SpringPropertyIndexProvider() {
		@Override
		public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
			IJavaProject jp = DocumentUtil.getJavaProject(doc);
			if (jp!=null) {
				return SpringPropertiesEditorPlugin.getIndexManager().get(jp);
			}
			return null;
		}

	};
	private TypeUtilProvider typeUtilProvider = new TypeUtilProvider() {
		@Override
		public TypeUtil getTypeUtil(IDocument doc) {
			return new TypeUtil(DocumentUtil.getJavaProject(doc));
		}
	};

	private IPropertyHoverInfoProvider hoverProvider = new YamlHoverInfoProvider(astProvider, indexProvider, documentContextFinder);
	private SpringPropertiesReconciler fReconciler;
	private SpringPropertiesReconcilerFactory fReconcilerFactory = new SpringPropertiesReconcilerFactory() {
		protected IReconcileEngine createEngine() throws Exception {
			return new SpringYamlReconcileEngine(astProvider, indexProvider, typeUtilProvider);
		}
	};

	private YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;

	private ICompletionEngine completionEngine = new ApplicationYamlCompletionEngine(yaml, indexProvider, documentContextFinder, structureProvider, typeUtilProvider,
			RelaxedNameConfig.COMPLETION_DEFAULTS);

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE) && ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK==stateMask) {
			ITextHover delegate = new SpringPropertiesAnnotationHover(sourceViewer, getQuickfixContext(sourceViewer));
			try {
				return new SpringPropertiesTextHover(sourceViewer, hoverProvider, delegate);
			} catch (Exception e) {
				SpringPropertiesEditorPlugin.log(e);
			}
			return delegate;
		} else {
			return super.getTextHover(sourceViewer, contentType, stateMask);
		}
	}

	protected QuickfixContext getQuickfixContext(ISourceViewer sourceViewer) {
		return new DefaultQuickfixContext(getPreferencesStore(), sourceViewer, new DefaultUserInteractions(getShell()));
	}

	private Shell getShell() {
		return editor.getSite().getShell();
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover() {
			@Override
			protected boolean isIncluded(Annotation annotation) {
				return ANNOTIONS_SHOWN_IN_OVERVIEW_BAR.contains(annotation.getType());
			}
		};
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fReconciler==null) {
			fReconciler = fReconcilerFactory.createReconciler(sourceViewer, documentContextFinder, this);
		}
		return fReconciler;
	}

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

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		SpringPropertiesHyperlinkDetector myDetector = null;
		try {
			myDetector = new SpringPropertiesHyperlinkDetector(hoverProvider);
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return merge(
				super.getHyperlinkDetectors(sourceViewer),
				myDetector
		);
	}


	public IContentAssistant getContentAssistant(ISourceViewer viewer) {
		IContentAssistant _a = super.getContentAssistant(viewer);

		if (_a instanceof ContentAssistant) {
			ContentAssistant a = (ContentAssistant)_a;
			//IContentAssistProcessor processor = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
			//if (processor!=null) {
			//TODO: don't overwrite existing processor but wrap it so
			// we combine our proposals with existing propopals
			//}

		    a.setInformationControlCreator(getInformationControlCreator(viewer));
		    a.enableColoredLabels(true);
		    a.enablePrefixCompletion(false);
		    a.enableAutoInsert(true);
		    a.enableAutoActivation(true);
			a.setRestoreCompletionProposalSize(getDialogSettings(viewer, DIALOG_SETTINGS_KEY));
			ProposalProcessor processor = new ProposalProcessor(completionEngine);
			a.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
			a.setSorter(PropertyCompletionFactory.SORTER);
		}
		return _a;
	}

	public void forceReconcile() {
		if (fReconciler!=null) {
			fReconciler.forceReconcile();
		}
	}


}
