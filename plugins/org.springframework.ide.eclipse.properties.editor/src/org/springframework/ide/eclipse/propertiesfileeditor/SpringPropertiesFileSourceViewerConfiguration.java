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
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class SpringPropertiesFileSourceViewerConfiguration 
extends PropertiesFileSourceViewerConfiguration {

	private static final String DIALOG_SETTINGS_KEY = null;

	public SpringPropertiesFileSourceViewerConfiguration(
			IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		try {
			ITextEditor editor = getEditor();
			if (editor!=null) {
				IJavaProject jp = EditorUtility.getJavaProject(editor.getEditorInput());
				if (jp!=null) {
					ContentAssistant a = new ContentAssistant();
					a.setContentAssistProcessor(new SpringPropertiesProposalProcessor(jp), IDocument.DEFAULT_CONTENT_TYPE);
					a.enableColoredLabels(true);
					a.enableAutoActivation(true);
					a.setInformationControlCreator(new IInformationControlCreator() {
						public IInformationControl createInformationControl(Shell parent) {
							return new DefaultInformationControl(parent);
						}
					});
					setSorter(a);
					a.setRestoreCompletionProposalSize(getDialogSettings(DIALOG_SETTINGS_KEY));
					return a;
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return null;
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
