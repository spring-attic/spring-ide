/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Enumeration-like class which indicates the supported Spring DM managed OSGi
 * service collection types. This class is used mainly for configuration
 * purposes (such as parsing the OSGi namespace).
 * 
 * @author Costin Leau
 */
public enum CollectionType {
//
//	private static final long serialVersionUID = 6165756098619186329L;
	/**
	 * Spring-managed list. The returned collection with implement the
	 * {@link List} interface.
	 * 
	 * @see java.util.List
	 */
	LIST(2, "LIST", List.class),
	/**
	 * Spring-managed set. The returned collection with implement the
	 * {@link Set} interface.
	 * 
	 * @see java.util.Set
	 */
	SET(3, "SET", Set.class),
	/**
	 * Spring-managed sorted list. The returned collection with implement the
	 * {@link List} interface.
	 * 
	 * @see java.lang.Comparable
	 * @see java.util.Comparator
	 * @see java.util.List
	 * @see java.util.SortedSet
	 */
	SORTED_LIST(4, "SORTED_LIST", List.class),
	/**
	 * Spring-managed sorted Set. The returned collection with implement the
	 * {@link SortedSet} interface.
	 * 
	 * @see java.lang.Comparable
	 * @see java.util.Comparator
	 * @see java.util.SortedSet
	 */
	SORTED_SET(5, "SORTED_SET", SortedSet.class);

	/** collection type */
	private final Class<?> collectionClass;


	/**
	 * Returns the actual collection class used underneath.
	 * 
	 * @return collection class
	 */
	public Class<?> getCollectionClass() {
		return collectionClass;
	}

	private CollectionType(int code, String label, Class<?> collectionClass) {
		this.collectionClass = collectionClass;
	}
}