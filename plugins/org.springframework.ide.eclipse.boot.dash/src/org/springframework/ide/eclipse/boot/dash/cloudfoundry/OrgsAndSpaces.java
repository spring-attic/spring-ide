/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.runtime.CoreException;

/**
 * Hierarchical representation of existing orgs and spaces in a Cloud Foundry
 * target.
 *
 */
public class OrgsAndSpaces {

	private final List<CloudSpace> originalSpaces;

	private Map<String, List<CloudSpace>> orgIDtoSpaces;

	private Map<String, CloudOrganization> orgIDtoOrg;

	/**
	 * 
	 * @param spaces
	 *            a flat list of all spaces for a given set of credentials and
	 *            server URL. Should not be empty or null.
	 * @throws CoreException
	 *             if given cloud server does not support orgs and spaces
	 */
	public OrgsAndSpaces(List<CloudSpace> spaces) {
		this.originalSpaces = spaces;
		setValues();
	}

	public CloudSpace getSpace(String orgName, String spaceName) {
		List<CloudSpace> oSpaces = orgIDtoSpaces.get(orgName);
		if (oSpaces != null) {
			for (CloudSpace clSpace : oSpaces) {
				if (clSpace.getName().equals(spaceName)) {
					return clSpace;
				}
			}
		}
		return null;
	}

	public List<CloudOrganization> getOrgs() {

		Collection<CloudOrganization> orgList = orgIDtoOrg.values();
		return new ArrayList<CloudOrganization>(orgList);
	}

	protected void setValues() {
		orgIDtoSpaces = new HashMap<String, List<CloudSpace>>();
		orgIDtoOrg = new HashMap<String, CloudOrganization>();

		for (CloudSpace clSpace : originalSpaces) {
			CloudOrganization org = clSpace.getOrganization();
			List<CloudSpace> spaces = orgIDtoSpaces.get(org.getName());
			if (spaces == null) {
				spaces = new ArrayList<CloudSpace>();
				orgIDtoSpaces.put(org.getName(), spaces);
				orgIDtoOrg.put(org.getName(), org);
			}

			spaces.add(clSpace);
		}
	}

	/**
	 * @param orgName
	 * @return
	 */
	public List<CloudSpace> getOrgSpaces(String orgName) {
		return orgIDtoSpaces.get(orgName);
	}

	/**
	 * @return all spaces available for the given account. Never null, although
	 *         may be empty if no spaces are resolved.
	 */
	public List<CloudSpace> getAllSpaces() {
		return originalSpaces != null ? new ArrayList<CloudSpace>(originalSpaces) : new ArrayList<CloudSpace>(0);
	}
}
