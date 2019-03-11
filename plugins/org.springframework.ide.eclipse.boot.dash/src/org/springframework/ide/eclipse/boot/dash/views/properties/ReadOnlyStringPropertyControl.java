/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.function.Function;

import javax.inject.Provider;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * URL control for property section composite
 *
 * @author Kris De Volder
 */
public class ReadOnlyStringPropertyControl<T extends BootDashElement> extends AbstractBdePropertyControl {

	private final String label;
	private StyledText value;

	private final Class<T> type;
	private final Function<T, String> getter;

	public ReadOnlyStringPropertyControl(Class<T> type, String label, Function<T, String> getter) {
		this.type = type;
		this.label = label;
		this.getter = getter;
	}

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);

		page.getWidgetFactory().createLabel(composite, label).setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		value = new StyledText(composite, SWT.READ_ONLY);
		value.setCaret(null);
		value.setText("Getting value...");
		value.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).create());
	}

	@Override
	public void refreshControl() {
		BootDashElement element = getBootDashElement();
		if (value != null && !value.isDisposed()) {
			String text = fetchValue(element);
			value.setText(text);
			int width = value.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			GridData data = GridDataFactory.copyData(((GridData)value.getLayoutData()));
			data.widthHint = width;
			value.setLayoutData(data);
			value.getParent().layout();
		}
	}

	protected String fetchValue(BootDashElement element) {
		if (element!=null && type.isAssignableFrom(element.getClass())) {
			@SuppressWarnings("unchecked")
			String val = getter.apply((T)element);
			if (val!=null) {
				return val;
			}
		}
		return "";
	}

}
