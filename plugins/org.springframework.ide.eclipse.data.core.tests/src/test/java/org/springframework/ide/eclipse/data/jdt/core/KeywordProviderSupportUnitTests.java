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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
 */
@RunWith(MockitoJUnitRunner.class)
public class KeywordProviderSupportUnitTests {

	@Mock
	IType type;

	@Test
	public void addsUpKeywordsPerMapping() throws JavaModelException {

		KeywordProviderSupport provider = new KeywordProviderSupport(new TypePredicates() {
			@Override
			public boolean typeImplements(IType type, String candidateType) {
				return Arrays.asList(Object.class.getName(), String.class.getName()).contains(candidateType);
			}
		});

		Set<Type> keywords = provider.getPartTypesForPropertyOf(type);
		assertThat(keywords.containsAll(Arrays.asList(Type.SIMPLE_PROPERTY, Type.NOT_LIKE)), is(true));
	}
}
