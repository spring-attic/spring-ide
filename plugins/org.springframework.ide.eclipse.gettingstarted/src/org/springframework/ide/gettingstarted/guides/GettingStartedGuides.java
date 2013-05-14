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
package org.springframework.ide.gettingstarted.guides;

import java.io.File;
import java.util.List;

import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.github.auth.AuthenticatedDownloader;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;

/**
 * An instance of this class provides access to Getting started content of type 'Guide'
 * 
 * @author Kris De Volder
 */
public class GettingStartedGuides {
	
	private static GettingStartedGuides instance;
	
	private GettingStartedGuide[] guides;
	
	private DownloadManager downloader = new DownloadManager(new AuthenticatedDownloader(), 
			new File(
					GettingStartedActivator.getDefault().getStateLocation().toFile(),
					"guides"
			)
	);
	
	//TODO: anticipate a need to have content for guides provided in different ways.
	
	public static GettingStartedGuides getInstance() {
		if (instance==null) {
			instance = new GettingStartedGuides();
		}
		return instance;
	}
	
	public GettingStartedGuide[] getAll() {
		if (guides==null) {
			guides = fetchGuides();
		}
		return guides;
	}

	/**
	 * Called to fetch the guides from wherever we are getting them from. 
	 */
	private GettingStartedGuide[] fetchGuides() {
		List<Repo> repos = new GithubClient().getGuidesRepos();
		GettingStartedGuide[] guides = new GettingStartedGuide[repos.size()];
		for (int i = 0; i < guides.length; i++) {
			guides[i] = new GettingStartedGuide(repos.get(i), downloader);
		}
		return guides;
	}
	
}
