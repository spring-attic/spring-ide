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
 */ 

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Assert;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansQuery implements ISearchQuery {

	private ISearchResult searchResult;
	private BeansSearchScope searchScope;

	public AbstractBeansQuery(BeansSearchScope scope) {
		Assert.isNotNull(scope);
		searchScope = scope;
	}
	
	public final ISearchResult getSearchResult() {
		if (searchResult == null) {
			searchResult = new BeansSearchResult(this);
		}
		return searchResult;
	}

	public BeansSearchScope getSearchScope() {
		return searchScope;
	}
	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public final IStatus run(IProgressMonitor monitor) {
		final BeansSearchResult result = (BeansSearchResult) getSearchResult();
		result.removeAll();
		IModelElement[] elements = searchScope.getModelElements();
		for (int i = 0; !monitor.isCanceled() && i < elements.length; i++) {
			IModelElement element = elements[i];
			IModelElementVisitor visitor = new IModelElementVisitor() {
				public boolean visit(IModelElement element,
													IProgressMonitor monitor) {
					if (doesMatch(element, monitor)) {
						int startLine;
						int lines;
						if (element instanceof ISourceModelElement) {
							ISourceModelElement sourceElement = (ISourceModelElement)
							element;
							startLine = sourceElement.getElementStartLine();
							lines = sourceElement.getElementEndLine() - startLine + 1;
						} else {
							startLine = -1;
							lines = -1;
						}
						Match match = new Match(element, Match.UNIT_LINE, startLine,
								lines);
						result.addMatch(match);
					}
					return true;
				}
			};
			element.accept(visitor, monitor);
		}
		Object[] args = new Object[] { new Integer(result.getMatchCount()) };
		String message = BeansSearchPlugin.getFormattedMessage("Search.status",
															   args);
		return new Status(IStatus.OK, BeansSearchPlugin.PLUGIN_ID, 0, message,
						  null);
	}

	/**
	 * Returns <code>true</code> if given <code>IModelElement</code> matches
	 * this query. 
	 */
	protected abstract boolean doesMatch(IModelElement element,
													 IProgressMonitor monitor);

}
