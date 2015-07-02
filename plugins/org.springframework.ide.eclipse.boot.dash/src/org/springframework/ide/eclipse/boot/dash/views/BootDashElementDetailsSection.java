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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * @author Kris De Volder
 */
public class BootDashElementDetailsSection extends PageSection {

	private LiveExpression<BootDashElement> input;
	private RequestMappingsSection requestMappings;

	protected BootDashElementDetailsSection(IPageWithSections owner, BootDashViewModel model, LiveExpression<BootDashElement> input) {
		super(owner);
		this.input = input;
		this.requestMappings = new RequestMappingsSection(owner, model, input);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(Composite page) {
		final Label header = new Label(page, SWT.NONE);
		input.addListener(new ValueListener<BootDashElement>() {
			public void gotValue(LiveExpression<BootDashElement> exp, BootDashElement value) {
				if (value!=null) {
					header.setText("Request mappings for: "+value.getName());
				} else {
					header.setText("Select a single element to see its request mappings");
				}
			}
		});
		requestMappings.createContents(page);
	}
}
