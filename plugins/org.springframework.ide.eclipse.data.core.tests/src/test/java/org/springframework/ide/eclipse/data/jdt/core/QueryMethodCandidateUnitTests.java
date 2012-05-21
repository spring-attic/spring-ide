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

import org.junit.Test;

/**
 * Unit tests for {@link QueryMethodCandidate}.
 * 
 * @author Oliver Gierke
 */
public class QueryMethodCandidateUnitTests {

	@Test
	public void findsCompleteMatch() {

		QueryMethodCandidate candidate = new QueryMethodCandidate("findByFirstname", User.class);
		QueryMethodPart part = candidate.getPartAtPosition(6);

		assertThat(part.isCompletePathMatch(), is(false));
	}

	@Test
	public void startsAfterKeyword() {

		QueryMethodCandidate candidate = new QueryMethodCandidate("findByFirstnameAndLast", User.class);
		QueryMethodPart part = candidate.getPartAtPosition("findByFirstnameAndLa".length());

		assertThat(part.isCompletePathMatch(), is(false));
		assertThat(part.isRoot(), is(true));
		assertThat(part.getSeed(), is("la"));
	}

	@Test
	public void startsAfterSecondKeyword() {

		QueryMethodCandidate candidate = new QueryMethodCandidate("findByFirstnameAndLastnameOrFirst", User.class);
		QueryMethodPart part = candidate.getPartAtPosition("findByFirstnameAndLastnameOrFir".length());

		assertThat(part.isCompletePathMatch(), is(false));
		assertThat(part.isRoot(), is(true));
		assertThat(part.getSeed(), is("fir"));
	}

	static class User {

		String firstname;
		String lastname;
	}
}
