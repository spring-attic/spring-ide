/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kaitlin Duck Sherwood
 */

// modified from ConfiguratorPreference, with some help from
// RuntimePreferencePage and SpringConfigPreferencePage
public abstract class AbstractNameUrlPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Table table;

	protected TableViewer tableViewer;

	List<NameUrlPair> elements;

	protected AbstractNameUrlPreferenceModel model;

	private Button editButton;

	private Button removeButton;

	private Button optionalCheckbox;

	protected abstract String preferencePageHeaderText();

	protected abstract AbstractNameUrlPreferenceModel getModel();

	protected abstract boolean shouldShowCheckbox();

	protected abstract String checkboxLabel();

	protected abstract String addDialogHeaderText();

	protected abstract AddEditNameUrlDialog getAddEditDialog(NameUrlPair existingNameUrlPair);

	public void init(IWorkbench workbench) {
		setPreferenceStore(doGetPreferenceStore());
		model = getModel();
	}

	@Override
	public boolean performOk() {
		boolean okay = super.performOk();
		model.persist();
		return okay;
	}

	@Override
	public boolean performCancel() {
		model.revert();
		return true;
	}

	private NameUrlPair getSelectedNameUrlPair() {
		StructuredSelection selectedTuple = (StructuredSelection) tableViewer.getSelection();
		NameUrlPair pair = (NameUrlPair) selectedTuple.getFirstElement();
		return pair;
	}

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
		layout.verticalSpacing = convertVerticalDLUsToPixels(3);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(preferencePageHeaderText());

		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(table);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table); // was LockedTableViewer
		tableViewer.setContentProvider(new NameUrlContentProvider());
		tableViewer.setSorter(new NameUrlViewerSorter(true));

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.FILL);
		TableColumn swtNameColumn = nameColumn.getColumn();
		swtNameColumn.setText("name");
		swtNameColumn.setWidth(50);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				org.springframework.ide.eclipse.wizard.template.util.NameUrlPair nameUrlPair = (NameUrlPair) element;
				return nameUrlPair.getName();
			}
		});

		nameColumn.getColumn().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new NameUrlViewerSorter(true));
				removeButton.setEnabled(false);
				editButton.setEnabled(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		TableViewerColumn urlColumn = new TableViewerColumn(tableViewer, SWT.FILL);
		urlColumn.getColumn().setText("URL");
		urlColumn.getColumn().setWidth(160);

		urlColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				org.springframework.ide.eclipse.wizard.template.util.NameUrlPair nameUrlPair = (NameUrlPair) element;
				return nameUrlPair.getUrlString();
			}
		});
		urlColumn.getColumn().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new NameUrlViewerSorter(false));
				removeButton.setEnabled(false);
				editButton.setEnabled(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		tableViewer.setInput(model);
		tableViewer.setColumnProperties(new String[] { "Name", "URL" });

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(true);
				editButton.setEnabled(true);
			}
		});

		// after adding an item do the packing of the table
		if (table.getItemCount() > 0) {
			TableColumn[] columns = table.getColumns();
			for (TableColumn column : columns) {
				column.pack();
			}
			table.pack();
		}

		Composite buttonComp = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, convertVerticalDLUsToPixels(3))
				.applyTo(buttonComp);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(buttonComp);

		Button addButton = new Button(buttonComp, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addButton);
		addButton.setText(NLS.bind("Add", null));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				AddEditNameUrlDialog dialog = getAddEditDialog(null);
				if (dialog.open() == Dialog.OK) {
					String urlString = dialog.getUrlString().trim().replaceAll("\\n", "");
					if (urlString.endsWith("/")) {
						urlString = urlString.substring(0, urlString.length() - 1);
					}
					// the validation of the URL is done in the dialog class
					String name = dialog.getName();

					if (name.length() > 0 && urlString.length() > 0) {
						try {
							model.addNameUrlPairInEncodedString(new NameUrlPair(name, urlString));
							tableViewer.refresh();
						}
						catch (URISyntaxException e) {
							// Errors in URL *should* be caught in the add/edit
							// dialog, i.e. before getting to this point.
							String message = NLS.bind(AddEditNameUrlDialogMessages.malformedUrlIgnoring, urlString);
							MessageDialog.openError(getShell(), AddEditNameUrlDialogMessages.malformedUrl, message);
						}
					}
				}
			}

		});

		editButton = new Button(buttonComp, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(editButton);
		editButton.setText(NLS.bind("Edit", null));
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				NameUrlPair oldNameUrl = getSelectedNameUrlPair();
				AddEditNameUrlDialog dialog = getAddEditDialog(oldNameUrl);
				if (dialog.open() == Dialog.OK) {
					String urlString = dialog.getUrlString().trim().replaceAll("\\n", "");
					if (urlString.endsWith("/")) {
						urlString = urlString.substring(0, urlString.length() - 1);
					}
					String name = dialog.getName();

					if (name.length() > 0 && urlString.length() > 0) {
						try {
							if (oldNameUrl != null) {
								model.removeNameUrlPairInEncodedString(oldNameUrl);
							}
							model.addNameUrlPairInEncodedString(new NameUrlPair(name, urlString));
							tableViewer.refresh();
							// the tableViewer refresh deselects the line,
							// so disable the buttons
							editButton.setEnabled(false);
							removeButton.setEnabled(false);
						}
						catch (URISyntaxException e) {
							// Errors in URL *should* be caught in the add/edit
							// dialog, i.e. before getting to this point.
							String message = NLS.bind(AddEditNameUrlDialogMessages.malformedUrlIgnoring, urlString);
							MessageDialog.openError(getShell(), AddEditNameUrlDialogMessages.malformedUrl, message);
						}
					}
				}
			}
		});

		removeButton = new Button(buttonComp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
		removeButton.setText(NLS.bind("Remove", null));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selectedTuple = (StructuredSelection) tableViewer.getSelection();
				NameUrlPair pair = (NameUrlPair) selectedTuple.getFirstElement(); // unencoded
				NameUrlPair encodedPair;
				try {
					encodedPair = new NameUrlPair(pair.getName(), pair.getUrlString());

					String title = NLS.bind("Are you sure?", null);
					String message = NLS.bind(
							"If you continue, {0} will be removed.  Are you sure you want to remove {0}?",
							pair.getName());
					boolean answer = MessageDialog.openQuestion(getShell(), title, message);

					if (answer) {
						model.removeNameUrlPairInEncodedString(encodedPair);
						tableViewer.remove(selectedTuple);
						tableViewer.refresh();
						editButton.setEnabled(false);
						removeButton.setEnabled(false);
					}
				}
				catch (URISyntaxException e1) {
					// There should be no errors; checking happened on input
					String errorTitle = NLS.bind("Internal error", null);
					String errorMessage = NLS
							.bind("This error was not expected.  URL {0} was malformed when it was retrieved from the preferences properties",
									pair.getUrlString());
					;
					MessageDialog.openError(getShell(), errorTitle, errorMessage);

				}
			}

		});

		if (shouldShowCheckbox()) {
			optionalCheckbox = new Button(parent, SWT.CHECK | SWT.WRAP | SWT.BOTTOM);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(optionalCheckbox);
			optionalCheckbox.setText(checkboxLabel());
			optionalCheckbox.setEnabled(true);
			optionalCheckbox.setSelection(model.getOptionalFlagValue());
			optionalCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					model.setOptionalFlagValue(optionalCheckbox.getSelection());
				}
			});
		}

		Dialog.applyDialogFont(composite);

		return composite;
	}

	@Override
	protected void performDefaults() {
		model.clearNonDefaults();
		tableViewer.refresh();
	}

	protected void updateSelection(ISelection selection) {
	}

	protected boolean getCheckboxValue() {
		return optionalCheckbox.getSelection();
	}

}
