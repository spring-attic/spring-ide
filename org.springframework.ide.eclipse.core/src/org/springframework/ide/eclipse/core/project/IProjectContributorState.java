/*******************************************************************************
 * Copyright (c) 2008, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.Dictionary;

/**
 * Interface to be implemented be objects that are able to hold given objects
 * @author Christian Dupuis
 * @since 2.2.0
 */
public interface IProjectContributorState {

	/**
	 * Hand the given <code>object</code> to the state implementation.
	 * @param obj the object that should be accessible from the state
	 * @return true if the object has been successfully handed to state object; if false an object of the same type is
	 * already managed
	 */
	boolean hold(Object obj);

	/**
	 * Hand the given <code>object</code> to the state implementation.
	 * @param obj the object that should be accessible from the state
	 * @param attributes attributes that should be used to register the object; can later be used to query
	 * @return true if the object has been successfully handed to state object; if false an object of the same type is
	 * already managed
	 * @since 2.3.1
	 */
	boolean hold(Object obj, Dictionary<String, String> attibutes);

	/**
	 * Returns the managed object from the state.
	 * @param clazz the class of the managed object that is requested
	 * @return the object that is managed by the state
	 */
	<T> T get(Class<T> clazz);

	/**
	 * Returns the managed object from the state.
	 * @param clazz the class of the managed object that is requested
	 * @param filter an LDAP-style filter expression
	 * @return the object that is managed by the state
	 * @since 2.3.1
	 */
	<T> T get(Class<T> clazz, String filter);

}
