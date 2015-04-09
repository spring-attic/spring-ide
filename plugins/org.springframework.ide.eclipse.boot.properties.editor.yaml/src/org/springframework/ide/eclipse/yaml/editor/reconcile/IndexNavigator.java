/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor.reconcile;

import static org.springframework.ide.eclipse.boot.util.StringUtil.hasText;

import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;

/**
 * An index navigator allows selecting subset of a property index as if
 * navigating the index by selecting on a property
 *
 * @author Kris De Volder
 */
public class IndexNavigator {

	//Possible opitmization: we could cache prefix match candidate and extended match candidate
	// since it is assumed that the index is immutable for the lifetime of
	// the index navigator.

	private static final char NAV_CHAR = '.';

	/**
	 * Property access in this navigator are interpreted relative
	 * to this prefix
	 */
	private String prefix = null;
	private FuzzyMap<PropertyInfo> index;

	private IndexNavigator(FuzzyMap<PropertyInfo> index) {
		this.index = index;
	}

	private IndexNavigator(FuzzyMap<PropertyInfo> index, String prefix) {
		this.index = index;
		this.prefix = prefix;
	}

	public static IndexNavigator with(FuzzyMap<PropertyInfo> index) {
		return new IndexNavigator(index);
	}

	public IndexNavigator selectSubProperty(String name) {
		return new IndexNavigator(index, join(prefix, name));
	}

	protected String join(String prefix, String postfix) {
		if (!hasText(prefix)) {
			return postfix;
		} else {
			return prefix + NAV_CHAR + postfix;
		}
	}

	/**
	 * @return property info that is an exact match with the current prefix or
	 * null if there's no exact match
	 */
	public PropertyInfo getExactMatch() {
		if (prefix!=null) {
			PropertyInfo candidate = index.findLongestCommonPrefixEntry(prefix);
			if (candidate.getId().equals(prefix)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Get a property that has the current prefix as a 'true' prefix. A true prefix
	 * is a String that has the current prefix as a prefix and continues onward with
	 * a navigation operation.
	 */
	public PropertyInfo getExtensionCandidate() {
		//If current prefix is null then all entries in the index are candidates since
		// the index is at the 'root' of the tree and we don't need a '.' to navigate
		String extendedPrefix = prefix==null?"":prefix + NAV_CHAR;
		PropertyInfo candidate = index.findLongestCommonPrefixEntry(extendedPrefix);
		if (candidate.getId().startsWith(extendedPrefix)) {
			return candidate;
		}
		return null;
	}

	public String getPrefix() {
		return prefix;
	}

}
