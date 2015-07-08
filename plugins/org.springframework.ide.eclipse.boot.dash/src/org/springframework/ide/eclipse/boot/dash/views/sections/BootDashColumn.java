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

	PROJECT(		"Project", 	150),
	APP(    		"Application", 150),
	RUN_TARGET(		"Target", 	60),
	RUN_STATE(		"State", 	100),
	RUN_STATE_ICN(	"",		"State",	20),
	TAGS(			"Tags",		100),
	LIVE_PORT(		"Port",		70),
	DEFAULT_PATH(	"Path",		70),
	HOST(           "Host",     70),
	INSTANCES(      "Instances", 50);

	//Configure some odds and ends that don't apply to every column:
	static {
		TAGS.editingSupportClass = TagEditingSupport.class;
		DEFAULT_PATH.editingSupportClass = DefaultPathEditorSupport.class;

		RUN_STATE_ICN.singleClickAction = new BootDashActionFactory() {
			public AbstractBootDashAction create(BootDashViewModel model, LiveExpression<BootDashElement> hoverElement, UserInteractions ui) {
				return new OpenInBrowserAction(model, MultiSelection.singletonOrEmpty(BootDashElement.class, hoverElement), ui);
			}
		};
	}

	private final String label;
	private final String longLabel;
	private final int defaultWidth;
	private final int allignment;
	private Class<? extends EditingSupport> editingSupportClass;
	private BootDashActionFactory singleClickAction;

	private BootDashColumn(String label, int defaultWidth) {
		this(label, defaultWidth, SWT.LEFT);
	}

	private BootDashColumn(String label, String longLabel, int defaultWidth) {
		this(label, longLabel, defaultWidth, SWT.LEFT);
	}

	private BootDashColumn(String label, int defaultWidth, int allignment) {
		this(label, label, defaultWidth, allignment);
	}

	private BootDashColumn(String label, String longLabel, int defaultWidth, int allignment) {
		this.label = label;
		this.longLabel = longLabel;
		this.defaultWidth = defaultWidth;
		this.allignment = allignment;
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