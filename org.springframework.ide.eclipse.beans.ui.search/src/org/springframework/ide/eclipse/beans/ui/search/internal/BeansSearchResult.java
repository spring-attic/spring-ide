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
package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Dave Watkins
 * @author Torsten Juergeleit
 */
public class BeansSearchResult extends AbstractTextSearchResult implements
		IEditorMatchAdapter, IFileMatchAdapter {

	public static final Match[] NO_MATCH = new Match[0];

	private ISearchQuery query;

	public BeansSearchResult(ISearchQuery query) {
		this.query = query;
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
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

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			return match.getElement().equals(fileEditorInput.getFile());
		}
		return false;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result,
			IFile file) {
		return getMatches(file);
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

	public ImageDescriptor getImageDescriptor() {
		return BeansUIImages.DESC_OBJS_SPRING;
	}

	public String getLabel() {
		return query.getLabel();
	}

	public ISearchQuery getQuery() {
		return query;
	}

	public String getTooltip() {
		return getLabel();
	}

	@Override
	public String toString() {
		return "Results for " + getQuery().getLabel() + " # = "
				+ getMatchCount();
	}
}
