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

import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * {@link KeywordProvider} for Spring Data MongoDB. Adds support for geo-spatial relevant types.
 * 
 * @author Oliver Gierke
 */
public class MongoDbKeywordProvider extends KeywordProviderSupport {

	private static final MultiValueMap<String, Type> KEYWORDS = new LinkedMultiValueMap<String, Type>();

	static {
		KEYWORDS.put("org.springframework.data.mongodb.core.geo.Point", Arrays.asList(NEAR));
		KEYWORDS.put("org.springframework.data.mongodb.core.geo.Shape", Arrays.asList(WITHIN));
	}

	/**
	 * Creates a new {@link MongoDbKeywordProvider} from the given {@link TypePredicates}.
	 * 
	 * @param predicates must not be {@literal null}.
	 */
	public MongoDbKeywordProvider(TypePredicates predicates) {
		super(predicates, KEYWORDS);
	}
}
