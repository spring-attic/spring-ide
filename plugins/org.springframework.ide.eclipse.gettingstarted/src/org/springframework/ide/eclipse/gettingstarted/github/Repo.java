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
package org.springframework.ide.eclipse.gettingstarted.github;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Repo extends IdAble {

	@JsonProperty("name")
	private String name;
	
	@JsonProperty("full_name")
	private String fullName;
	
	@JsonProperty("private")
	private boolean isPrivate;
	
	@JsonProperty("html_url")
	private String htmlUrl;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("fork")
	private boolean isFork;
	
	@JsonProperty("url")
	private String url;
	
	private Owner owner;

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public Owner getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return "Repo("+getName()+")";
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}
	
	public String getHtmlUrl() {
		return htmlUrl;
	}

}
