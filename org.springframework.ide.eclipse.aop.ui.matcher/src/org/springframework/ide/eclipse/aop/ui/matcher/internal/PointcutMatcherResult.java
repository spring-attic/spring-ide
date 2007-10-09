/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * {@link ISearchResult} implementation that encapsulates a
 * {@link PointcutMatchQuery} and the results of the query.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatcherResult extends AbstractTextSearchResult implements
		IEditorMatchAdapter, IFileMatchAdapter {

	public static final Match[] NO_MATCH = new Match[0];

	private PointcutMatchQuery query;

	public PointcutMatcherResult(PointcutMatchQuery query) {
		this.query = query;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result,
			IEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			return getMatches(fileEditorInput.getFile());
		}
		return NO_MATCH;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result,
			IFile file) {
		return getMatches(file);
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	public IFile getFile(Object element) {
		if (element instanceof ISourceModelElement) {
			IResource resource = ((ISourceModelElement) element)
					.getElementResource();
			if (resource instanceof IFile) {
				return (IFile) resource;
			}
		}
		return null;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public ImageDescriptor getImageDescriptor() {
		return BeansUIImages.DESC_OBJS_SPRING;
	}

	public String getLabel() {
		return MessageUtils.format(PointcutMatcherMessages.MatcherResult_label,
				"'" + query.getExpression() + "'", getMatchCount(), query
						.getScope().getDescription());
	}

	public ISearchQuery getQuery() {
		return query;
	}

	public String getTooltip() {
		return getLabel();
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			return match.getElement().equals(fileEditorInput.getFile());
		}
		return false;
	}

	@Override
	public String toString() {
		return "Results for " + getQuery().getLabel() + " # = "
				+ getMatchCount();
	}
}
