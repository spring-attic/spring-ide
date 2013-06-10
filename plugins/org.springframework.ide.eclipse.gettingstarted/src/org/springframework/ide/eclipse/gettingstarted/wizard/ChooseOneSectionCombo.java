/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * Wizard section to choose one element from list of elements. Uses a pulldown Combo box to allow selecting 
 * an element.
 */
public class ChooseOneSectionCombo<T> extends ChooseOneSection {

	private SelectionModel<T> selection;
	private String label; //Descriptive Label for this section
	private T[] options; //The elements to choose from

	public ChooseOneSectionCombo(IPageWithSections owner, String label, SelectionModel<T> selection, T[] options) {
		super(owner);
		this.label = label;
		this.selection = selection;
		this.options = options;
	}
	
	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}

	@Override
	public void createContents(Composite page) {
		Composite field = new Composite(page, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).create();
		field.setLayout(layout);
		Label fieldNameLabel = new Label(field, SWT.NONE);
		fieldNameLabel.setText(label);
		
		final Combo combo = new Combo(field, SWT.READ_ONLY);
		combo.setItems(getLabels());
		T preselect = selection.selection.getValue();
		if (preselect!=null) {
			combo.setText(labelProvider.getText(preselect));
		}
		GridDataFactory.fillDefaults().applyTo(combo);
		
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				int selected = combo.getSelectionIndex();
				if (selected>=0) {
					selection.selection.setValue(options[selected]);
				} else {
					selection.selection.setValue(null);
				}
			}
		});
		
	}

	private String[] getLabels() {
		String[] labels = new String[options.length]; 
		for (int i = 0; i < labels.length; i++) {
			labels[i] = labelProvider.getText(options[i]);
		}
		return labels;
	}

}
