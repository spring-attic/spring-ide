/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.github.auth.AuthenticatedDownloader;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;

/**
 * Singleton class. The instance of this class provides access to all the
 * getting started content.
 * 
 * NOTE: templates are not (yet?) included in this. Code to discover and
 * manage them already existed before this framework was implemented.
 */
public class GettingStartedContent extends ContentManager {

	private static GettingStartedContent INSTANCE = null;
	
	public static GettingStartedContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GettingStartedContent();
		}
		return INSTANCE;
	}
	
	private GithubClient github = new GithubClient(); 
	
	private GettingStartedContent() {
		register(GettingStartedGuide.class, new ContentProvider<GettingStartedGuide>() {
			@Override
			public GettingStartedGuide[] fetch(DownloadManager downloader) {
				Repo[] repos = github.getOrgRepos("springframework-meta");
				List<GettingStartedGuide> guides = new ArrayList<GettingStartedGuide>();
				for (Repo repo : repos) {
					if (repo.getName().startsWith("gs-")) {
						guides.add(new GettingStartedGuide(repo, downloader));
					}
				}
				return guides.toArray(new GettingStartedGuide[guides.size()]);
			}
		});
	}
	
	/**
	 * Factory method to create a DownloadManager for a given content type name
	 */
	@Override
	public DownloadManager downloadManagerFor(Class<?> contentType) {
		return new DownloadManager(new AuthenticatedDownloader(), 
				new File(
						GettingStartedActivator.getDefault().getStateLocation().toFile(),
						contentType.getSimpleName()
				)
		);		
	}

	/**
	 * Get all getting started guides.
	 */
	public GettingStartedGuide[] getGuides() {
		return get(GettingStartedGuide.class);
	}
	
	
}
