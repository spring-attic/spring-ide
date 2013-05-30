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
import java.util.NoSuchElementException;

import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * Content for a GettingStartedGuide provided via a Github Repo
 * 
 * @author Kris De Volder
 */
public class GettingStartedGuide extends GithubRepoContent {

	protected Repo repo;

	private List<CodeSet> codesets;

	/**
	 * All getting started guides are supposed to have the same codesets names. This constant defines those
	 * names.
	 */
	public static final String[] codesetNames = {
		"initial", "complete"
	};
	
	public GettingStartedGuide(Repo repo, DownloadManager dl) {
		super(dl);
		this.repo = repo;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+getName()+")";
	}
	
	public List<CodeSet> getCodeSets() {
		if (codesets==null) {
			CodeSet[] array = new CodeSet[codesetNames.length];
			for (int i = 0; i < array.length; i++) {
				array[i] = CodeSet.fromZip(codesetNames[i], getZip(), getRootPath().append(codesetNames[i]));
			}
			codesets = Arrays.asList(array);
		}
		return codesets;
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

	/**
	 * Creates a validator that checks whether a given build type is supported by a project. This only
	 * consider project content, not whether requisite build tooling is installed.
	 * <p>
	 * This validator needs access to the content. Thus it forces the content to be downloaded.
	 * Content is downloaded in a background job so as not to block the UI thread in which
	 * this method is typically called to provide validation logic for a wizard.
	 * <p>
	 * The returned validator will provide a specifc 'Content is downloading message' until the
	 * content is available. After than point a real validation message will be returned.
	 */
	public ValidationResult validateBuildType(BuildType bt) {
		return getInitialCodeSet().validateBuildType(bt);
	}
	
	public boolean isDownloaded() {
		return getZip().isDownloaded();
	}

	public CodeSet getCodeSet(String name) {
		for (CodeSet cs : getCodeSets()) {
			if (cs.getName().equals(name)) {
				return cs;
			}
		}
		throw new NoSuchElementException(this+" has no codeset '"+name+"'");
	}
	
}
