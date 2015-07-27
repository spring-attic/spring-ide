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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * Default Path control for the properties section
 *
 * @author Alex Boyko
 *
 */
public class DefaultPathPropertyControl extends AbstractBdePropertyControl {

	private Text defaultPath;

	@Override
	public void createControl(final Composite composite, TabbedPropertySheetPage page) {
		page.getWidgetFactory().createLabel(composite, "Path:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		defaultPath = page.getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		defaultPath.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		defaultPath.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				BootDashElement bde = getBootDashElement();
				if (bde != null) {
					bde.setDefaultRequestMapingPath(defaultPath.getText());
				}
			}
		});
		defaultPath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\u001b') { // Escape character
					defaultPath.setText(getLabelProvider().getText(getBootDashElement(), BootDashColumn.DEFAULT_PATH));
					composite.getDisplay().getActiveShell().forceFocus();
				} else if (e.character == '\r') { // Return key
					composite.getDisplay().getActiveShell().forceFocus();
				}

			}
		});
	}

	@Override
	public void refreshControl() {
		BootDashElement bde = getBootDashElement();
		defaultPath.setText(getLabelProvider().getText(bde, BootDashColumn.DEFAULT_PATH));
	}

}
