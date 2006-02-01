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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansQuery implements ISearchQuery {

	private ISearchResult result;

	public final IStatus run(IProgressMonitor monitor) {
		AbstractTextSearchResult textResult = (AbstractTextSearchResult)
															 getSearchResult();
		textResult.removeAll();
		List elements = getMatchedElements(monitor);
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			IModelElement element = (IModelElement) iter.next();
			int startLine = -1;
			int lines = -1;
			if (element instanceof ISourceModelElement) {
				ISourceModelElement sourceElement = (ISourceModelElement)
																	   element;
				startLine = sourceElement.getElementStartLine();
				lines = sourceElement.getElementEndLine() - startLine + 1;
			}
			Match match = new Match(element, Match.UNIT_LINE, startLine,
									lines);
			textResult.addMatch(match);
		}
		return new MultiStatus(BeansSearchPlugin.PLUGIN_ID, IStatus.OK,
							   "Search message", null);
	}

	/**
	 * Returns list of <code>IModelElements</code> which match this query. 
	 */
	protected abstract List getMatchedElements(IProgressMonitor monitor);

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public final ISearchResult getSearchResult() {
		if (result == null) {
			result = new BeansSearchResult(this);
		}
		return result;
	}
}
