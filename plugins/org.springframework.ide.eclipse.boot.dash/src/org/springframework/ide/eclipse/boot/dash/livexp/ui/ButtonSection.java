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
package org.springframework.ide.eclipse.boot.dash.livexp.ui;

import java.util.concurrent.Callable;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * A section containing a single clickable button.
 *
 * @author Kris De Volder
 */
public class ButtonSection extends WizardPageSection {

	private String label;
	private Callable<Void> clickHandler;

	public ButtonSection(IPageWithSections owner, String label, Callable<Void> clickHandler) {
		super(owner);
		this.label = label;
		this.clickHandler = clickHandler;
	}

	@Override
	public void createContents(Composite page) {
		Button button = new Button(page, SWT.PUSH);
		button.setText(label);
		applyLayoutData(button);
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					clickHandler.call();
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}
		});
	}

	/**
	 * Default implementation aligns the button to the right side of the screen. Override this
	 * method to change the layout.
	 */
	protected void applyLayoutData(Button button) {
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(button);
	}

}
