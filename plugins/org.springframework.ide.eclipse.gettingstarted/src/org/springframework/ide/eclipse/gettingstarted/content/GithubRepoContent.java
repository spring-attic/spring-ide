/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;
import org.springframework.ide.eclipse.gettingstarted.util.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public abstract class GithubRepoContent extends AGSContent {

	private DownloadableItem zip;
	
	protected GithubRepoContent(DownloadManager dl) {
		super(dl);
	}
	
	public URL getHomePage() {
		try {
			return new URL(getRepo().getHtmlUrl());
		} catch (MalformedURLException e) {
			GettingStartedActivator.log(e);
			return null;
		}
	}

	public abstract Repo getRepo();
	
	/**
	 * Get a URL pointing to zip file where the entire contents of this
	 * repo (master branch) can be downloaded. 
	 */
	public DownloadableItem getZip() {
		if (zip==null) {
			String repoUrl = getRepo().getHtmlUrl(); 
			//repoUrl is something like "https://github.com/springframework-meta/gs-consuming-rest-android"
			//zipUrl is something like  "https://github.com/springframework-meta/gs-consuming-rest-android/archive/master.zip" 
			try {
				DownloadableItem item = new DownloadableItem(new URL(repoUrl+"/archive/master.zip"), downloader);
				item.setFileName(getRepo().getName());
				zip = item;
			} catch (MalformedURLException e) {
				GettingStartedActivator.log(e);
				return null;
			}
		}
		return zip;
	}
	
	public String getName() {
		return getRepo().getName();
	}
	
	/**
	 * Defines the location of the 'root' relative to the zip file. The interesting contents
	 * of the zip file may not be directly at the root of the archive.
	 * <p>
	 * Note this method is made public for testing purposes only. Clients shouldn't really
	 * need to get at this information. Rather they should rely on the 'CodeSets' to
	 * access this data.
	 */
	public IPath getRootPath() {
		return new Path(getRepo().getName()+"-master");
	}
	
	public String getDescription() {
		return getRepo().getDescription();
	}

}
