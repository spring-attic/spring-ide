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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.swt.events.MouseEvent;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashElementsTableSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicCompositeSection.SectionFactory;
import org.springframework.ide.eclipse.boot.dash.views.sections.ExpandableSectionWithSelection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

class RunTargetSectionFactory implements SectionFactory<BootDashModel> {

	protected final IPageWithSections owner;
	private LiveExpression<Filter<BootDashElement>> elementFilter;
	private BootDashViewModel viewModel;
	private UserInteractions ui;
	private ViewStyler viewerStyler;

	/**
	 * This is used as a 'shared event bus' where all
	 * BootDashElementsTableSection post their mousedown events. This allows
	 * tables to clear their selection when the user clicks in another table
	 * without holding down CTRL key.
	 */
	private LiveVariable<MouseEvent> tableMouseEvent;

	public RunTargetSectionFactory(IPageWithSections owner, BootDashViewModel model,
			LiveExpression<Filter<BootDashElement>> elementFilter, UserInteractions ui, ViewStyler viewStyler) {
		this.owner = owner;
		this.viewModel = model;
		this.elementFilter = elementFilter;
		this.tableMouseEvent = new LiveVariable<MouseEvent>();
		this.ui = ui;
		this.viewerStyler = viewStyler;
	}

	@Override
	public IPageSection create(BootDashModel model) {
		RunTarget runTarget = model.getRunTarget();
		String sectionName = runTarget.getName();
		BootDashElementsTableSection section = new BootDashElementsTableSection(owner, viewModel, model, elementFilter,
				tableMouseEvent, ui);
		return new ExpandableSectionWithSelection(owner, sectionName, section, viewerStyler);
	}
}