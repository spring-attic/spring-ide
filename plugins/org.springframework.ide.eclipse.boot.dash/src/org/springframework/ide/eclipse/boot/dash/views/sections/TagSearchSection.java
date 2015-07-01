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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.TagSearchFilter;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * Tags searching section. Creates a search text box UI wise and allows to
 * register listeners to tags and search term changes.
 * 
 * @author Alex Boyko
 *
 */
public class TagSearchSection extends PageSection implements Disposable {
	
	private Text tagsSearchBox;

	private LiveVariable<Filter<BootDashElement>> searchFilterModel;
	
	public TagSearchSection(IPageWithSections owner, LiveVariable<Filter<BootDashElement>> searchFilterModel) {
		super(owner);
		this.searchFilterModel = searchFilterModel;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(Composite page) {
		tagsSearchBox = new Text(page, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		tagsSearchBox.setMessage("Type tags to match");
		if (searchFilterModel.getValue() instanceof TagSearchFilter) {
			tagsSearchBox.setText(searchFilterModel.toString());			
		}
		tagsSearchBox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		tagsSearchBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (tagsSearchBox.getText().isEmpty()) {
					searchFilterModel.setValue(null);
				} else {
					searchFilterModel.setValue(new TagSearchFilter(tagsSearchBox.getText()));					
				}
			}
		});
	}

	@Override
	public void dispose() {
		tagsSearchBox.dispose();
	}

}
