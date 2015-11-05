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

package org.eclipse.gemini.blueprint.blueprint.container.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * Registrar holding the specific Blueprint editors. This class is used by the Spring DM extender for all Blueprint
 * contexts.
 * 
 * @author Costin Leau
 */
public class BlueprintEditorRegistrar implements PropertyEditorRegistrar {

	/**
	 * Class that changes the default Spring implementation for 'collection'. Spring uses a LinkedHashSet but Blueprint
	 * mandates an ArrayList.
	 * 
	 * @author Costin Leau
	 */
	private static class BlueprintCustomCollectionEditor extends CustomCollectionEditor {

		public BlueprintCustomCollectionEditor(Class<? extends Collection> collectionType) {
			super(collectionType);
		}

		@Override
		public void setValue(Object value) {
			if (value != null && (!(value instanceof Collection)) && (!value.getClass().isArray())) {
				throw new IllegalArgumentException("Cannot create collection from type " + value.getClass()
						+ " of instance " + value);
			}
			super.setValue(value);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Collection createCollection(Class collectionType, int initialCapacity) {
			if (!collectionType.isInterface()) {
				try {
					return (Collection) collectionType.newInstance();
				} catch (Exception ex) {
					throw new IllegalArgumentException("Could not instantiate collection class ["
							+ collectionType.getName() + "]: " + ex.getMessage());
				}
			} else if (List.class.equals(collectionType)) {
				return new ArrayList(initialCapacity);
			} else if (Set.class.equals(collectionType)) {
				return new LinkedHashSet(initialCapacity);
			} else if (SortedSet.class.equals(collectionType)) {
				return new TreeSet();
			} else {
				return new ArrayList(initialCapacity);
			}
		}
	};

	public void registerCustomEditors(PropertyEditorRegistry registry) {
		// Date
		registry.registerCustomEditor(Date.class, new DateEditor());
		// Collection concrete types
		registry.registerCustomEditor(Stack.class, new BlueprintCustomCollectionEditor(Stack.class));
		registry.registerCustomEditor(Vector.class, new BlueprintCustomCollectionEditor(Vector.class));

		// Spring creates a LinkedHashSet for Collection, RFC mandates an ArrayList
		// reinitialize default editors
		registry.registerCustomEditor(Collection.class, new BlueprintCustomCollectionEditor(Collection.class));
		registry.registerCustomEditor(Set.class, new BlueprintCustomCollectionEditor(Set.class));
		registry.registerCustomEditor(SortedSet.class, new BlueprintCustomCollectionEditor(SortedSet.class));
		registry.registerCustomEditor(List.class, new BlueprintCustomCollectionEditor(List.class));
		registry.registerCustomEditor(SortedMap.class, new CustomMapEditor(SortedMap.class));

		registry.registerCustomEditor(HashSet.class, new BlueprintCustomCollectionEditor(HashSet.class));
		registry.registerCustomEditor(LinkedHashSet.class, new BlueprintCustomCollectionEditor(LinkedHashSet.class));
		registry.registerCustomEditor(TreeSet.class, new BlueprintCustomCollectionEditor(TreeSet.class));

		registry.registerCustomEditor(ArrayList.class, new BlueprintCustomCollectionEditor(ArrayList.class));
		registry.registerCustomEditor(LinkedList.class, new BlueprintCustomCollectionEditor(LinkedList.class));

		// Map concrete types
		registry.registerCustomEditor(HashMap.class, new CustomMapEditor(HashMap.class));
		registry.registerCustomEditor(LinkedHashMap.class, new CustomMapEditor(LinkedHashMap.class));
		registry.registerCustomEditor(Hashtable.class, new CustomMapEditor(Hashtable.class));
		registry.registerCustomEditor(TreeMap.class, new CustomMapEditor(TreeMap.class));
		registry.registerCustomEditor(Properties.class, new PropertiesEditor());

		// JDK 5 types
		registry.registerCustomEditor(ConcurrentMap.class, new CustomMapEditor(ConcurrentHashMap.class));
		registry.registerCustomEditor(ConcurrentHashMap.class, new CustomMapEditor(ConcurrentHashMap.class));
		registry.registerCustomEditor(Queue.class, new BlueprintCustomCollectionEditor(LinkedList.class));

		// Legacy types
		registry.registerCustomEditor(Dictionary.class, new CustomMapEditor(Hashtable.class));
	}
}