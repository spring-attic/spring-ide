/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;

/**
 * @author Martin Lippert
 */
public class ExposedPropertyControl extends AbstractBdePropertyControl {

	private Label exposedURL;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Exposed via:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		exposedURL = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		exposedURL.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		if (exposedURL != null && !exposedURL.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			if (bde instanceof BootProjectDashElement) {
				String projectName = bde.getName();
				NGROKClient ngrokClient = NGROKLaunchTracker.get(projectName);
				if (ngrokClient != null) {
					exposedURL.setText(ngrokClient.getTunnel().getPublic_url());
				}
				else {
					exposedURL.setText("");
				}
			}
		}
	}

}
