/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.search.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchLabelProvider;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchResult;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanClassQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanPropertyQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.util.StringUtils;

/**
 * {@link IQueryParticipant} implementation that hooks into JDT's Java reference search and displays {@link IBean} that
 * have the class or property name under question
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansJavaSearchParticipant implements IQueryParticipant, IMatchPresentation {

	private static final int SEARCH_FOR_TYPES = 0;

	private static final int SEARCH_FOR_FIELDS = 1;

	private static final int LIMIT_TO_REF = 2;

	private static final int LIMIT_TO_ALL = 3;

	private int searchFor = -1;

	public int estimateTicks(QuerySpecification specification) {
		return 100;
	}

	public IMatchPresentation getUIParticipant() {
		return this;
	}

	public void search(final ISearchRequestor requestor, QuerySpecification querySpecification, IProgressMonitor monitor) {

		if (querySpecification.getLimitTo() != LIMIT_TO_REF && querySpecification.getLimitTo() != LIMIT_TO_ALL) {
			return;
		}

		String search = null;
		List<String> requiredTypeNames = new ArrayList<String>();
		IJavaProject project = null;

		if (querySpecification instanceof ElementQuerySpecification) {
			ElementQuerySpecification elementQuerySpecification = (ElementQuerySpecification) querySpecification;
			if (elementQuerySpecification.getElement() instanceof IType) {
				search = ((IType) elementQuerySpecification.getElement()).getFullyQualifiedName();
				project = ((IType) elementQuerySpecification.getElement()).getJavaProject();
			}
			else if (elementQuerySpecification.getElement() instanceof IField) {
				IField field = ((IField) elementQuerySpecification.getElement());
				search = field.getElementName();
				getTypeHierachy(monitor, requiredTypeNames, field.getDeclaringType());
				project = field.getJavaProject();
			}
			else if (elementQuerySpecification.getElement() instanceof IMethod) {
				IMethod method = ((IMethod) elementQuerySpecification.getElement());
				search = method.getElementName();
				// do property name magic
				if (search.startsWith("set")) {
					search = StringUtils.uncapitalize(search.substring(3));
				}
				getTypeHierachy(monitor, requiredTypeNames, method.getDeclaringType());
				project = method.getJavaProject();
			}
			else {
				search = elementQuerySpecification.getElement().getElementName();
			}

			int type = ((ElementQuerySpecification) querySpecification).getElement().getElementType();
			if (type == IJavaElement.TYPE) {
				searchFor = SEARCH_FOR_TYPES;
			}
			else if (type == IJavaElement.FIELD || type == IJavaElement.METHOD) {
				searchFor = SEARCH_FOR_FIELDS;
			}
		}
		else {
			searchFor = ((PatternQuerySpecification) querySpecification).getSearchFor();
			search = ((PatternQuerySpecification) querySpecification).getPattern();
		}

		List<ISearchQuery> queries = new ArrayList<ISearchQuery>();
		BeansSearchScope scope = BeansSearchScope.newSearchScope();
		if (searchFor == SEARCH_FOR_TYPES) {
			queries.add(new BeanClassQuery(scope, search, true, false));
		}
		else if (searchFor == SEARCH_FOR_FIELDS) {
			queries.add(new BeanPropertyQuery(scope, search, true, false));
			queries.add(new BeanReferenceQuery(scope, search, true, false));
		}

		for (ISearchQuery query : queries) {
			query.run(monitor);

			BeansSearchResult searchResult = (BeansSearchResult) query.getSearchResult();
			for (Object obj : searchResult.getElements()) {
				Match[] matches = searchResult.getMatches(obj);
				if (matches != null && matches.length > 0) {
					for (Match match : matches) {
						if (match.getElement() instanceof IBean) {
							IBean bean = (IBean) match.getElement();
							IType type = JdtUtils.getJavaType(bean.getElementResource().getProject(), bean
									.getClassName());
							if (project == null || (type != null && project.isOnClasspath(type))) {
								if (searchFor == SEARCH_FOR_FIELDS) {
									// check if the match fits to the selected class
									String beanClass = BeansModelUtils.getBeanClass(bean, null);
									if (requiredTypeNames.contains(beanClass)) {
										requestor.reportMatch(match);
									}
								}
								else {
									requestor.reportMatch(match);
								}
							}
						}
						else {
							requestor.reportMatch(match);
						}
					}
				}
			}
		}
	}

	private void getTypeHierachy(IProgressMonitor monitor, List<String> requiredTypeNames, IType baseType) {
		try {
			IType[] types = baseType.newTypeHierarchy(monitor).getAllSubtypes(baseType);
			requiredTypeNames.add(baseType.getFullyQualifiedName());
			for (IType type : types) {
				requiredTypeNames.add(type.getFullyQualifiedName());
			}
		}
		catch (JavaModelException e) {
		}
	}

	public ILabelProvider createLabelProvider() {

		// This label provider will be disposed by the search page
		return new BeansSearchLabelProvider(true);
	}

	public void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		if (match.getElement() instanceof IResourceModelElement) {
			BeansUIUtils.openInEditor((IResourceModelElement) match.getElement(), activate);
		}
	}
}
