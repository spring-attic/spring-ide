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

import java.util.Arrays;
import java.util.List;

import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;

/**
 * Content for a GettingStartedGuide provided via a Github Repo
 * 
 * @author Kris De Volder
 */
public class GettingStartedGuide extends GithubRepoContent {

	protected Repo repo;
	
	public GettingStartedGuide(Repo repo, DownloadManager dl) {
		super(dl);
		this.repo = repo;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+getName()+")";
	}
	
	public List<CodeSet> getCodeSets() {
		return Arrays.asList(
				CodeSet.fromZip("initial", getZip(), getRootPath().append("initial")),
				CodeSet.fromZip("complete", getZip(), getRootPath().append("complete"))
		);
	}
	
	public CodeSet getInitialCodeSet() {
		return getCodeSets().get(0);
	}
	
	public CodeSet getCompleteCodeSet() {
		return getCodeSets().get(1);
	}

	@Override
	public Repo getRepo() {
		return this.repo;
	}
	
}
