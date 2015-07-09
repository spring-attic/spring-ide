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
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.OpenInBrowserAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public enum BootDashColumn {

	RUN_STATE_ICN(	"",	        20),
	INSTANCES(      "Instances", 50),
	PROJECT(		"Project", 	150),
	APP(    		"Application", 150),
	HOST(           "Host",     70),
	LIVE_PORT(		"Port",		70),
	DEFAULT_PATH(	"Path",		70),
	TAGS(			"Tags",		100);
	// RUN_TARGET(		"Target", 	60); TODO: remove? Why display thsi in table ever since its at the top of the table.

	//Configure some odds and ends that don't apply to every column:
	static {
		RUN_STATE_ICN.longLabel = "State";
		TAGS.editingSupportClass = TagEditingSupport.class;
		DEFAULT_PATH.editingSupportClass = DefaultPathEditorSupport.class;

		RUN_STATE_ICN.singleClickAction = new BootDashActionFactory() {
			public AbstractBootDashAction create(BootDashViewModel model, LiveExpression<BootDashElement> hoverElement, UserInteractions ui) {
				return new OpenInBrowserAction(model, MultiSelection.singletonOrEmpty(BootDashElement.class, hoverElement), ui);
			}
		};
	}

	private final String label;
	private String longLabel;
	private final int defaultWidth;
	private int allignment = SWT.LEFT;
	private Class<? extends EditingSupport> editingSupportClass;
	private BootDashActionFactory singleClickAction;

	private BootDashColumn(String label, int defaultWidth) {
		this.label = label;
		this.longLabel = label;
		this.defaultWidth = defaultWidth;
	}

	public String getLabel() {
		return label;
	}

	public String getLongLabel() {
		return longLabel;
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

	public BootDashActionFactory getSingleClickAction() {
		return singleClickAction;
	}
}