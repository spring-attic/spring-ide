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

import static org.springframework.ide.eclipse.boot.dash.views.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.BootDashColumn.RUN_STATE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;

/**
 * Test view to experiment with widgets and sections before mucking up the
 * existing dashboard view.
 *
 * TODO: Remove or at least deactivate this view prior to merging into
 * master.
 *
 * @author Kris De Volder
 */
public class TestView extends ViewPartWithSections {

	private BootDashModel model = BootDashActivator.getDefault().getModel();

	@Override
	protected List<IPageSection> createSections() throws CoreException {
		List<IPageSection> sections = new ArrayList<IPageSection>();
		BootDashElementsTableSection localApsTable = new BootDashElementsTableSection(this, model);
		localApsTable.setColumns(PROJECT, RUN_STATE);
		sections.add(new ExpandableSection(this, "Local Boot Apps", localApsTable));
		return sections;
	}

}
