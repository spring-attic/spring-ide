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
