/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * CFT wrapper around {@link UserInteractions} used by boot dashboard.
 *
 */
public class CftUiInteractions {

	public UserInteractions getUserInteractions() {
		return BootDashActivator.getDefault().getInjections().getBean(UserInteractions.class);
	}

}
