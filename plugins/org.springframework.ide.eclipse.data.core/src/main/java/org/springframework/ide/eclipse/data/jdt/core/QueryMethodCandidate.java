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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * A candidate for a Spring Data query method. Allows to lookup {@link QueryMethodPart}s at a given position inside the
 * method name.
 * 
 * @author Oliver Gierke
 */
class QueryMethodCandidate {

	private final Pattern pattern = Pattern.compile("^(find|read|get)(\\p{Upper}.*?)??By");
	private final Pattern CONCATENATOR_PATERN = Pattern.compile(".*(And|Or)(?=\\p{Upper}.*?)?");

	private final String name;
	private final int prefixLength;
	private final Class<?> entityClass;

	/**
	 * Creates a new {@link QueryMethodCandidate} with the given name based on the given entity type.
	 * 
	 * @param name must not be {@literal null}. or empty.
	 * @param entityType must not be {@literal null}.
	 */
	public QueryMethodCandidate(String name, Class<?> entityType) {

		Assert.hasText(name, "Method name must not be null or empty!");
		Assert.notNull(entityType, "Entity type must not be null!");

		Matcher matcher = pattern.matcher(name);
		boolean prefixFound = matcher.find();

		this.prefixLength = prefixFound ? matcher.end() : 0;
		this.name = prefixFound ? name.substring(matcher.end()) : name;
		this.entityClass = entityType;
	}

	/**
	 * Returns the {@link QueryMethodPart} for the given cursor position within the method name. Will inspect the name for
	 * concatenators ({@literal And}, {@literal Or}) and inspect the part between the concatenator and the cursor
	 * position.
	 * 
	 * @param i the current position within the method name.
	 * @return
	 */
	public QueryMethodPart getPartAtPosition(int i) {

		Matcher matcher = CONCATENATOR_PATERN.matcher(name);
		int startIndex = matcher.find() ? matcher.end() : 0;
		int endIndex = i - prefixLength > name.length() ? name.length() : i - prefixLength;

		String subName = startIndex >= name.length() ? "" : name.substring(startIndex, endIndex);
		return new QueryMethodPart(subName, entityClass);
	}
}
