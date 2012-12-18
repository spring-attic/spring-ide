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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.repository.query.parser.Part.Type;

/**
 * Unit tests for {@link KeywordProviderSupport}.
 * 
 * @author Oliver Gierke
 * @author Tomasz Zarna
 */
@RunWith(MockitoJUnitRunner.class)
public class KeywordProviderSupportUnitTests {

	@Mock
	IType type;

	@Test
	public void addsUpKeywordsPerMapping() throws JavaModelException {

		KeywordProviderSupport provider = new KeywordProviderSupport(new TypePredicates() {
			public boolean typeImplements(IType type, String candidateType) {
				return Arrays.asList(Object.class.getName(), String.class.getName()).contains(candidateType);
			}
		});

		Set<Type> keywords = provider.getPartTypesForPropertyOf(type);
		assertThat(keywords.containsAll(Arrays.asList(Type.SIMPLE_PROPERTY, Type.NOT_LIKE)), is(true));
	}
}
