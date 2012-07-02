/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
