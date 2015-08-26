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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;
import org.springframework.ide.eclipse.boot.dash.views.sections.UIUtils;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * @author Martin Lippert
 */
public class ExposedPropertyControl extends AbstractBdePropertyControl {

	private Link exposedURL;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Exposed via:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$

		exposedURL = new Link(composite, SWT.NONE);
		exposedURL.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		exposedURL.setBackground(composite.getBackground());

		exposedURL.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				BootProjectDashElement bde = getLocalBootDashElement();
				if (bde != null) {
					String projectName = bde.getName();
					NGROKClient ngrokClient = NGROKLaunchTracker.get(projectName);
					if (ngrokClient != null) {
						String addr = ngrokClient.getURL();
						UiUtil.openUrl(addr);
					}
				}
			}
		});
	}

	@Override
	public void refreshControl() {
		BootProjectDashElement bde = getLocalBootDashElement();
		if (bde != null) {
			String projectName = bde.getName();
			NGROKClient ngrokClient = NGROKLaunchTracker.get(projectName);
			if (ngrokClient != null) {
				exposedURL.setText(ngrokClient.getTunnel().getPublic_url() + "   --- (local ngrok instance at: <a href=\"\">" + ngrokClient.getURL() + "</a>)");
			}
			else {
				exposedURL.setText("");
			}
		}
	}

	private BootProjectDashElement getLocalBootDashElement() {
		if (exposedURL != null && !exposedURL.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			if (bde instanceof BootProjectDashElement) {
				return (BootProjectDashElement) bde;
			}
		}

		return null;
	}

}
