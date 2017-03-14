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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springframework.ide.eclipse.boot.dash.views.OpenInBrowserAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

public enum BootDashColumn {

	RUN_STATE_ICN(	"",	        20),
	INSTANCES(      "Instances", 50),
	PROJECT(		"Project", 	150),
	NAME(			"Name",		150),
	HOST(           "Host",     70),
	LIVE_PORT(		"Port",		70),
	DEFAULT_PATH(	"Path",		70),
	TAGS(			"Tags",		100),
	EXPOSED_URL(	"Exposed via",		100),
	DEVTOOLS(		"Devtools", 70),
	TREE_VIEWER_MAIN("", 100); //this is a 'fake' column which corresponds to the single column shown in unified tree viewer.

	//Configure some odds and ends that don't apply to every column:
	static {
		RUN_STATE_ICN.longLabel = "State";

		RUN_STATE_ICN.singleClickAction = new BootDashActionFactory() {
			public AbstractBootDashElementsAction create(BootDashViewModel model, LiveExpression<BootDashElement> hoverElement, UserInteractions ui) {
				return new OpenInBrowserAction(model, MultiSelection.singletonOrEmpty(BootDashElement.class, hoverElement), ui);
			}
		};

		TAGS.editingSupport = new EditingSupportFactory() {
			@Override
			public EditingSupport createEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection,
					BootDashViewModel model, Stylers stylers) {
				return new TagEditingSupport(viewer, selection, model, stylers);
			}
		};

		DEFAULT_PATH.editingSupport = new EditingSupportFactory() {
			@Override
			public EditingSupport createEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection,
					BootDashViewModel model, Stylers stylers) {
				return new DefaultPathEditorSupport(viewer, selection, stylers);
			}
		};
	}

	private final String label;
	private String longLabel;
	private final int defaultWidth;
	private int allignment = SWT.LEFT;
	private EditingSupportFactory editingSupport;
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

	public EditingSupportFactory getEditingSupport() {
		return editingSupport;
	}

	public BootDashActionFactory getSingleClickAction() {
		return singleClickAction;
	}
}