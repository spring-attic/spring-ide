/*******************************************************************************
 * Copyright (c) 2006, 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - copied from org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy and modified 
 *                      from for spring properties reconciling
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.reconciling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;

import static org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesCompletionEngine.ASSIGNABLE_TYPES;

/**
 * Reconcile strategy used for spell checking.
 *
 * @since 3.3
 */
public class SpringPropertiesReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {


	/**
	 * Spelling problem collector.
	 */
	private static class ProblemCollector implements IProblemCollector {

		/** Annotation model. */
		private IAnnotationModel fAnnotationModel;

		/** Annotations to add. */
		private Map fAddAnnotations;

		/** Lock object for modifying the annotations. */
		private Object fLockObject;

		/**
		 * Initializes this collector with the given annotation model.
		 *
		 * @param annotationModel the annotation model
		 */
		public ProblemCollector(IAnnotationModel annotationModel) {
			Assert.isLegal(annotationModel != null);
			fAnnotationModel= annotationModel;
			if (fAnnotationModel instanceof ISynchronizable)
				fLockObject= ((ISynchronizable)fAnnotationModel).getLockObject();
			else
				fLockObject= fAnnotationModel;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#accept(org.eclipse.ui.texteditor.spelling.SpellingProblem)
		 */
		public void accept(SpringPropertyProblem problem) {
			fAddAnnotations.put(new SpringPropertyAnnotation(problem), new Position(problem.getOffset(), problem.getLength()));
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginCollecting()
		 */
		public void beginCollecting() {
			fAddAnnotations= new HashMap();
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endCollecting()
		 */
		public void endCollecting() {

			List toRemove= new ArrayList();

			synchronized (fLockObject) {
				Iterator iter= fAnnotationModel.getAnnotationIterator();
				while (iter.hasNext()) {
					Annotation annotation= (Annotation)iter.next();
					if (SpringPropertyAnnotation.TYPE.equals(annotation.getType()))
						toRemove.add(annotation);
				}
				Annotation[] annotationsToRemove= (Annotation[])toRemove.toArray(new Annotation[toRemove.size()]);

				if (fAnnotationModel instanceof IAnnotationModelExtension)
					((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(annotationsToRemove, fAddAnnotations);
				else {
					for (int i= 0; i < annotationsToRemove.length; i++)
						fAnnotationModel.removeAnnotation(annotationsToRemove[i]);
					for (iter= fAddAnnotations.keySet().iterator(); iter.hasNext();) {
						Annotation annotation= (Annotation)iter.next();
						fAnnotationModel.addAnnotation(annotation, (Position)fAddAnnotations.get(annotation));
					}
				}
			}

			fAddAnnotations= null;
		}
	}


	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
	
	/** The text editor to operate on. */
	private ISourceViewer fViewer;

	/** The document to operate on. */
	private IDocument fDocument;

	/** The progress monitor. */
	private IProgressMonitor fProgressMonitor;

	private ProblemCollector fProblemCollector;

	private SpringPropertiesReconcileEngine fEngine;

	/**
	 * Creates a new comment reconcile strategy.
	 *
	 * @param viewer the source viewer
	 * @param spellingService the spelling service to use
	 */
	public SpringPropertiesReconcileStrategy(ISourceViewer viewer, FuzzyMap<ConfigurationMetadataProperty> index) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(index);
		fViewer= viewer;
		fEngine = new SpringPropertiesReconcileEngine(index);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile(new Region(0, fDocument.getLength()));
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		try {
			IRegion startLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset());
			IRegion endLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset() + Math.max(0, subRegion.getLength() - 1));
			if (startLineInfo.getOffset() == endLineInfo.getOffset())
				subRegion= startLineInfo;
			else
				subRegion= new Region(startLineInfo.getOffset(), endLineInfo.getOffset() + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());

		} catch (BadLocationException e) {
			subRegion= new Region(0, fDocument.getLength());
		}
		reconcile(subRegion);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion region) {
		if (getAnnotationModel() == null || fProblemCollector == null)
			return;
		//Note: This isn't an 'incremental' reconciler. It always checks the whole document. The dirty
		// region is ignored.
		fEngine.reconcile(fDocument, fProblemCollector, fProgressMonitor);
	}

	
	
	/**
	 * Returns the content type of the underlying editor input.
	 *
	 * @return the content type of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	protected IContentType getContentType() {
		return TEXT_CONTENT_TYPE;
	}

	/**
	 * Returns the document which is spell checked.
	 *
	 * @return the document
	 */
	protected final IDocument getDocument() {
		return fDocument;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument= document;
		fProblemCollector= createSpellingProblemCollector();
	}

	/**
	 * Creates a new spelling problem collector.
	 *
	 * @return the collector or <code>null</code> if none is available
	 */
	protected ProblemCollector createSpellingProblemCollector() {
		IAnnotationModel model= getAnnotationModel();
		if (model == null)
			return null;
		return new ProblemCollector(model);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	/**
	 * Returns the annotation model to be used by this reconcile strategy.
	 *
	 * @return the annotation model of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	protected IAnnotationModel getAnnotationModel() {
		return fViewer.getAnnotationModel();
	}

}
