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
 * Host control for the property section
 *
 * @author Alex Boyko
 *
 */
public class HostPropertyControl extends AbstractBdePropertyControl {

	private Label host;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		page.getWidgetFactory().createLabel(composite, "Host:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		host = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		host.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		if (host != null && !host.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			host.setText(getLabelProvider().getText(bde, BootDashColumn.HOST));
		}
	}

}
