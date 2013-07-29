/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.preferences;

import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;

/**
 * 
 * @author Kris De Volder
 */
public class GettingStartedPreferences {

	private static final String DASH_WEB_PAGES = "dashboard_page_urls";
	public static final URLBookmark[] DEFAULT_DASH_WEB_PAGES = {
		new URLBookmark("Getting Started", "http://sagan.cfapps.io/guides/gs"),
		new URLBookmark("News", "http://www.springsource.org/news-events")
	};
	private IEclipsePreferences store;

	public GettingStartedPreferences(IEclipsePreferences node) {
		this.store = node;
	}
	
	public URLBookmark[] getDashboardWebPages() {
		String encoded = store.get(DASH_WEB_PAGES, null);
		if (encoded != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(encoded, URLBookmark[].class);
			} catch (Exception e) {
				GettingStartedActivator.log(e);
			}
		}
		return DEFAULT_DASH_WEB_PAGES;
	}
	
	public void setDashboardWebPages(URLBookmark[] bookmarks) {
		try {
			URLBookmark[] existing = getDashboardWebPages();
			if (!Arrays.equals(existing, bookmarks)) {
				ObjectMapper mapper = new ObjectMapper();
				String encoded = mapper.writeValueAsString(bookmarks);
				store.put(DASH_WEB_PAGES, encoded);
				store.flush();
			}
		} catch (Exception e) {
			GettingStartedActivator.log(e);
		}
	}

}
