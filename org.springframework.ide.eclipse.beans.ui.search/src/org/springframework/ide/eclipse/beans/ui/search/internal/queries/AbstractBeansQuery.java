/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.search.internal.queries;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Assert;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchMessages;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchResult;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.PatternUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public abstract class AbstractBeansQuery implements ISearchQuery {

	private BeansSearchScope scope;
	private String pattern;
	private Pattern compiledPattern;
	private ISearchResult result;

	public AbstractBeansQuery(BeansSearchScope scope, String pattern,
			boolean isCaseSensitive, boolean isRegexSearch) {
		Assert.isNotNull(scope);
		this.scope = scope;
		this.pattern = pattern;
		this.compiledPattern = PatternUtils.createPattern(pattern,
				isCaseSensitive, isRegexSearch);
	}

	public BeansSearchScope getScope() {
		return scope;
	}

	public String getPattern() {
		return pattern;
	}

	public final ISearchResult getSearchResult() {
		if (result == null) {
			result = new BeansSearchResult(this);
		}
		return result;
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
		for (IModelElement element : scope.getModelElements()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IModelElementVisitor visitor = new IModelElementVisitor() {
				public boolean visit(IModelElement element,
						IProgressMonitor monitor) {
					if (doesMatch(element, compiledPattern, monitor)) {
						int startLine;
						int lines;
						if (element instanceof ISourceModelElement) {
							ISourceModelElement sourceElement =
								(ISourceModelElement) element;
							startLine = sourceElement.getElementStartLine();
							lines = sourceElement.getElementEndLine()
									- startLine + 1;
						} else {
							startLine = -1;
							lines = -1;
						}
						Match match = new Match(element, Match.UNIT_LINE,
								startLine, lines);
						result.addMatch(match);
					}
					return true;
				}
			};
			element.accept(visitor, monitor);
		}
		Object[] args = new Object[] { new Integer(result.getMatchCount()) };
		String message = MessageUtils.format(
				BeansSearchMessages.SearchQuery_status, args);
		return new Status(IStatus.OK, BeansSearchPlugin.PLUGIN_ID, 0, message,
				null);
	}

	/**
	 * Returns <code>true</code> if given {@link IModelElement} matches
	 * this query. 
	 */
	protected abstract boolean doesMatch(IModelElement element,
			Pattern pattern, IProgressMonitor monitor);
}
