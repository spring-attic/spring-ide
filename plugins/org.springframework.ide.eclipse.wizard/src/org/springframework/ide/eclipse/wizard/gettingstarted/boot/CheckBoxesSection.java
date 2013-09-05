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
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * @author Kris De Volder
 */
public class CheckBoxesSection<T> extends WizardPageSection {

	private final MultiSelectionFieldModel<T> model;
	private Group group;
	private WizardPageSection[] subsections;
	private int numCols;

	public CheckBoxesSection(IPageWithSections owner, MultiSelectionFieldModel<T> model) {
		super(owner);
		this.model = model;
	}

	public CheckBoxesSection<T> columns(int howMany) {
		Assert.isLegal(howMany>0);
		this.numCols = howMany;
		return this;
	}



	private class CheckBox extends WizardPageSection {

		private Button cb;
		private final T value;
		private final String label;

		public CheckBox(IPageWithSections owner, T value, String label) {
			super(owner);
			this.value = value;
			this.label = label;
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return Validator.OK;
		}

		@Override
		public void createContents(Composite page) {
			if (page!=null && !page.isDisposed()) {
				this.cb = new Button(page, SWT.CHECK);
				cb.setText(label);
				cb.setSelection(model.getSelecteds().contains(value));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(cb);
				cb.addSelectionListener(new SelectionListener() {
					//@Override
					public void widgetSelected(SelectionEvent e) {
						handleSelection();
					}

					//@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						handleSelection();
					}

					private void handleSelection() {
						boolean add = cb.getSelection();
						if (add) {
							model.add(value);
						} else {
							model.remove(value);
						}
					}
				});
			}
		}

		@Override
		public void dispose() {
			if (cb!=null && !cb.isDisposed()) {
				cb.dispose();
				cb = null;
			}
		}
	}

	protected GridLayout createLayout() {
		return new GridLayout(numCols, true);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return model.getValidator();
	}

	@Override
	public void createContents(Composite page) {
		this.group = new Group(page, SWT.NONE);
		this.group.setText(model.getLabel());
		GridLayout layout = createLayout();
		group.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		T[] options = model.getChoices();

		subsections = new WizardPageSection[Math.max(1, options.length)];

//				GridData gd = (GridData) group.getLayoutData();
//				boolean visible = checkboxes.length>0;
//				gd.exclude = !visible;

		if (options.length==0) {
			//don't leave section empty it looks ugly
			subsections[0] = new CommentSection(owner, "No choices available");
			subsections[0].createContents(group);
		}

		for (int i = 0; i < options.length; i++) {
			subsections[i] = new CheckBox(owner, options[i], model.getLabel(options[i]));
			subsections[i].createContents(group);
		}
	}

}
