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

import org.eclipse.jdt.core.IField;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.util.Assert;

/**
 * A part of a Spring Data query method, i.e. exactly the part between the last concatenator ({@literal And},
 * {@literal Or}) and the current cursor position.
 * 
 * @author Oliver Gierke
 */
class QueryMethodPart {

	private final String source;
	private PropertyPath path;
	private String seed;
	private Type type;

	/**
	 * Creates a new {@link QueryMethodPart} from the given source and entity type.
	 * 
	 * @param source must not be {@literal null}.
	 * @param domainType must not be {@literal null}.
	 */
	public QueryMethodPart(String source, Class<?> domainType) {

		this.source = source;

		try {

			Part part = new Part(source, domainType);
			this.path = part.getProperty();
			this.seed = null;
			this.type = part.getType();

		} catch (PropertyReferenceException e) {

			this.path = e.getBaseProperty();
			this.seed = e.getPropertyName();
			this.type = null;
		}
	}

	/**
	 * Returns whether the part is a complete match, i.e. the part can be resolved into a {@link Part} entirely.
	 * 
	 * @return
	 */
	public boolean isCompletePathMatch() {
		return path != null && seed == null;
	}

	/**
	 * Return whether the {@link QueryMethodPart} is referencing the root entity. This means no property reference could
	 * be resolved on top of the configured domain type.
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return path == null;
	}

	/**
	 * Returns whether the part is a complete match including a dedicated keyword.
	 * 
	 * @return
	 */
	public boolean isKeywordComplete() {

		if (!isCompletePathMatch()) {
			return false;
		}

		if (!type.equals(Type.SIMPLE_PROPERTY)) {
			return true;
		}

		for (String keyword : Type.SIMPLE_PROPERTY.getKeywords()) {
			if (source.endsWith(keyword)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the leaf property of the query method part or the one of the base property in case the part is a partial
	 * match only.
	 * 
	 * @return
	 */
	public PropertyPath getPathLeaf() {
		return this.path == null ? null : this.path.getLeafProperty();
	}

	/**
	 * Returns the seed to hint into the next property or keyword.
	 * 
	 * @return the seed to hint into the next property or keyword or {@literal null} if none available.
	 */
	public String getSeed() {
		return seed;
	}

	/**
	 * Returns the explicit keyword a {@link QueryMethodPart} ends with.
	 * 
	 * @return the explicit keyword a {@link QueryMethodPart} ends with or {@literal null} if no explicit keyword is used.
	 */
	public String getKeyword() {

		if (!isCompletePathMatch()) {
			return null;
		}

		PropertyPath current = path;
		int index = path.getSegment().length();

		while (current.hasNext()) {
			current = current.next();
			index += current.getSegment().length();
		}

		return source.length() == index ? null : source.substring(index);
	}

	/**
	 * Returns whether the given {@link IField} is a proposal candidate.
	 * 
	 * @param field must not be {@literal null}.
	 * @return
	 */
	public boolean isProposalCandidate(IField field) {

		Assert.notNull(field);
		return seed == null ? true : field.getElementName().startsWith(seed);
	}
}