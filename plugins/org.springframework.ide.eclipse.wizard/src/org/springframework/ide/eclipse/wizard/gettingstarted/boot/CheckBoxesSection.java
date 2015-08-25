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
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * @author Kris De Volder
 */
public class CheckBoxesSection<T> extends WizardPageSection {

	private final MultiSelectionFieldModel<T> model;
	private Composite composite;
	private WizardPageSection[] subsections;
	private int numCols;
	private String label;

	public CheckBoxesSection(IPageWithSections owner, MultiSelectionFieldModel<T> model, String label) {
		super(owner);
		this.model = model;
		this.label = label;
	}

	public CheckBoxesSection(IPageWithSections owner, MultiSelectionFieldModel<T> model) {
		this(owner, model, /*label*/null);
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
		private final String tooltip;
		private LiveExpression<Boolean> enablement;

		public CheckBox(IPageWithSections owner, T value, String label, String tooltip, LiveExpression<Boolean> enablement) {
			this(owner, value, label, tooltip);
			this.enablement = enablement;
		}
		public CheckBox(IPageWithSections owner, T value, String label, String tooltip) {
			super(owner);
			this.value = value;
			this.label = label;
			this.tooltip = tooltip;
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
				if (tooltip!=null) {
					cb.setToolTipText(tooltip);
				}
				cb.setSelection(model.getSelection(value).getValue());
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
							model.select(value);
						} else {
							model.unselect(value);
						}
					}
				});
				if (enablement!=null) {
					enablement.addListener(new ValueListener<Boolean>() {
						public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
							if (value!=null) {
								cb.setEnabled(value);
							}
						}
					});
				}
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
		composite = createComposite(page);
		GridLayout layout = createLayout();
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		T[] options = model.getChoices();

		subsections = new WizardPageSection[Math.max(1, options.length)];

//				GridData gd = (GridData) group.getLayoutData();
//				boolean visible = checkboxes.length>0;
//				gd.exclude = !visible;

		if (options.length==0) {
			//don't leave section empty it looks ugly
			subsections[0] = new CommentSection(owner, "No choices available");
			subsections[0].createContents(composite);
		}

		for (int i = 0; i < options.length; i++) {
			T option = options[i];
			subsections[i] = new CheckBox(owner, option, model.getLabel(option), model.getTooltip(option), model.getEnablement(option));
			subsections[i].createContents(composite);
		}
	}

	protected Composite createComposite(Composite page) {
		if (this.label!=null) {
			Group comp = new Group(page, SWT.NONE);
			comp.setText(model.getLabel());
			return comp;
		} else {
			return new Composite(page, SWT.NONE);
		}
	}

}
