/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylyn.ui.editor;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.beans.ui.editor.BeansStructuredTextViewerConfiguration;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class FocusedBeansStructuredTextViewerConfiguration extends
		BeansStructuredTextViewerConfiguration {

	@Override
	public IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {

		IContentAssistProcessor[] processors = super
				.getContentAssistProcessors(sourceViewer, partitionType);
		if (processors != null) {
			IContentAssistProcessor[] wrappedProcessors = 
				new IContentAssistProcessor[processors.length];
			for (int i = 0; i < processors.length; i++) {
				wrappedProcessors[i] = new FocusedBeansContentAssistProcessor(
						processors[i]);
			}
			return wrappedProcessors;
		}
		return processors;
	}
}
