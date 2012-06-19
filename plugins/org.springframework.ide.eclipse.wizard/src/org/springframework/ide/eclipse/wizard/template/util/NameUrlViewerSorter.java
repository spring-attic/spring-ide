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
package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

class NameUrlViewerSorter extends ViewerSorter {
	// sortByName true to sort by name, false to
	// sort by URL
	boolean sortByName;

	public NameUrlViewerSorter(boolean sortByName) {
		this.sortByName = sortByName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		NameUrlPair nameUrlPair1 = (NameUrlPair) e1;
		NameUrlPair nameUrlPair2 = (NameUrlPair) e2;
		if (sortByName) {
			return getComparator().compare(notNull(nameUrlPair1.getName()), notNull(nameUrlPair2.getName()));
		}

		if (nameUrlPair1.getUrlString() == null) {
			return -1;
		}
		if (nameUrlPair2.getUrlString() == null) {
			return 1;
		}
		return getComparator().compare(notNull(nameUrlPair1.getUrlString()), notNull(nameUrlPair2.getUrlString()));
	}

	protected String notNull(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}
}