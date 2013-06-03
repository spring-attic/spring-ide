/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class TypeHierarchyElementCache {
	
	private Map<Integer, TypeHierarchyElement> cache;
	
	public TypeHierarchyElementCache() {
		this.cache = new ConcurrentHashMap<Integer, TypeHierarchyElement>();
	}

	public TypeHierarchyElement get(char[] fullyQualifiedClassName) {
		return cache.get(charArrayToHashcode(fullyQualifiedClassName));
	}

	public void put(char[] fullyQualifiedClassName, TypeHierarchyElement typeElement) {
		cache.put(charArrayToHashcode(fullyQualifiedClassName), typeElement);
	}
	
	private int charArrayToHashcode(char[] characters) {
		int h = 0;
		for (int i = 0; i < characters.length; i++) {
			h = 31*h + characters[i];
		}
        return h;
	}

}
