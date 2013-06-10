/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Displays a short textual desciption.
 */
public class DescriptionSection extends WizardPageSection {

	private LiveExpression<String> model;

	public DescriptionSection(IPageWithSections owner, LiveExpression<String> description) {
		super(owner);
		this.model = description;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

	@Override
	public void createContents(Composite page) {
//		Composite field = new Composite(page, SWT.NONE);
//		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).create();
//		field.setLayout(layout);
//		Label fieldNameLabel = new Label(field, SWT.NONE);
//		fieldNameLabel.setText("Description");
		
		final Text text = new Text(page, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		
		//Determine vertical space so there's enough room for about 5 lines of text
		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		int preferredHeight = fm.getHeight()*5;
		
//		GridDataFactory grab = GridDataFactory
//				.fillDefaults().align(SWT.FILL, SWT.FILL) //without this SWT.WRAP doesn't work?
//				.grab(true, false)
//				.minSize(SWT.DEFAULT, preferredHeight)
//				.hint(SWT.DEFAULT, preferredHeight);
//		grab.applyTo(field);
//		grab.applyTo(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL); //Without this, SWT.WRAP doesn't work!
		  //See: http://vzurczak.wordpress.com/2012/08/28/force-a-swt-text-to-wrap/
		data.heightHint = preferredHeight;
		text.setLayoutData(data);
		
//		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fieldNameLabel);
		
		this.model.addListener(new ValueListener<String>() {
			@Override
			public void gotValue(LiveExpression<String> exp, String value) {
				text.setText(value);
			}
		});
	}

}
