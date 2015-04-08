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
package org.springframework.ide.eclipse.yaml.editor;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesTextHover;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlASTProvider;
import org.yaml.snakeyaml.Yaml;

public class SpringYeditSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	public static void debug(String string) {
		System.out.println(string);
	}

//	public IContentAssistant getContentAssistant(ISourceViewer viewer) {
//		IContentAssistant a = super.getContentAssistant(viewer);
//
//		if (a instanceof ContentAssistant) {
//			//IContentAssistProcessor processor = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
//			//if (processor!=null) {
//			//TODO: don't overwrite existing processor but wrap it so
//			// we combine our proposals with existing propopals
//			//}
//			((ContentAssistant) a).enableAutoActivation(true);
//			SpringYamlContentAssistProcessor processor = new SpringYamlContentAssistProcessor();
//			((ContentAssistant) a).setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
//		}
//
//
//		return a;
//	}

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
	private IPropertyHoverInfoProvider hoverProvider = new YamlHoverInfoProvider(astProvider, indexProvider, DocumentContextFinder.DEFAULT);

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		ITextHover delegate = super.getTextHover(sourceViewer, contentType, stateMask);
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				return new SpringPropertiesTextHover(sourceViewer, hoverProvider, delegate);
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return delegate;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}

}
