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
import java.util.List;

import org.springframework.data.repository.query.parser.Part.Type;

/**
 * Keyword provider for Spring Data JPA. Excludes {@literal Near} and {@literal Within} keywords from the list of
 * proposals.
 * 
 * @author Oliver Gierke
 */
public class JpaKeywordProvider extends KeywordProviderSupport {

	/**
	 * Creates a new {@link JpaKeywordProvider} for the given {@link TypePredicates}.
	 * 
	 * @param predicates must not be {@literal null}.
	 */
	public JpaKeywordProvider(TypePredicates predicates) {
		super(predicates);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.data.jdt.core.KeywordProviderSupport#getUnsupportedKeywords()
	 */
	@Override
	protected List<Type> getUnsupportedKeywords() {
		return Arrays.asList(NEAR, WITHIN);
	}
}
