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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * The filter for searching for tags.
 *
 * @author Alex Boyko
 *
 */
public class TagSearchFilter implements Filter<BootDashElement> {

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
	public boolean accept(BootDashElement element) {
		if (searchTags.length == 0 && searchTerm.isEmpty()) {
			return true;
		}
		LinkedHashSet<String> elementTags = ((Taggable) element).getTags();
		int initSize = elementTags.size();
		elementTags.removeAll(Arrays.asList(searchTags));
		// Check if all search tags are present in the element tags set
		if (elementTags.size() == initSize - searchTags.length) {
			if (searchTerm.isEmpty()) {
				return true;
			} else {
				for (String elementTag : elementTags) {
					if (elementTag.contains(searchTerm)) {
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

}
