/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.processors;

import java.util.Comparator;

import org.eclipse.jdt.internal.ui.text.correction.NameMatcher;

/**
 * String comparator for sorting a list of suggested names. Sort by most
 * relevant to least relevant.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NameSuggestionComparator implements Comparator<String> {

	private final String toMatch;

	public NameSuggestionComparator(String toMatch) {
		this.toMatch = toMatch;
	}

	public int compare(String fullName1, String fullName2) {
		return getSimilarity(fullName1) - getSimilarity(fullName2);
	}

	private int getSimilarity(String fullName) {
		String name;
		int pos = fullName.lastIndexOf(".");
		if (pos < 0) {
			name = fullName;
		}
		else {
			name = fullName.substring(pos + 1);
		}

		if (name.equals(toMatch)) {
			return 300;
		}
		return NameMatcher.getSimilarity(toMatch, name);
	}

}
