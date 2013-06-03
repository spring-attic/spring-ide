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
package org.springframework.ide.gettingstarted.guides.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * Wizard section to choose one element from list of elements. Uses a table viewer to allow selecting 
 * an element.
 * <p>
 * This class is very similar in functionality (from client's point of view) to {@link ChooseOneSectionCombo}.
 * It should be possible to use either one of these classes as functional replacements for one another.
 */
public class ChooseOneSectionTable<T> extends ChooseOneSection {

	private SelectionModel<T> selection;
	private String label; //Descriptive Label for this section
	private T[] options; //The elements to choose from

	public ChooseOneSectionTable(IPageWithSections owner, String label, SelectionModel<T> selection, T[] options) {
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
		
		final TableViewer tv = new TableViewer(field, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
		tv.setLabelProvider(labelProvider);
		tv.setContentProvider(ArrayContentProvider.getInstance());
		tv.setInput(options);

		
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fieldNameLabel);
		GridDataFactory grabHor = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 150);
		grabHor.applyTo(field);
		grabHor.applyTo(tv.getTable());
		
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked") @Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = tv.getSelection();
				if (sel.isEmpty()) {
					selection.selection.setValue(null);
				} else if (sel instanceof IStructuredSelection){
					IStructuredSelection ss = (IStructuredSelection) sel;
					selection.selection.setValue((T)ss.getFirstElement());
				}
			}
		});
	}

//	private String[] getLabels() {
//		String[] labels = new String[options.length]; 
//		for (int i = 0; i < labels.length; i++) {
//			labels[i] = labelProvider.getText(options[i]);
//		}
//		return labels;
//	}

}
