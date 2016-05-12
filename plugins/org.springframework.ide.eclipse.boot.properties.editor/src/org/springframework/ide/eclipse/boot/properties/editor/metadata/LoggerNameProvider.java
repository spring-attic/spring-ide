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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderFactory;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.FluxJdtSearch;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides the algorithm for 'logger-name' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 */
public class LoggerNameProvider extends CachingValueProvider {

	private  static final ValueProviderStrategy INSTANCE = new LoggerNameProvider();
	public static final ValueProviderFactory FACTORY = (params) -> INSTANCE;

	@Override
	public Flux<ValueHint> getValuesAsycn(IJavaProject javaProject, String query) {
		try {
			return new FluxJdtSearch()
				.scope(javaProject)
				.pattern(toPattern(query))
				.search()
				.flatMap((r) -> getFQName(r))
				.filter((fqName) -> 0!=FuzzyMatcher.matchScore(query, fqName))
				.distinct()
				.map((fqName) -> {
//					debug("distinct["+query+"]: "+fqName);
					return hint(fqName);
				});
		} catch (Exception e) {
			return Flux.error(e);
		}
	}

	public static Mono<String> getFQName(SearchMatch match) {
		Object element = match.getElement();
		if (element instanceof IType) {
			IType type = (IType) element;
			return Mono.justOrEmpty(type.getFullyQualifiedName());
		} else if (element instanceof IPackageFragment) {
			IPackageFragment pkg = (IPackageFragment) element;
			return Mono.justOrEmpty(pkg.getElementName());
		}
		return Mono.empty();
	}

	private static String toWildCardPattern(String query) {
		StringBuilder builder = new StringBuilder("*");
		for (char c : query.toCharArray()) {
			builder.append(c);
			builder.append('*');
		}
		return builder.toString();
	}

	public static SearchPattern toPattern(String query) {
		String wildCardedQuery = toWildCardPattern(query);
		return SearchPattern.createOrPattern(
				toTypePattern(wildCardedQuery),
				toPackagePattern(wildCardedQuery)
		);
	}

	private static SearchPattern toPackagePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.PACKAGE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	private static SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	private static ValueHint hint(String fqName) {
		ValueHint h = new ValueHint();
		h.setValue(fqName);
		return h;
	}

}
