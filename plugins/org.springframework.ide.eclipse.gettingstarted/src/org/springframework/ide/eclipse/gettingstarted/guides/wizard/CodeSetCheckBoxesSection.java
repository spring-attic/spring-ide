/*******************************************************************************
 * Copyright (c) 2013 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.guides.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

/**
 * @author Kris De Volder
 */
public class CodeSetCheckBoxesSection extends GroupSection {

	public static class CheckBox extends WizardPageSection {

		private String name;
		private MultiSelectionModel<String> model;
		
		public CheckBox(WizardPageWithSections owner, String name, MultiSelectionModel<String> model) {
			super(owner);
			this.name = name;
			this.model = model;
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return Validator.OK;
		}

		@Override
		public void createContents(Composite page) {
			final Button cb = new Button(page, SWT.CHECK);
			cb.setText(name);
			cb.setSelection(model.selecteds.contains(name));
			GridDataFactory.fillDefaults().grab(true, false).applyTo(cb);
			cb.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleSelection();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					handleSelection();
				}
				
				private void handleSelection() {
					boolean add = cb.getSelection();
					if (add) {
						model.selecteds.add(name);
					} else {
						model.selecteds.remove(name);
					}
				}
			});
		}
	}

	private String[] options;
	private MultiSelectionModel<String> model;
	

	public CodeSetCheckBoxesSection(WizardPageWithSections owner, String[] options, MultiSelectionModel<String> model) {
		super(owner, "Code Sets", createSections(owner, options, model));
		this.model = model;
		this.options = options;
	}

	private static WizardPageSection[] createSections(WizardPageWithSections owner, String[] options, MultiSelectionModel<String> model) {
		WizardPageSection[] checkboxes = new WizardPageSection[options.length];
		for (int i = 0; i < checkboxes.length; i++) {
			checkboxes[i] = new CheckBox(owner, options[i], model);
		}
		return checkboxes;
	}

	@Override
	protected GridLayout createLayout() {
		return new GridLayout(2, true);
	}
	
	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return model.validator;
	}

}
