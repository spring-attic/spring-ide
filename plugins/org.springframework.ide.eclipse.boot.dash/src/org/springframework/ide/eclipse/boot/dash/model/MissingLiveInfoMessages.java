/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

public interface MissingLiveInfoMessages {

	static final String EXTERNAL_DOCUMENT_LINK = "https://github.com/spring-projects/sts4/wiki/Live-Application-Information#application-requirements-for-spring-boot-projects";

	static final MissingLiveInfoMessages DEFAULT = new MissingLiveInfoMessages() {};

	static final String NOT_YET_COMPUTED = "Not yet computed...";

	default String getMissingInfoMessage(String appName, String actuatorEndpoint) {
		StringBuilder message = new StringBuilder();
		message.append("'");
		message.append(appName);
		message.append("'");

		message.append(" must be running with JMX and actuator endpoint enabled:");
		message.append('\n');
		message.append('\n');

		message.append("1. Enable actuator ");
		message.append("'");
		message.append(actuatorEndpoint);
		message.append("'");
		message.append(" endpoint in the application.");
		message.append('\n');

		message.append("2. Select 'Enable JMX' in the application launch configuration.");
		message.append('\n');

		return message.toString();
	}

	static String noSelectionMessage(String element) {
		return "Select single element in Boot Dashboard to see live " + element;
	}

}
