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
package org.springframework.ide.eclipse.metadata.ui;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.springframework.util.AntPathMatcher;

/**
 * @author Leo Dos Santos
 */
public class RequestMappingViewSorter extends ViewerSorter {

	private RequestMappingViewLabelProvider labelProvider;

	private AntPathMatcher matcher;

	private int sortColumn;

	private int sortDirection;

	public RequestMappingViewSorter(
			RequestMappingViewLabelProvider labelProvider) {
		matcher = new AntPathMatcher();
		this.labelProvider = labelProvider;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int result = compareColumn(viewer, e1, e2);
		if (sortDirection == SWT.UP) {
			return result;
		}
		return result * -1;
	}

	public void setSortColumn(int column) {
		sortColumn = column;
	}

	public void setSortDirection(int direction) {
		sortDirection = direction;
	}

	private int compareColumn(Viewer viewer, Object e1, Object e2) {
		if (sortColumn == RequestMappingView.COLUMN_URL) {
			return sortByUrl(e1, e2);
		} else if (sortColumn == RequestMappingView.COLUMN_REQUEST_METHOD) {
			int result = sortByRequestMethod(e1, e2);
			if (result == 0) {
				result = sortByHandlerMethod(e1, e2);
			}
			return result;
		} else if (sortColumn == RequestMappingView.COLUMN_HANDLER_METHOD) {
			return sortByHandlerMethod(e1, e2);
		}
		return 0;
	}

	private int sortByHandlerMethod(Object e1, Object e2) {
		String str1 = labelProvider.getColumnText(e1,
				RequestMappingView.COLUMN_HANDLER_METHOD);
		String str2 = labelProvider.getColumnText(e2,
				RequestMappingView.COLUMN_HANDLER_METHOD);
		return str1.compareTo(str2);
	}

	private int sortByRequestMethod(Object e1, Object e2) {
		String str1 = labelProvider.getColumnText(e1,
				RequestMappingView.COLUMN_REQUEST_METHOD);
		String str2 = labelProvider.getColumnText(e2,
				RequestMappingView.COLUMN_REQUEST_METHOD);
		return str1.compareTo(str2);
	}

	private int sortByUrl(Object e1, Object e2) {
		String str1 = labelProvider.getColumnText(e1,
				RequestMappingView.COLUMN_URL);
		String str2 = labelProvider.getColumnText(e2,
				RequestMappingView.COLUMN_URL);

		String prefix = getCommonPrefix(str1, str2);
		str1 = str1.substring(prefix.length(), str1.length());
		str2 = str2.substring(prefix.length(), str2.length());

		Comparator<String> comparator = matcher.getPatternComparator(prefix);
		if (prefix.length() > 1
				&& (matcher.isPattern(str1) || str1.contains("{") //$NON-NLS-1$
						|| matcher.isPattern(str2) || str2.contains("{"))) { //$NON-NLS-1$
			return comparator.compare(str1, str2);
		} else {
			int strResult = str1.compareTo(str2);
			if (strResult == 0) {
				// If urls are equal, sort by method name
				strResult = sortByHandlerMethod(e1, e2);
			}
			return strResult;
		}
	}

	private String getCommonPrefix(String str1, String str2) {
		if (str1.equals(str2)) {
			return str1;
		}
		String prefix = ""; //$NON-NLS-1$
		int i = 0;
		while (i < str1.length() && i < str2.length()) {
			String s = str1.substring(i, i + 1);
			if (s.equals(str2.substring(i, i + 1))) {
				prefix = prefix.concat(s);
				i++;
			} else {
				break;
			}
		}
		return prefix;
	}

}
