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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * Control for Instances of the app for properties section
 *
 * @author Alex Boyko
 *
 */
public class InstancesPropertyControl extends AbstractBdePropertyControl {

	private Label instances;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		page.getWidgetFactory().createLabel(composite, "Instances:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		instances = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		instances.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		BootDashElement element = getBootDashElement();
		instances.setText(getLabelProvider().getText(element, BootDashColumn.INSTANCES));
	}

}
