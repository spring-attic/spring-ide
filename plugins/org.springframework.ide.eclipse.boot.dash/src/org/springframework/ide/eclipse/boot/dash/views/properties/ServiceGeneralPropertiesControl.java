/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudServiceDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * Properties general section control for Cloud Service
 *
 * @author Alex Boyko
 *
 */
public class ServiceGeneralPropertiesControl extends AbstractBdePropertyControl {

	private static final String EMPTY_STR = "";

	private Label plan;
	private Label provider;
	private Label version;
	private Label type;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);

		page.getWidgetFactory().createLabel(composite, "Plan:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		plan = page.getWidgetFactory().createLabel(composite, "", SWT.BORDER); //$NON-NLS-1$
		plan.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());

		page.getWidgetFactory().createLabel(composite, "Provider:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		provider = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		provider.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());

		page.getWidgetFactory().createLabel(composite, "Version:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		version = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		version.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());

		page.getWidgetFactory().createLabel(composite, "Type:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		type = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		type.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());
	}

	@Override
	public void refreshControl() {
		BootDashElement element = getBootDashElement();
		if (element instanceof CloudServiceDashElement) {
			CFService service = ((CloudServiceDashElement) element).getCloudService();
			if (plan != null && !plan.isDisposed()) {
				plan.setText(service == null || service.getPlan() == null ? EMPTY_STR : service.getPlan());
			}
			if (provider != null && !provider.isDisposed()) {
				provider.setText(service == null || service.getProvider() == null ? EMPTY_STR : service.getProvider());
			}
			if (version != null && !version.isDisposed()) {
				version.setText(service == null || service.getProvider() == null ? EMPTY_STR : service.getVersion());
			}
			if (type != null && !type.isDisposed()) {
				type.setText(service == null || service.getType() == null ? EMPTY_STR : service.getType());
			}
		}
	}

}
