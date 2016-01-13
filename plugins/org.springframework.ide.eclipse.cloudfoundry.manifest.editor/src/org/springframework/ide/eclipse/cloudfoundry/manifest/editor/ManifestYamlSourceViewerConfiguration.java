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

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.completions.ProposalProcessor;

public class ManifestYamlSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	private final String DIALOG_SETTINGS_KEY = this.getClass().getName();
	private ICompletionEngine completionEngine;

	public ManifestYamlSourceViewerConfiguration() {
	}

	@Override
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
			ProposalProcessor processor = new ProposalProcessor(getCompletionEngine());
			a.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
			a.setSorter(CompletionFactory.SORTER);
		}
		return _a;
	}

	protected ICompletionEngine getCompletionEngine() {
		if (completionEngine==null) {
			completionEngine = new DummyCompletionEngine();
		}
		return completionEngine;
	}

	private IDialogSettings getDialogSettings(ISourceViewer sourceViewer, String dialogSettingsKey) {
		IDialogSettings dialogSettings = ManifestEditorActivator.getDefault().getDialogSettings();
		IDialogSettings existing = dialogSettings.getSection(DIALOG_SETTINGS_KEY);
		if (existing!=null) {
			return existing;
		}
		IDialogSettings created = dialogSettings.addNewSection(DIALOG_SETTINGS_KEY);
		Rectangle windowBounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
//		int suggestW = (int)(windowBounds.width*0.35);
//		int suggestH = (int)(suggestW*0.6);
//		if (suggestW>300) {
//			created.put(ContentAssistant.STORE_SIZE_X, suggestW);
//			created.put(ContentAssistant.STORE_SIZE_Y, suggestH);
//		}
		return created;
	}

}
