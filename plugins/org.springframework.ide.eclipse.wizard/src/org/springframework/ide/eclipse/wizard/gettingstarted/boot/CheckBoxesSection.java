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

import java.util.List;

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
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * A section that shows a series of checkboxes in rows and columns.
 * <p>
 * The section has an optional label. If the label is provided then
 * the section will be created as a labeled group with a bounding border
 * around it. Otherwise a plain composite will be used to host the
 * rows/columns of checkboxes.
 *
 * @author Kris De Volder
 */
public class CheckBoxesSection<T> extends WizardPageSection {

	public static class CheckBoxModel<T> {
		private final String label;
		private final LiveVariable<Boolean> selection;
		private final LiveExpression<Boolean> enablement;
		private final T value;
		private String tooltip = null; //optional tooltip
		public CheckBoxModel(String label, T value, LiveVariable<Boolean> selection, LiveExpression<Boolean> enablement) {
			this.label = label;
			this.value = value;
			this.selection = selection;
			this.enablement = enablement;
			if (label.contains("DevTools")) {
				System.out.println("DevTools selection = "+selection);
			}
		}
		public String getLabel() {
			return label;
		}
		public LiveVariable<Boolean> getSelection() {
			return selection;
		}
		public LiveExpression<Boolean> getEnablement() {
			return enablement;
		}
		public String getTooltip() {
			return tooltip;
		}
		public void setTooltip(String tooltip) {
			this.tooltip = tooltip;
		}
		public T getValue() {
			return value;
		}
		@Override
		public String toString() {
			return "CheckBox("+label+", "+getSelection().getValue()+")" ;
		}
	}

	private final List<CheckBoxModel<T>> model;
	private Composite composite;
	private WizardPageSection[] subsections;
	private int numCols;
	private String label;

	public CheckBoxesSection(IPageWithSections owner, List<CheckBoxModel<T>> model, String label) {
		super(owner);
		this.label = label;
		this.model = model;
	}

	public CheckBoxesSection(IPageWithSections owner, List<CheckBoxModel<T>> model) {
		this(owner, model, /*label*/null);
	}

	public CheckBoxesSection<T> columns(int howMany) {
		Assert.isLegal(howMany>0);
		this.numCols = howMany;
		return this;
	}

	private static class CheckBox<T> extends WizardPageSection {

		private Button cb;
		private CheckBoxModel<T> model;

		public CheckBox(IPageWithSections owner, CheckBoxModel<T> model) {
			super(owner);
			this.model = model;
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return Validator.OK;
		}

		@Override
		public void createContents(Composite page) {
			if (page!=null && !page.isDisposed()) {
				this.cb = new Button(page, SWT.CHECK);
				cb.setText(model.getLabel());
				String tooltip = model.getTooltip();
				if (tooltip!=null) {
					cb.setToolTipText(tooltip);
				}
				model.getSelection().addListener(new ValueListener<Boolean>() {
					public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
						if (value!=null) {
							cb.setSelection(value);
						}
					}
				});
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
						model.getSelection().setValue(cb.getSelection());
					}
				});
				LiveExpression<Boolean> enablement = model.getEnablement();
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

//	@Override
//	public LiveExpression<ValidationResult> getValidator() {
//		return model.getValidator();
//	}

	@Override
	public void createContents(Composite page) {
		composite = createComposite(page);
		GridLayout layout = createLayout();
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		subsections = new WizardPageSection[Math.max(1, model.size())];

//				GridData gd = (GridData) group.getLayoutData();
//				boolean visible = checkboxes.length>0;
//				gd.exclude = !visible;

		if (model.isEmpty()) {
			//don't leave section empty it looks ugly
			subsections[0] = new CommentSection(owner, "No choices available");
			subsections[0].createContents(composite);
		}

		for (int i = 0; i < model.size(); i++) {
			subsections[i] = new CheckBox(owner, model.get(i));
			subsections[i].createContents(composite);
		}
	}

	protected Composite createComposite(Composite page) {
		if (this.label!=null) {
			Group comp = new Group(page, SWT.NONE);
			comp.setText(label);
			return comp;
		} else {
			return new Composite(page, SWT.NONE);
		}
	}

}
