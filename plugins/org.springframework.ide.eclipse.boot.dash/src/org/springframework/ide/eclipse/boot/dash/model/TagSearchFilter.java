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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springsource.ide.eclipse.commons.livexp.util.Filter;

/**
 * The filter for searching for tags.
 *
 * @author Alex Boyko
 *
 */
public class  TagSearchFilter<T extends Taggable> implements Filter<T> {

	private String searchTerm;

	private String[] searchTags;

	public TagSearchFilter() {
		this(null, null);
	}

	public TagSearchFilter(String[] searchTags, String searchTerm) {
		this.searchTags = searchTags == null ? new String[0] : searchTags;
		this.searchTerm = searchTerm == null ? "" : searchTerm;
	}

	public TagSearchFilter(String s) {
		this();
		if (!s.isEmpty()) {
			String[] splitSearchStr = TagUtils.parseTags(s);
			if (splitSearchStr.length > 0) {
				if (Pattern.matches("(.+)" + TagUtils.SEPARATOR_REGEX, s)) {
					this.searchTags = splitSearchStr;
				} else {
					this.searchTags = Arrays.copyOfRange(splitSearchStr, 0, splitSearchStr.length - 1);
					this.searchTerm = splitSearchStr[splitSearchStr.length - 1];
				}
			}
		}
	}

	@Override
	public boolean accept(T element) {
		if (searchTags.length == 0 && searchTerm.isEmpty()) {
			return true;
		}

		List<String> searchTags_caseIndependent = new ArrayList<>(searchTags.length);
		for (String searchTag : searchTags) {
			searchTags_caseIndependent.add(searchTag.toLowerCase());
		}

		String searchTerm_caseIndependent = searchTerm.toLowerCase();

		Set<String> elementTags = getTags(element);
		HashSet<String> tags_caseIndependent = new HashSet<>();
		for (String tag : elementTags) {
			tags_caseIndependent.add(tag.toLowerCase());
		}

		int initSize = tags_caseIndependent.size();
		tags_caseIndependent.removeAll(searchTags_caseIndependent);
		// Check if all search tags are present in the element tags set
		if (tags_caseIndependent.size() == initSize - searchTags_caseIndependent.size()) {
			if (searchTerm_caseIndependent.isEmpty()) {
				return true;
			} else {
				for (String elementTag : tags_caseIndependent) {
					if (elementTag.contains(searchTerm_caseIndependent)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String initSearchText = TagUtils.toString(searchTags);
		if (!searchTerm.isEmpty()) {
			initSearchText += TagUtils.SEPARATOR + searchTerm;
		}
		return initSearchText;
	}

	protected Set<String> getTags(T element) {
		return element.getTags();
	}

}
