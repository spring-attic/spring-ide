/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/package org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestEditorActivator;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.reconcile.AbstractAnnotationHover;
import org.springframework.ide.eclipse.editor.support.reconcile.QuickfixContext;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;

/**
 * Annotation hover for annotation markers produced by LSP4E
 *  
 * @author Alex Boyko
 *
 */
public class LSBasedAnnotationHover extends AbstractAnnotationHover<MarkerAnnotation> {
	
	private static final String LS_DIAGNOSTIC_MARKER_TYPE = "org.eclipse.lsp4e.diagnostic";

	static boolean isLspAnnotation(Annotation annotation) {
		if (annotation instanceof MarkerAnnotation) {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
			try {
				return LS_DIAGNOSTIC_MARKER_TYPE.equals(markerAnnotation.getMarker().getType());
			} catch (CoreException e) {
				ManifestEditorActivator.log(e);
			}
		}
		return false;
	}
	
	public LSBasedAnnotationHover(ISourceViewer sourceViewer, QuickfixContext context) {
		super(sourceViewer, context, MarkerAnnotation.class);
	}

	@Override
	protected boolean acceptAnnotation(MarkerAnnotation annotation) {
		return isLspAnnotation(annotation) && super.acceptAnnotation(annotation);
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		MarkerAnnotation annot = getAnnotationAt(model, hoverRegion.getOffset());
		if (annot!=null) {
			return new HoverInfo() {
				@Override
				protected String renderAsHtml() {
					HtmlBuffer html = new HtmlBuffer();
					html.text(annot.getText());
					return html.toString();
				}
			};
		}
		return null;
	}
	
}
