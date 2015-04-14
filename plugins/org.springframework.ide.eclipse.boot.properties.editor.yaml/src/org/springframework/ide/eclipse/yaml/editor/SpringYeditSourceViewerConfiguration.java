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

import static org.springframework.ide.eclipse.boot.properties.editor.util.HyperlinkDetectorUtil.merge;

import java.util.HashSet;
import java.util.Set;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesHyperlinkDetector;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesReconciler;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesReconcilerFactory;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesTextHover;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlASTProvider;
import org.springframework.ide.eclipse.yaml.editor.reconcile.SpringYamlReconcileEngine;
import org.yaml.snakeyaml.Yaml;

public class SpringYeditSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	private static final Set<String> ANNOTIONS_SHOWN_IN_TEXT = new HashSet<>();
	static {
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.warning");
		ANNOTIONS_SHOWN_IN_TEXT.add("org.eclipse.jdt.ui.error");
	}

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
	private TypeUtilProvider typeUtilProvider = new TypeUtilProvider() {
		@Override
		public TypeUtil getTypeUtil(IDocument doc) {
			return new TypeUtil(DocumentUtil.getJavaProject(doc));
		}
	};

	private IPropertyHoverInfoProvider hoverProvider = new YamlHoverInfoProvider(astProvider, indexProvider, DocumentContextFinder.DEFAULT);
	private SpringPropertiesReconciler fReconciler;
	private SpringPropertiesReconcilerFactory fReconcilerFactory = new SpringPropertiesReconcilerFactory() {

		protected IReconcileEngine createEngine() throws Exception {
			return new SpringYamlReconcileEngine(astProvider, indexProvider, typeUtilProvider);
		}
	};


	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE) && ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK==stateMask) {
			ITextHover delegate = super.getTextHover(sourceViewer, contentType, stateMask);
			if (delegate == null) {
				//why doesn't YeditSourceViewer configuration provide a good default?
				delegate = new DefaultTextHover(sourceViewer) {
					protected boolean isIncluded(Annotation annotation) {
						return ANNOTIONS_SHOWN_IN_TEXT.contains(annotation.getType());
					}
				};
			}
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

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fReconciler==null) {
			fReconciler = fReconcilerFactory.createReconciler(sourceViewer);
		}
		return fReconciler;
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

}
