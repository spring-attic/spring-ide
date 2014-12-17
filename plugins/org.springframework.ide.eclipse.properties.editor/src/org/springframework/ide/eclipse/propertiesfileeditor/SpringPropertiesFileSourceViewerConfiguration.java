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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

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
				a.enableColoredLabels(true);
				a.enableAutoActivation(true);
				a.setInformationControlCreator(new SpringPropertiesInformationControlCreator());
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

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,String contentType) {
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				SpringPropertiesCompletionEngine engine = getEngine();
				if (engine!=null) {
					return new SpringPropertiesTextHover(sourceViewer, contentType, engine);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return super.getTextHover(sourceViewer, contentType);
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				SpringPropertiesCompletionEngine engine = getEngine();
				if (engine!=null) {
					return new SpringPropertiesTextHover(sourceViewer, contentType, engine);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return super.getTextHover(sourceViewer, contentType, stateMask);
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
	
}
