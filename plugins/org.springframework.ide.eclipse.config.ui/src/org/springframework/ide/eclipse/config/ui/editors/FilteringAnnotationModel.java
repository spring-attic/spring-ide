/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.wst.sse.ui.internal.reconcile.TemporaryAnnotation;

/**
 * A specialized annotation model that is delegating all of its real calls to
 * the underlying backing annotation model, but returns empty positions for
 * specific annotations in oder to avoid them being drawn on the overview ruler.
 *
 * This is used to hide those annotations from the overview ruler that are
 * purely created as markers for quick fixes.
 *
 * @author Martin Lippert
 */
public class FilteringAnnotationModel implements IAnnotationModel {

	public static final String QUICK_FIX_MARKER = "org.springframework.ide.eclipse.config.ui.editors.quickFixMarker";

	private final IAnnotationModel model;

	public FilteringAnnotationModel(IAnnotationModel model) {
		this.model = model;
	}

	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		this.model.addAnnotationModelListener(listener);
	}

	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		this.model.removeAnnotationModelListener(listener);
	}

	public void connect(IDocument document) {
		this.model.connect(document);
	}

	public void disconnect(IDocument document) {
		this.model.disconnect(document);
	}

	public void addAnnotation(Annotation annotation, Position position) {
		this.model.addAnnotation(annotation, position);
	}

	public void removeAnnotation(Annotation annotation) {
		this.model.removeAnnotation(annotation);
	}

	public Iterator getAnnotationIterator() {
		return this.model.getAnnotationIterator();
	}

	public Position getPosition(Annotation annotation) {
		if (annotation instanceof TemporaryAnnotation) {
			TemporaryAnnotation tempAnnotation = (TemporaryAnnotation) annotation;
			if (tempAnnotation.getAttributes() != null && tempAnnotation.getAttributes().containsKey(QUICK_FIX_MARKER)) {
				return null;
			}
		}
		return this.model.getPosition(annotation);
	}
}
