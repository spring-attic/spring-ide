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

	ILaunchConfiguration getConfig();
	void setConfig(ILaunchConfiguration config);

	//TODO: the operations below don't belong here they are really 'UI' not 'model'.

	void stopAsync() throws Exception;
	void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception;
	void openConfig(UserInteractions ui);

}
