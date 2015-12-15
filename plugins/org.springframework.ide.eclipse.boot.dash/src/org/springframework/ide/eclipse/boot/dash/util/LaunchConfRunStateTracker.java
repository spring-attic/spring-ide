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
package org.springframework.ide.eclipse.boot.dash.util;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Kris De Volder
 */
public class LaunchConfRunStateTracker extends RunStateTracker<ILaunchConfiguration> {

	@Override
	protected ILaunchConfiguration getOwner(ILaunch l) {
		return l.getLaunchConfiguration();
	}

}
