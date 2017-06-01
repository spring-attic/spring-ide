/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinders;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.IReconcileTrigger;
import org.springframework.ide.eclipse.boot.properties.editor.RelaxedNameConfig;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesHyperlinkDetector;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesReconcilerFactory;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.SpringPropertyProblemQuickAssistProcessor;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.completions.ApplicationYamlAssistContextProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.util.DefaultUserInteractions;
import org.springframework.ide.eclipse.editor.support.util.DocumentUtil;
import org.springframework.ide.eclipse.editor.support.util.ShellProviders;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

@SuppressWarnings("restriction")
public class ApplicationYamlSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration implements IReconcileTrigger {

	public static final DocumentContextFinder documentContextFinder = DocumentContextFinders.YAML_DEFAULT;

	public static void debug(String string) {
		System.out.println(string);
	}


	public ApplicationYamlSourceViewerConfiguration(ITextEditor editor) {
		super(ShellProviders.from(editor));
	}

	@Override
	protected Point getDefaultPopupSize() {
		Rectangle windowBounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		int suggestW = (int)(windowBounds.width*0.35);
		int suggestH = (int)(suggestW*0.6);
		if (suggestW>300) {
			return new Point(suggestW, suggestH);
		}
		return null;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,String contentType) {
		return getTextHover(sourceViewer, contentType, 0);
	}

	SpringPropertyIndexProvider indexProvider = new SpringPropertyIndexProvider() {
		@Override
		public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
			IJavaProject jp = DocumentUtil.getJavaProject(doc);
			if (jp!=null) {
				return SpringPropertiesEditorPlugin.getIndexManager().get(jp);
			}
			return null;
		}

	};
	TypeUtilProvider typeUtilProvider = new TypeUtilProvider() {
		@Override
		public TypeUtil getTypeUtil(IDocument doc) {
			return new TypeUtil(DocumentUtil.getJavaProject(doc));
		}
	};

	private final YamlStructureProvider structureProvider = ApplicationYamlStructureProvider.INSTANCE;
	private final YamlAssistContextProvider assistContextProvider = new ApplicationYamlAssistContextProvider(indexProvider, typeUtilProvider, RelaxedNameConfig.COMPLETION_DEFAULTS, documentContextFinder);
	public final SpringPropertiesReconcilerFactory fReconcilerFactory = new SpringPropertiesReconcilerFactory() {
		protected IReconcileEngine createEngine() throws Exception {
			return new ApplicationYamlReconcileEngine(getAstProvider(), indexProvider, typeUtilProvider);
		}
	};

	final PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(documentContextFinder);

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		QuickAssistAssistant assistant= new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new SpringPropertyProblemQuickAssistProcessor(getPreferencesStore(), new DefaultUserInteractions(getShell())));
		assistant.setRestoreCompletionProposalSize(EditorsPlugin.getDefault().getDialogSettingsSection("quick_assist_proposal_size")); //$NON-NLS-1$
		assistant.setInformationControlCreator(getQuickAssistAssistantInformationControlCreator());
		return assistant;
	}

	@Override
	protected IPreferenceStore getPreferencesStore() {
		return SpringPropertiesEditorPlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected String getPluginId() {
		return SpringPropertiesEditorPlugin.PLUGIN_ID;
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
			myDetector = new SpringPropertiesHyperlinkDetector(getHoverProvider(sourceViewer));
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return merge(
				super.getHyperlinkDetectors(sourceViewer),
				myDetector
		);
	}

	public void forceReconcile() {
		if (fReconciler!=null) {
			fReconciler.forceReconcile();
		}
	}

	@Override
	protected IDialogSettings getPluginDialogSettings() {
		return YamlEditorPlugin.getDefault().getDialogSettings();
	}

	@Override
	public YamlStructureProvider getStructureProvider() {
		return structureProvider;
	}

	@Override
	public YamlAssistContextProvider getAssistContextProvider(ISourceViewer viewer) {
		return assistContextProvider;
	}

	@Override
	protected ForceableReconciler createReconciler(ISourceViewer sourceViewer) {
		return fReconcilerFactory.createReconciler(sourceViewer, documentContextFinder, this);
	}

}
