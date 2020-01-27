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

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;

/**
 * CFT wrapper around {@link UserInteractions} used by boot dashboard.
 *
 */
public class CftUiInteractions {

	private final UIContext uiContext;

	/**
	 * 
	 * @param uiContext UI context that contains a shell to use for user
	 *                  interactions
	 */
	public CftUiInteractions(UIContext uiContext) {
		this.uiContext = uiContext;
	}

	public UserInteractions getUserInteractions() {
		return new DefaultUserInteractions(getDiContext(uiContext));
	}
	
	protected SimpleDIContext getDiContext(UIContext uiContext) {
		return new SimpleDIContext().defInstance(UIContext.class, uiContext);
	}
}
