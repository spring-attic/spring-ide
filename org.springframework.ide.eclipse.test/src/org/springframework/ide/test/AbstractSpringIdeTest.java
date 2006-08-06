/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.ide.test;

import junit.framework.TestCase;

/**
 * Base class for the tests. Defines generic setUp and TearDown methods.
 * 
 * @author Loren Rosen
 */
public class AbstractSpringIdeTest extends TestCase {

	protected TestProject project;

	public AbstractSpringIdeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		project = new TestProject();
	}

	protected void tearDown() throws Exception {
		project.dispose();
	}
}
