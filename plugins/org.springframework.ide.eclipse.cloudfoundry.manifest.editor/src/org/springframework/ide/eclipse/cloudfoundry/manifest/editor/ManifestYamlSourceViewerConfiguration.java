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

import java.util.Date;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.util.HtmlUtil;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class ManifestYamlSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration {

	private ICompletionEngine completionEngine;
	private ManifestYmlSchema schema = new ManifestYmlSchema();
	private YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;

	public ManifestYamlSourceViewerConfiguration() {
	}

	@Override
	public ICompletionEngine getCompletionEngine() {
		if (completionEngine==null) {
			completionEngine = new ManifestYamlCompletionEngine(structureProvider, schema);
		}
		return completionEngine;
	}

	@Override
	protected IDialogSettings getPluginDialogSettings() {
		return ManifestEditorActivator.getDefault().getDialogSettings();
	}

	@Override
	protected ITextHover getTextAnnotationHover(ISourceViewer sourceViewer) {
		return null;
	}

	protected HoverInfoProvider getHoverProvider() {
		return new HoverInfoProvider() {

			@Override
			public IRegion getHoverRegion(IDocument document, int offset) {
				return new Region(offset, 0);
			}

			@Override
			public HoverInfo getHoverInfo(IDocument doc, IRegion r) {
				return new HoverInfo() {
					@Override
					protected String renderAsHtml() {
						return HtmlUtil.text2html("Hover time: "+new Date().toString());
					}
				};
			}
		};
	}

}
