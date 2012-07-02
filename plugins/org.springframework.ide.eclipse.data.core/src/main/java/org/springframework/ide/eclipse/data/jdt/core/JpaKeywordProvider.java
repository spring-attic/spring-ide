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
