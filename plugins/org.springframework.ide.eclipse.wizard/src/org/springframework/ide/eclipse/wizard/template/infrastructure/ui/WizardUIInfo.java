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
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WizardUIInfo {
	static class OrderComparator implements Comparator<WizardUIInfoElement> {
		public int compare(WizardUIInfoElement o1, WizardUIInfoElement o2) {
			return o1.getOrder() - o2.getOrder();
		}
	}

	static class PageComparator implements Comparator<WizardUIInfoElement> {
		public int compare(WizardUIInfoElement o1, WizardUIInfoElement o2) {
			return o1.getPage() - o2.getPage();
		}
	}

	private static final String PROJECT_NAME = "projectName";

	private ArrayList<WizardUIInfoElement> elements;

	private ArrayList<WizardUIInfoPage> pages;

	private String topLevelPackage;

	private String projectName;

	/**
	 * Get elements for the given page sorted by their relative order
	 * @param page
	 * @return
	 */
	public List<WizardUIInfoElement> getElementsForPage(int page) {
		List<WizardUIInfoElement> pageElements = new ArrayList<WizardUIInfoElement>();
		if (elements != null) {
			for (WizardUIInfoElement element : elements) {
				if (element.getPage() == page) {
					pageElements.add(element);
				}
			}
		}
		Collections.sort(pageElements, new OrderComparator());
		return pageElements;
	}

	public WizardUIInfoPage getPage(int order) {
		for (WizardUIInfoPage page : pages) {
			if (page.getOrder() == order) {
				return page;
			}
		}
		WizardUIInfoPage defaultPage = WizardUIInfoPage.getDefaultPage(order);
		pages.add(defaultPage);
		return defaultPage;
	}

	public int getPageCount() {
		if (elements == null || elements.size() == 0) {
			return 1;
		}
		WizardUIInfoElement element = Collections.max(elements, new PageComparator());
		return element.getPage() + 1;
	}

	public String getProjectNameToken() {
		if (projectName == null) {
			return PROJECT_NAME;
		}
		return projectName;
	}

	public String[] getTopLevelPackageTokens() {
		if (topLevelPackage != null) {
			return topLevelPackage.split("\\.");
		}
		return new String[0];
	}
}
