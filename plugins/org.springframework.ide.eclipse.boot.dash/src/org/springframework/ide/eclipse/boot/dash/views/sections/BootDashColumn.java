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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.swt.SWT;

public enum BootDashColumn {

	PROJECT(		"Project", 	200),
	RUN_TARGET(		"Target", 	60),
	RUN_STATE(		"State", 	100),
	RUN_STATE_ICN(	"",			20);

	private final String label;
	private final int defaultWidth;
	private final int allignment;

	private BootDashColumn(String label, int defaultWidth) {
		this(label, defaultWidth, SWT.LEFT);
	}

	private BootDashColumn(String label, int defaultWidth, int allignment) {
		this.label = label;
		this.defaultWidth = defaultWidth;
		this.allignment = allignment;
	}
	public String getLabel() {
		return label;
	}
	public int getDefaultWidth() {
		return defaultWidth;
	}

	/**
	 * @return SWT style constant controlling aligment (e.g SWT.LEFT or SWT.RIGHT)
	 */
	public int getAllignment() {
		return allignment;
	}
}