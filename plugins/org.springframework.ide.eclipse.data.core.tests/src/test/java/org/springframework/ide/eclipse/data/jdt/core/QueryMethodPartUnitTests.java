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
