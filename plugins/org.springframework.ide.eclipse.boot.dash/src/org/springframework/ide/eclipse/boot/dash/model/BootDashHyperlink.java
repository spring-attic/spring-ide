/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.boot.dash.util.Performable;

public abstract class BootDashHyperlink implements ButtonModel {

	private String linkText;
	private Performable clickHandler;

	public BootDashHyperlink(String linkText) {
		this.linkText = linkText;
	}

	@Override
	public String getLabel() {
		return linkText;
	}
}
