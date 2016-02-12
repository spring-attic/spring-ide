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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * URL control for property section composite
 *
 * @author Alex Boyko
 *
 */
public class UrlPropertyControl extends AbstractBdePropertyControl {

	private Hyperlink url;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);

		page.getWidgetFactory().createLabel(composite, "URL:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		url = page.getWidgetFactory().createHyperlink(composite, null, SWT.NO_FOCUS);
		url.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());
		url.setEnabled(false);
		url.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (!url.getText().isEmpty()) {
					UiUtil.openUrl(url.getText());
				}
			}
		});
	}

	@Override
	public void refreshControl() {
		BootDashElement element = getBootDashElement();
			if (url != null && !url.isDisposed()) {
				String text = element == null || element.getUrl() == null ? "" : element.getUrl();
				url.setText(text);
				url.setEnabled(!text.isEmpty());
				int width = url.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				GridData data = GridDataFactory.copyData(((GridData)url.getLayoutData()));
				data.widthHint = width;
				url.setLayoutData(data);
				url.getParent().layout();
		}
	}

}
