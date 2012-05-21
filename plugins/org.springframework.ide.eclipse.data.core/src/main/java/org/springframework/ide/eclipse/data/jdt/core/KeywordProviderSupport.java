/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.ide.eclipse.data.jdt.core;

import static org.springframework.data.repository.query.parser.Part.Type.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Base {@link KeywordProvider} implementation that produces some keyword proposals based on the type of the property
 * inspected.
 * 
 * @author Oliver Gierke
 */
public class KeywordProviderSupport implements KeywordProvider {

	protected static final MultiValueMap<String, Type> KEYWORDS = new LinkedMultiValueMap<String, Part.Type>();

	static {
		KEYWORDS.put(Number.class.getName(),
				Arrays.asList(GREATER_THAN, GREATER_THAN_EQUAL, BEFORE, LESS_THAN, LESS_THAN_EQUAL));
		KEYWORDS.put(String.class.getName(), Arrays.asList(LIKE, NOT_LIKE, STARTING_WITH, ENDING_WITH, CONTAINING));
		KEYWORDS.put(Date.class.getName(), Arrays.asList(BEFORE, BETWEEN, AFTER));
		KEYWORDS.put(Object.class.getName(), Arrays.asList(IS_NOT_NULL, IS_NULL, IN, NOT_IN, EXISTS, SIMPLE_PROPERTY));
		KEYWORDS.put(Boolean.class.getName(), Arrays.asList(TRUE, FALSE));
		KEYWORDS.put(Collection.class.getName(), Arrays.asList(CONTAINING));
	}

	private final TypePredicates predicates;
	private final MultiValueMap<String, Type> keywordSuperset;

	/**
	 * Creates a new {@link KeywordProviderSupport} instance using the given {@link TypePredicates} and a default set of
	 * keywords per type.
	 * 
	 * @param predicates must not be {@literal null}.
	 */
	public KeywordProviderSupport(TypePredicates predicates) {
		this(predicates, new LinkedMultiValueMap<String, Type>());
	}

	/**
	 * Creates a new {@link KeywordProviderSupport} instance using the given {@link TypePredicates} as well as the given
	 * keywords in addition to the default ones.
	 * 
	 * @param predicates must not be {@literal null}.
	 * @param additionalKeywords must not be {@literal null}.
	 */
	public KeywordProviderSupport(TypePredicates predicates, MultiValueMap<String, Type> additionalKeywords) {

		Assert.notNull(predicates);
		Assert.notNull(additionalKeywords);

		this.predicates = predicates;

		this.keywordSuperset = new LinkedMultiValueMap<String, Type>();
		this.keywordSuperset.putAll(KEYWORDS);
		this.keywordSuperset.putAll(additionalKeywords);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.data.jdt.core.KeywordProvider#getKeywordsForPropertyOf(org.eclipse.jdt.core.IType)
	 */
	public Set<String> getKeywordsForPropertyOf(IType type, String seed) {

		Set<String> result = new HashSet<String>();
		Set<String> candidates = getKeywordsForPropertyOf(type);

		if (!StringUtils.hasText(seed)) {
			return candidates;
		}

		for (String candidate : candidates) {
			if (candidate.startsWith(seed)) {
				result.add(candidate);
			}
		}

		return result;
	}

	/**
	 * Returns all keywords for the given type.
	 * 
	 * @param type
	 * @return
	 */
	private Set<String> getKeywordsForPropertyOf(IType type) {

		if (type == null) {
			return Collections.emptySet();
		}

		Set<Type> source = getPartTypesForPropertyOf(type);

		if (source.isEmpty()) {
			return Collections.emptySet();
		}

		Set<String> result = new HashSet<String>();

		for (Type partType : source) {
			result.addAll(partType.getKeywords());
		}

		return result;
	}

	/**
	 * Returns all {@link Type}s supported for the given {@link IType}. Inspects the configured keyword map and adds all
	 * keywords matching the given type.
	 * 
	 * @param type
	 * @return
	 */
	Set<Type> getPartTypesForPropertyOf(IType type) {

		if (type == null) {
			return Collections.emptySet();
		}

		Set<Type> keywords = new HashSet<Type>();

		for (String typeKey : keywordSuperset.keySet()) {
			if (type.getFullyQualifiedName().equals(typeKey) || predicates.typeImplements(type, typeKey)) {
				keywords.addAll(keywordSuperset.get(typeKey));
			}
		}

		keywords.removeAll(getUnsupportedKeywords());
		return keywords;
	}

	/**
	 * Returns all keywords not supported at all. Adds as a global filter.
	 * 
	 * @return
	 */
	protected List<Type> getUnsupportedKeywords() {
		return Collections.emptyList();
	}
}
