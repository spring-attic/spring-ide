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

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.LimitedTimeCache;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.async.FluxJdtSearch;

import reactor.core.publisher.Mono;

/**
 * Provides the algorithm for 'class-reference' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 */
public class ClassReferenceProvider extends JdtSearchingValueProvider {

	/**
	 * Default value for the 'concrete' parameter.
	 */
	private static final boolean DEFAULT_CONCRETE = true;

	private static final ClassReferenceProvider UNTARGETTED_INSTANCE = new ClassReferenceProvider(null, DEFAULT_CONCRETE);

	public static final Function<Map<String, Object>, ValueProviderStrategy> FACTORY = LimitedTimeCache.applyOn(
		Duration.ofMinutes(1),
		(params) -> {
			String target = getTarget(params);
			Boolean concrete = getConcrete(params);
			if (target!=null || concrete!=null) {
				if (concrete==null) {
					concrete = DEFAULT_CONCRETE;
				}
				return new ClassReferenceProvider(target, concrete);
			}
			return UNTARGETTED_INSTANCE;
		}
	);

	private static String getTarget(Map<String, Object> params) {
		if (params!=null) {
			Object obj = params.get("target");
			if (obj instanceof String) {
				String target = (String) obj;
				if (StringUtil.hasText(target)) {
					return target;
				}
			}
		}
		return null;
	}

	/**
	 * Filter that drops all search matches that are not concrete types.
	 */
	private Mono<StsValueHint> filterConcreteTypes(SearchMatch match) {
		Object element = match.getElement();
		if (element instanceof IType) {
			IType type = (IType) element;
			if (isAbstract(type)) {
				return Mono.empty();
			}
			return Mono.justOrEmpty(hint(type));
		}
		return Mono.empty();
	}

	private static boolean isAbstract(IType type) {
		try {
			return type.isInterface() || Flags.isAbstract(type.getFlags());
		} catch (Exception e) {
			Log.log(e);
			return false;
		}
	}

	@Override
	protected Function<SearchMatch, Mono<StsValueHint>> getPostProcessor() {
		if (concrete) {
			return this::filterConcreteTypes;
		}
		return super.getPostProcessor();
	}

	private static Boolean getConcrete(Map<String, Object> params) {
		try {
			if (params!=null) {
				Object obj = params.get("concrete");
				if (obj instanceof String) {
					String concrete = (String) obj;
					return Boolean.valueOf(concrete);
				} else if (obj instanceof Boolean) {
					return (Boolean) obj;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	/**
	 * Optional, fully qualified name of the 'target' type. Suggested hints should be a subtype of this type.
	 */
	private String target;

	/**
	 * Optional parameter, whether only concrete types should be suggested. Default value is true.
	 */
	private boolean concrete;

	private ClassReferenceProvider(String target, boolean concrete) {
		this.target = target;
		this.concrete = concrete;
	}


	@Override
	protected SearchPattern toPattern(String query) {
		String wildcardedQuery = toWildCardPattern(query);
// Beware: the commented code may seem like a good idea, but its not, because it drops 'enums' from the search results.
// So... for example org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy will not be found as a
// a concrete mongodb FieldNamingStrategy implementation!
//		if (concrete) {
//			return toClassPattern(wildcardedQuery);
//		}
		return toTypePattern(wildcardedQuery);
	}

	public IJavaSearchScope getScope(IJavaProject project) throws JavaModelException {
		if (target!=null) {
			IType type = getTargetType(project);
			if (type!=null) {
				boolean onlySubtypes = true;
				boolean includeFocusType = true;
				WorkingCopyOwner owner = null;
				return SearchEngine.createStrictHierarchyScope(project, type, onlySubtypes, includeFocusType, owner);
			}
			return null; //target type not on classpath so... can't search (and arguably if type isn't on CP
						// neither should any of its subtypes... so searching is a bit pointless).
						// scope = null will cause FluxJdtSearch to quickly return zero results.
		}
		return FluxJdtSearch.searchScope(project);
	}

	private IType getTargetType(IJavaProject project) {
		try {
			if (target!=null) {
				return project.findType(target);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
