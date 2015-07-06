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

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashElementsTableSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicCompositeSection.SectionFactory;
import org.springframework.ide.eclipse.boot.dash.views.sections.ExpandableSectionWithSelection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

class RunTargetSectionFactory implements SectionFactory<BootDashModel> {

	protected final IPageWithSections owner;
	private LiveExpression<Filter<BootDashElement>> elementFilter;
	private BootDashViewModel viewModel;

	public RunTargetSectionFactory(IPageWithSections owner, BootDashViewModel model, LiveExpression<Filter<BootDashElement>> elementFilter) {
		this.owner = owner;
		this.viewModel = model;
		this.elementFilter = elementFilter;
	}

	@Override
	public IPageSection create(BootDashModel model) {
		RunTarget runTarget = model.getRunTarget();
		String sectionName = runTarget.getName();
		BootDashElementsTableSection section = new BootDashElementsTableSection(owner, viewModel, model, elementFilter);
		return new ExpandableSectionWithSelection(owner, sectionName, section);
	}
}