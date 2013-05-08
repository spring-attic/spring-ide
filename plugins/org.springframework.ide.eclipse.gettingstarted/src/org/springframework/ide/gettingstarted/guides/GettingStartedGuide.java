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

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.Repo;

/**
 * Content for a GettingStartedGuide provided via a Github Repo
 * 
 * @author Kris De Volder
 */
public class GettingStartedGuide {

	private Repo repo;

	public GettingStartedGuide(Repo repo) {
		this.repo = repo;
	}
	
	public String getName() {
		return repo.getName();
	}
	
	public String getDescription() {
		return repo.getDescription();
	}
	
	public URI getHomePage() {
		try {
			return new URI(repo.getHtmlUrl());
		} catch (URISyntaxException e) {
			GettingStartedActivator.log(e);
			return null;
		}
	}
	
}
