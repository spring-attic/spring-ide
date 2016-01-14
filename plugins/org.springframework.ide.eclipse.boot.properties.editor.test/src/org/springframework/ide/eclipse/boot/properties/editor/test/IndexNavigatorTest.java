/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile.IndexNavigator;

public class IndexNavigatorTest extends YamlEditorTestHarness {

	public void testSimple() throws Exception {
		defaultTestData();

		start();
		assertContinuable();

		navigate("server");
		assertContinuable();

		navigate("port");
		assertProperty();

		navigate("extracrap");
		assertEmpty();
	}

	public void testPartialName() throws Exception {
		defaultTestData();

		start();
		assertContinuable();

		navigate("serv");
		// As a plain string 'serv' is a prefix of 'server'
		// but it shouldn't be treated as such since it doesn't continue with
		// a '.'
		assertEmpty();
	}

	public void testAmbiguous() throws Exception {
		data("foo.bar", "java.lang.String", null, "Foo dot bar");
		data("foo", "java.lang.String", null, "Just foo");
		data("fooaaaa", "java.lang.String", null, "Confuse the foo match");

		start();
		navigate("foo");
		assertAmbiguous();
	}

	/////////////// test harnes /////////////////////////////////////

	/**
	 * Assert that current navigation state is unambiguous and allows
	 * further navigation. I.e. the current navigation state does not
	 * represent an exact property match but contains valid sub-properties
	 */
	public void assertContinuable() {
		assertNull(navigator.getExactMatch());
		assertNotNull(navigator.getExtensionCandidate());
	}

	/**
	 * Assert that current navigation state is unambiguous and represents
	 * an exact property match.
	 */
	public void assertProperty() {
		assertNotNull(navigator.getExactMatch());
		assertNull(navigator.getExtensionCandidate());
	}

	/**
	 * Assert that current navigation state is both an exact property match
	 * (so navigation could end here) and also contains valid subproperties
	 * (so navigation could continue).
	 */
	public void assertAmbiguous() {
		assertNotNull(navigator.getExactMatch());
		assertNotNull(navigator.getExtensionCandidate());
	}

	/**
	 * Assert that the current navigation state is neither an exact property
	 * match nor continuable.
	 */
	public void assertEmpty() {
		assertNull(navigator.getExactMatch());
		assertNull(navigator.getExtensionCandidate());
	}

	/**
	 * Current navigation state
	 */
	public IndexNavigator navigator;

	/**
	 * Reset navigation state to point at the root of the index.
	 */
	public void start() {
		navigator = IndexNavigator.with(index);
	}

	/**
	 * Navigate from current navigation 'root' to a sub property
	 */
	public void navigate(String propName) {
		navigator = navigator.selectSubProperty(propName);
	}

}
