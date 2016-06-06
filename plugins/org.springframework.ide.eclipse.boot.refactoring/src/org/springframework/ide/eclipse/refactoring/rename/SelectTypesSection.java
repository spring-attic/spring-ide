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
package org.springframework.ide.eclipse.refactoring.rename;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import com.google.common.collect.ImmutableSet;

public class SelectTypesSection extends WizardPageSection {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private int jeLabelProviderFlags = JavaElementLabelProvider.SHOW_POST_QUALIFIED;

	private static void debug(String string) {
		System.out.println(string);
	}

	private final ObservableSet<IType> types;
	private final LiveSetVariable<IType> checked;

	public SelectTypesSection(IPageWithSections owner, ObservableSet<IType> types, LiveSetVariable<IType> checked) {
		super(owner);
		this.types = types;
		this.checked = checked;
	}

	@Override
	public void createContents(Composite page) {
		Composite parent = page;

		Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		table.setLayoutData(gd);
//		table.setHeaderVisible(true);
		table.setLinesVisible(true);

//		TableColumn column = new TableColumn(table, SWT.NULL);
//		column.setText("Type");

		//TODO: add a second column to display the new name of the type?

		CheckboxTableViewer tv = new CheckboxTableViewer(table);
		JavaElementLabelProvider jeLabels = new JavaElementLabelProvider(jeLabelProviderFlags);
		tv.setLabelProvider(jeLabels);
		tv.setContentProvider(new ArrayContentProvider());
		table.addDisposeListener((widget) -> jeLabels.dispose());

		tv.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				return checked.getValues().contains(element);
			}
		});

		tv.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object e = event.getElement();
				boolean isChecked = event.getChecked();
				if (e instanceof IType) {
					if (isChecked) {
						checked.add((IType) e);
					} else {
						checked.remove((IType) e);
					}
				}
			}
		});

		types.addListener(UIValueListener.from((e, ts) -> {
			if (tv!=null && !tv.getTable().isDisposed()) {
				tv.setInput(ts);
			}
		}));

		Composite buttonBar = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(buttonBar);
		button(buttonBar, "Select All", () -> {
			checked.replaceAll(types.getValues());
			tv.refresh(); //The TV isn't registered as a listener of 'checked' because it fires too many spurious events.
		});
		button(buttonBar, "Deselect All", () -> {
			checked.replaceAll(ImmutableSet.of());
			tv.refresh(); //The TV isn't registered as a listener of 'checked' because it fires too many spurious events.
		});

	}

	private Button button(Composite parent, String label, Runnable clickHandler) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clickHandler.run();
			}
		});
		GridDataFactory.fillDefaults().applyTo(button);
		return button;
	}

}
