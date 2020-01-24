/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

public enum BootDashColumn {

	RUN_STATE_ICN,
	INSTANCES,
	PROJECT,
	NAME,
	HOST,
	LIVE_PORT,
	DEFAULT_PATH,
	TAGS,
	EXPOSED_URL,
	DEVTOOLS,
	JMX_SSH_TUNNEL,
	TREE_VIEWER_MAIN; //this is a 'fake' column which corresponds to the single column shown in unified tree viewer.

}