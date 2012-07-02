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
 * 
 * @author Oliver Gierke
 */
public class QueryMethodPartUnitTests {

	@Test
	public void detectsStart() {

		QueryMethodPart part = new QueryMethodPart("", Entity.class);
		assertThat(part.isCompletePathMatch(), is(false));
		assertThat(part.isRoot(), is(true));
		assertThat(part.getPathLeaf(), is(nullValue()));
	}

	@Test
	public void detectsRootSeed() {

		QueryMethodPart part = new QueryMethodPart("Us", Entity.class);
		assertThat(part.isCompletePathMatch(), is(false));
		assertThat(part.isRoot(), is(true));
		assertThat(part.getPathLeaf(), is(nullValue()));
		assertThat(part.getSeed(), is("us"));
	}

	@Test
	public void detectsKeyWordComplete() {

		// Invalid property reference
		QueryMethodPart part = new QueryMethodPart("UsernameLike", Entity.class);
		assertThat(part.isKeywordComplete(), is(false));
		assertThat(part.getKeyword(), is(nullValue()));

		// Partial property reference
		part = new QueryMethodPart("Us", Entity.class);
		assertThat(part.isKeywordComplete(), is(false));
		assertThat(part.getKeyword(), is(nullValue()));

		// Match with special keyword
		part = new QueryMethodPart("UserFirstnameLike", Entity.class);
		assertThat(part.isKeywordComplete(), is(true));
		assertThat(part.getKeyword(), is("Like"));

		// Match with simple keyword
		part = new QueryMethodPart("UserFirstnameIs", Entity.class);
		assertThat(part.isKeywordComplete(), is(true));
		assertThat(part.getKeyword(), is("Is"));

		// Match without keyword
		part = new QueryMethodPart("UserFirstname", Entity.class);
		assertThat(part.isKeywordComplete(), is(false));
		assertThat(part.getKeyword(), is(nullValue()));
	}

	@Test
	public void foo() {

		QueryMethodPart wrapper = new QueryMethodPart("User", Entity.class);
		assertThat(wrapper.isCompletePathMatch(), is(true));

		wrapper = new QueryMethodPart("UserFirstname", Entity.class);
		assertThat(wrapper.isCompletePathMatch(), is(true));
	}

	@Test
	public void detectsPartialMatch() {
		QueryMethodPart wrapper = new QueryMethodPart("UserFirst", Entity.class);
		assertThat(wrapper.isCompletePathMatch(), is(false));
		assertThat(wrapper.getPathLeaf(), is(notNullValue()));
		assertThat(wrapper.getSeed(), is("first"));
	}

	@Test
	public void detectsKeywordMatch() {

		QueryMethodPart part = new QueryMethodPart("FirstnameLike", User.class);
		assertThat(part.isCompletePathMatch(), is(true));
	}

	class Entity {
		User user;
	}

	class User {

		String firstname;
	}
}
