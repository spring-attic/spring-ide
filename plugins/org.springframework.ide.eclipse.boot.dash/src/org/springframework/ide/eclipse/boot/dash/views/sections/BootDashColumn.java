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

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;

public enum BootDashColumn {

	PROJECT(		"Project", 	150),
	RUN_TARGET(		"Target", 	60),
	RUN_STATE(		"State", 	100),
	RUN_STATE_ICN(	"",			20),
	TAGS(			"Tags",		100,	TagEditingSupport.class),
	LIVE_PORT(		"Port",		70);

	private final String label;
	private final int defaultWidth;
	private final int allignment;
	private final Class<? extends EditingSupport> editingSupportClass;

	private BootDashColumn(String label, int defaultWidth) {
		this(label, defaultWidth, SWT.LEFT, null);
	}

	private BootDashColumn(String label, int defaultWidth, Class<? extends EditingSupport> editingSupportClass) {
		this(label, defaultWidth, SWT.LEFT, editingSupportClass);
	}

	private BootDashColumn(String label, int defaultWidth, int allignment, Class<? extends EditingSupport> editingSupportClass) {
		this.label = label;
		this.defaultWidth = defaultWidth;
		this.allignment = allignment;
		this.editingSupportClass = editingSupportClass;
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

	public Class<? extends EditingSupport> getEditingSupportClass() {
		return editingSupportClass;
	}
}