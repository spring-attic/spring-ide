/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 22-Aug-2004
 */

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
	private static final Match[] NO_MATCH = new Match[0];

	private ISearchQuery query;

	public BeansSearchResult(ISearchQuery query) {
		this.query = query;
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}
	
	public Match[] computeContainedMatches(AbstractTextSearchResult result,
										   IEditorPart editor) {
		IEditorInput ei = editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi = (IFileEditorInput) ei;
			return getMatches(fi.getFile());
		}
		return NO_MATCH;
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei = editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi = (IFileEditorInput) ei;
			return match.getElement().equals(fi.getFile());
		}
		return false;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result,
										   IFile file) {
		return NO_MATCH;
	}

	public IFile getFile(Object element) {
		if (element instanceof ISourceModelElement) {
			IResource resource = ((ISourceModelElement)
												 element).getElementResource();
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

	public String toString() {
		return "Results for " + getQuery().getLabel() + " # = " +
			   getMatchCount();
	}
}
