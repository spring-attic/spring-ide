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
package org.springframework.ide.eclipse.editor.support.reconcile;

import java.util.Iterator;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.editor.support.hover.HoverInformationControlCreator;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.QuickfixContext;

public class ReconcileProblemAnnotationHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	private ISourceViewer sourceViewer;
	private QuickfixContext context;

	public ReconcileProblemAnnotationHover(ISourceViewer sourceViewer, QuickfixContext context) {
		this.sourceViewer = sourceViewer;
		this.context = context;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		ReconcileProblemAnnotation annot = getAnnotationAt(model, hoverRegion.getOffset());
		if (annot!=null) {
			return annot.getText();
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		ReconcileProblemAnnotation annot = getAnnotationAt(model, offset);
		if (annot!=null) {
			Position pos = model.getPosition(annot);
			if (pos!=null) {
				return new Region(pos.getOffset(), pos.getLength());
			}
		}
		return null;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		ReconcileProblemAnnotation annot = getAnnotationAt(model, hoverRegion.getOffset());
		if (annot!=null) {
			return new SpringPropertyProblemHoverInfo(annot.getSpringPropertyProblem(), context);
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new HoverInformationControlCreator(false, "F2 for focus");
	}

	/////////////////////////////////////////////////////////////////////

	private ReconcileProblemAnnotation getAnnotationAt(IAnnotationModel model, int offset) {
		if (model!=null) {
			@SuppressWarnings("rawtypes")
			Iterator iter= model.getAnnotationIterator();
			ReconcileProblemAnnotation found = null;
			Position foundPos = null;
			while (iter.hasNext()) {
				Object _annotation= iter.next();
				if (_annotation instanceof ReconcileProblemAnnotation) {
					ReconcileProblemAnnotation annotation = (ReconcileProblemAnnotation) _annotation;
					Position pos= model.getPosition(annotation);
					if (isAtPosition(offset, pos)) {
						if (foundPos==null || pos.length<foundPos.length) {
							found = annotation;
							foundPos = pos;
						}
					}
				}
			}
			return found;
		}
		return null;
	}

	private boolean isAtPosition(int offset, Position pos) {
		return (pos != null) && (offset >= pos.getOffset() && offset <= (pos.getOffset() +  pos.getLength()));
	}

}
