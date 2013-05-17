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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;

/**
 * Content for a GettingStartedGuide provided via a Github Repo
 * 
 * @author Kris De Volder
 */
public class GettingStartedGuide extends GithubContent {

	private Repo repo;
	private DownloadManager downloader;

	public GettingStartedGuide(Repo repo, DownloadManager downloader) {
		this.repo = repo;
		this.downloader = downloader;
	}

	public String getName() {
		return repo.getName();
	}
	
	public String getDescription() {
		return repo.getDescription();
	}
	
	public URL getHomePage() {
		try {
			return new URL(repo.getHtmlUrl());
		} catch (MalformedURLException e) {
			GettingStartedActivator.log(e);
			return null;
		}
	}
	
	/**
	 * Get a URL pointing to zip file where the entire contents of this
	 * guide can be downloaded. 
	 */
	public DownloadableItem getZip() {
		String repoUrl = repo.getHtmlUrl(); 
		//repoUrl is something like "https://github.com/springframework-meta/gs-consuming-rest-android"
		//zipUrl is something like  "https://github.com/springframework-meta/gs-consuming-rest-android/archive/master.zip" 
		try {
			DownloadableItem item = new DownloadableItem(new URL(repoUrl+"/archive/master.zip"), downloader);
			item.setFileName(getName());
			return item;
		} catch (MalformedURLException e) {
			GettingStartedActivator.log(e);
			return null;
		}
	}
	
	public List<CodeSet> getCodeSets() {
		return Arrays.asList(
				CodeSet.fromZip("initial", getZip(), getRootPath().append("initial")),
				CodeSet.fromZip("complete", getZip(), getRootPath().append("complete"))
		);
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
		return new Path(getName()+"-master");
	}

	public CodeSet getInitialCodeSet() {
		return getCodeSets().get(0);
	}
	
	public CodeSet getCompleteCodeSet() {
		return getCodeSets().get(1);
	}
	
}
