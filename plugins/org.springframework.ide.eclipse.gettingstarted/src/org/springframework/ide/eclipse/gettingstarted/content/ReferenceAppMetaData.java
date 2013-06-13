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

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * JSON metadata about reference apps retrieved from some external url.
 * 
 * @author Kris De Volder
 */
public class ReferenceAppMetaData {

	@JsonProperty("name")
	private String name; //optional. If not provided reference app name will be repo name
	
	@JsonProperty("type")
	private String type; //only legal value now is "github". In the future maybe we will allow other types of metadata
						  // to define to other ways/places of obtaining the sample code.
	
	@JsonProperty("description")
	private String description; //optional if not provided will use the github repo's description
	
	@JsonProperty("owner")
	private String owner; //mandatory: github repo owner name

	@JsonProperty("repo")
	private String repo; ///mandatory:  repo name

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	public String getOwner() {
		return owner;
	}
	public String getRepo() {
		return repo;
	}
	
}
