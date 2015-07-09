/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;

public class CloudFoundryTargetProperties extends TargetProperties {

	public final static String URL_PROP = "url";
	public final static String USERNAME_PROP = "username";
	public final static String ORG_PROP = "organization";
	public final static String SPACE_PROP = "space";
	public final static String SELF_SIGNED_PROP = "selfsigned";

	public CloudFoundryTargetProperties() {
		super(RunTargetTypes.CLOUDFOUNDRY);
	}

	public CloudFoundryTargetProperties(TargetProperties targetProperties) {
		super(targetProperties.getAllProperties(), RunTargetTypes.CLOUDFOUNDRY);
		if (get(RUN_TARGET_ID) == null) {
			put(RUN_TARGET_ID, getId(this));
		}
	}

	public String getUrl() {
		return map.get(URL_PROP);
	}

	public String getSpaceName() {
		return map.get(SPACE_PROP);
	}

	public String getOrganizationName() {
		return map.get(ORG_PROP);
	}

	public boolean isSelfsigned() {
		return map.get(SELF_SIGNED_PROP) != null && Boolean.getBoolean(getAllProperties().get(SELF_SIGNED_PROP));
	}

	public String getUserName() {
		return map.get(USERNAME_PROP);
	}

	public String getPassword() {
		return map.get(PASSWORD_PROP);
	}

	@Override
	public Map<String, String> getPropertiesToPersist() {
		Map<String, String> map = super.getPropertiesToPersist();
		// Exclude password as password are persisted separately
		map.remove(PASSWORD_PROP);
		return map;
	}

	public static String getId(CloudFoundryTargetProperties cloudProps) {
		return getId(cloudProps.getUserName(), cloudProps.getUrl(), cloudProps.getSpaceName(),
				cloudProps.getOrganizationName());
	}

	public static String getId(String userName, String url, String orgName, String spaceName) {
		return userName + " : " + url + " : " + orgName + " : " + spaceName;
	}
}
