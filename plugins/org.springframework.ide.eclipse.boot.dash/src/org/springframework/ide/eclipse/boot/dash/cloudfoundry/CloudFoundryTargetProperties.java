/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.util.Log;

public class CloudFoundryTargetProperties extends TargetProperties {


	public final static String ORG_PROP = "organization";
	public final static String SPACE_PROP = "space";
	public final static String SELF_SIGNED_PROP = "selfsigned";
	public final static String SKIP_SSL_VALIDATION_PROP = "skipSslValidation";

	public final static String ORG_GUID = "organization_guid";
	public final static String SPACE_GUID = "space_guid";

	public final static String DISCONNECTED = "disconnected";

	public CloudFoundryTargetProperties(RunTargetType runTargetType, BootDashModelContext context) {
		super(runTargetType, context);
	}

	public CloudFoundryTargetProperties(TargetProperties targetProperties, RunTargetType runTargetType) {
		super(targetProperties, runTargetType);
		if (get(RUN_TARGET_ID) == null) {
			put(RUN_TARGET_ID, getId(this));
		}
	}

	public String getSpaceName() {
		return map.get(SPACE_PROP);
	}

	public String getOrganizationName() {
		return map.get(ORG_PROP);
	}

	public String getSpaceGuid() {
		return map.get(SPACE_GUID);
	}

	public String getOrganizationGuid() {
		return map.get(ORG_GUID);
	}

	public boolean isSelfsigned() {
		return map.get(SELF_SIGNED_PROP) != null && Boolean.parseBoolean(map.get(SELF_SIGNED_PROP));
	}

	public boolean skipSslValidation() {
		return map.get(SKIP_SSL_VALIDATION_PROP) != null && Boolean.parseBoolean(map.get(SKIP_SSL_VALIDATION_PROP));
	}

	public static String getId(CloudFoundryTargetProperties cloudProps) {
		return getId(cloudProps.getUsername(), cloudProps.getUrl(), cloudProps.getOrganizationName(),
				cloudProps.getSpaceName());
	}

	public static String getName(CloudFoundryTargetProperties cloudProps) {
		return cloudProps.getOrganizationName() + " : " + cloudProps.getSpaceName() + " - [" + cloudProps.getUrl()
				+ "]";
	}

	public static String getId(String userName, String url, String orgName, String spaceName) {
		return userName + " : " + url + " : " + orgName + " : " + spaceName;
	}
}
