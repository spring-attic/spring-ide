/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import java.util.function.Function;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.FluxJdtSearch;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract implementation of {@link ValueProviderStrategy} based on a JDTSearch.
 * <p>
 * All a subclass must provide is the means to create a {@link SearchPattern} and, optionally a
 * {@link IJavaSearchScope}.
 *
 * @author Kris De Volder
 */
public abstract class JdtSearchingValueProvider extends CachingValueProvider {

	public static String toWildCardPattern(String query) {
		StringBuilder builder = new StringBuilder("*");
		for (char c : query.toCharArray()) {
			builder.append(c);
			builder.append('*');
		}
		return builder.toString();
	}

	public static SearchPattern toPackagePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.PACKAGE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toClassPattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.CLASS;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	protected String asStringValue(IJavaElement element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			return type.getFullyQualifiedName();
		} else if (element instanceof IPackageFragment) {
			IPackageFragment pkg = (IPackageFragment) element;
			return pkg.getElementName();
		}
		return null;
	}

	protected StsValueHint hint(IJavaElement e) {
		String fqName = asStringValue(e);
		return fqName==null?null:StsValueHint.create(fqName, e);
	}

	protected abstract SearchPattern toPattern(String query);
	protected IJavaSearchScope getScope(IJavaProject javaProject) throws JavaModelException {
		return FluxJdtSearch.searchScope(javaProject);
	}

	@Override
	public Flux<StsValueHint> getValuesAsycn(IJavaProject javaProject, String query) {
		try {
			return new FluxJdtSearch()
			.scope(getScope(javaProject))
			.pattern(toPattern(query))
			.search()
			.flatMap(getPostProcessor())
			.filter((hint) ->  0!=FuzzyMatcher.matchScore(query, hint.getValue()))
			.distinct(StsValueHint::getValue);
		} catch (Exception e) {
			return Flux.error(e);
		}
	}

	protected Function<SearchMatch, Mono<StsValueHint>> getPostProcessor() {
		return (match) -> {
			Object e = match.getElement();
			if (e instanceof IJavaElement) {
				return Mono.justOrEmpty(hint((IJavaElement)e));
			}
			return Mono.empty();
		};
	}
}
