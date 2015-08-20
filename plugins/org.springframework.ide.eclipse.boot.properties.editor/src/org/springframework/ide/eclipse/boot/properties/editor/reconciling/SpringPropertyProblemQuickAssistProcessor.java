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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.TextInvocationContext;
import org.eclipse.ui.internal.texteditor.spelling.NoCompletionsProposal;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;

@SuppressWarnings("restriction")
public class SpringPropertyProblemQuickAssistProcessor implements IQuickAssistProcessor {


	private static final ICompletionProposal[] NO_PROPOSALS=  new ICompletionProposal[] { new NoCompletionsProposal() };

	private IPreferenceStore preferences;

	public SpringPropertyProblemQuickAssistProcessor(IPreferenceStore preferences) {
		this.preferences = preferences;
	}

	public SpringPropertyProblemQuickAssistProcessor() {
		this(SpringPropertiesEditorPlugin.getDefault().getPreferenceStore());
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext quickAssistContext) {
		ISourceViewer viewer= quickAssistContext.getSourceViewer();
		int documentOffset= quickAssistContext.getOffset();

		int length= viewer != null ? viewer.getSelectedRange().y : -1;
		TextInvocationContext context= new TextInvocationContext(viewer, documentOffset, length);


		IAnnotationModel model= viewer.getAnnotationModel();
		if (model == null)
			return NO_PROPOSALS;

		List<ICompletionProposal> proposals= computeProposals(context, model);
		if (proposals.isEmpty())
			return NO_PROPOSALS;

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private boolean isAtPosition(int offset, Position pos) {
		return (pos != null) && (offset >= pos.getOffset() && offset <= (pos.getOffset() +  pos.getLength()));
	}

	private List<ICompletionProposal> computeProposals(IQuickAssistInvocationContext context, IAnnotationModel model) {
		int offset= context.getOffset();
		ArrayList<SpringPropertyProblem> annotationList= new ArrayList<SpringPropertyProblem>();
		@SuppressWarnings("rawtypes")
		Iterator iter= model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= (Annotation)iter.next();
			if (canFix(annotation)) {
				Position pos= model.getPosition(annotation);
				if (isAtPosition(offset, pos)) {
					collectionProblems(annotation, annotationList);
				}
			}
		}
		SpringPropertyProblem[] problems= annotationList.toArray(new SpringPropertyProblem[annotationList.size()]);
		return computeProposals(context, problems);
	}

	private void collectionProblems(Annotation annotation, ArrayList<SpringPropertyProblem> annotationList) {
		if (annotation instanceof SpringPropertyAnnotation) {
			annotationList.add(((SpringPropertyAnnotation)annotation).getSpringPropertyProblem());
		}
	}

	private List<ICompletionProposal> computeProposals(IQuickAssistInvocationContext context, SpringPropertyProblem[] problems) {
		List<ICompletionProposal> proposals= new ArrayList<ICompletionProposal>();
		for (SpringPropertyProblem problem : problems) {
			proposals.add(new IgnoreProblemTypeQuickfix(preferences, problem.getType()));
		}
		return proposals;
	}

	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		return annotation instanceof SpringPropertyAnnotation && !annotation.isMarkedDeleted();
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canAssist(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
	 */
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return false;
	}



}
