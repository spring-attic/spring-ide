/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.search.jdt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.PatternQuerySpecification;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchResult;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanClassQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanPropertyQuery;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * {@link IQueryParticipant} implementation that hooks into JDT's Java reference
 * search and displays {@link IBean} that have the class or property name under
 * question
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansJavaSearchParticipant implements IQueryParticipant,
		IMatchPresentation {

	private static final ILabelProvider LABEL_PROVIDER = new BeansModelLabelProvider(
			true);

	private static final int S_LIMIT_REF = 2;

	private static final int S_LIMIT_ALL = 3;

	private static final int S_FOR_TYPES = 0;

	private static final int S_FOR_FIELDS = 1;

	private int searchFor = -1;

	public int estimateTicks(QuerySpecification specification) {
		return 100;
	}

	public IMatchPresentation getUIParticipant() {
		return this;
	}

	public void search(final ISearchRequestor requestor,
			QuerySpecification querySpecification, IProgressMonitor monitor) {

		if (querySpecification.getLimitTo() != S_LIMIT_REF
				&& querySpecification.getLimitTo() != S_LIMIT_ALL) {
			return;
		}

		String search = null;
		if (querySpecification instanceof ElementQuerySpecification) {
			ElementQuerySpecification elementQuerySpecification = (ElementQuerySpecification) querySpecification;
			if (elementQuerySpecification.getElement() instanceof IType) {
				search = ((IType) elementQuerySpecification.getElement())
						.getFullyQualifiedName();
			}
			else {
				search = elementQuerySpecification.getElement()
						.getElementName();
			}
			int type = ((ElementQuerySpecification) querySpecification)
					.getElement().getElementType();
			if (type == IJavaElement.TYPE) {
				searchFor = S_FOR_TYPES;
			}
			else if (type == IJavaElement.FIELD) {
				searchFor = S_FOR_FIELDS;
			}
		}
		else {
			searchFor = ((PatternQuerySpecification) querySpecification)
					.getSearchFor();
			search = ((PatternQuerySpecification) querySpecification)
					.getPattern();
		}
		ISearchQuery query = null;
		BeansSearchScope scope = BeansSearchScope.newSearchScope();
		if (searchFor == S_FOR_TYPES) {
			query = new BeanClassQuery(scope, search, true, false);
		}
		else if (searchFor == S_FOR_FIELDS) {
			query = new BeanPropertyQuery(scope, search, true, false);
		}
		if (query != null) {
			query.run(monitor);

			BeansSearchResult searchResult = (BeansSearchResult) query
					.getSearchResult();
			for (Object obj : searchResult.getElements()) {
				Match[] matches = searchResult.getMatches(obj);
				if (matches != null && matches.length > 0) {
					for (Match m : matches) {
						requestor.reportMatch(m);
					}
				}
			}
		}
	}

	public ILabelProvider createLabelProvider() {
		return LABEL_PROVIDER;
	}

	public void showMatch(Match match, int currentOffset, int currentLength,
			boolean activate) throws PartInitException {
		if (match.getElement() instanceof IResourceModelElement) {
			SpringUIUtils.openInEditor((IResourceModelElement) match
					.getElement(), activate);
		}
	}
}
