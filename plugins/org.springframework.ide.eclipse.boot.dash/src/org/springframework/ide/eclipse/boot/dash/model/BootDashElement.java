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
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;

public interface BootDashElement extends Nameable {
	IJavaProject getJavaProject();
	IProject getProject();
	RunState getRunState();
	RunTarget getTarget();

	/**
	 * Return the port this element is running on. If the port can not
	 * be determined or the app is not running this returns -1.
	 */
	int getLivePort();

	/**
	 * Get the 'active' launch configuration. This may be null.
	 * <p>
	 * If only one existing configuration is associated with this element then
	 * it is automatically considered as the 'active' configuration.
	 * <p>
	 * If there are no configurations associated with this element then the active configuration
	 * is undefined (null).
	 * <p>
	 * If more than one configuration exists then the 'preferred config' is used to decide which one
	 * of the existing elements should be considered as 'active'.
	 *
	 * @return active configuration or null.
	 */
	ILaunchConfiguration getActiveConfig();

	/**
	 * A preferred configuration may be associated with an element. This is used by various operations
	 * as a 'tie breaker' if there is more than one existing configuration associated with an element.
	 */
	ILaunchConfiguration getPreferredConfig();
	void setPreferredConfig(ILaunchConfiguration config);

	//TODO: the operations below don't belong here they are really 'UI' not 'model'.

	void stopAsync() throws Exception;
	void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception;
	void openConfig(UserInteractions ui);

}
