/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
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
import java.util.Hashtable;
import java.util.Set;

/**
 * Simple generic holder for collections to be used with {@link IProjectContributorState}.
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class CollectionState<T> {

	private static final String COLLECTION_CLASS_KEY = "collectionClass";

	private final Set<T> resources;

	public CollectionState(Set<T> resources) {
		this.resources = resources;
	}

	public Set<T> get() {
		return resources;
	}

	public static Dictionary<String, String> getAttributes(Class clazz) {
		Dictionary<String, String> attributes = new Hashtable<String, String>();
		attributes.put(COLLECTION_CLASS_KEY, clazz.getName());
		return attributes;
	}

	public static String getFilter(Class clazz) {
		return new StringBuilder().append("(").append(COLLECTION_CLASS_KEY).append("=").append(clazz.getName()).append(
				")").toString();
	}
}
