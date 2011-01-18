/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.springframework.ide.eclipse.webflow.core.internal.model.Attribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.Import;
import org.springframework.ide.eclipse.webflow.core.internal.model.Variable;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("unused")
public class ImportComposite {

	/**
	 * 
	 */
	private IWebflowState state;

	/**
	 * 
	 */
	private Button removeButton;

	/**
	 * 
	 */
	private Button addButton;

	/**
	 * 
	 */
	private Button editButton;

	/**
	 * 
	 */
	private TableViewer configsViewer;

	/**
	 * 
	 */
	private IDialogValidator validator;

	/**
	 * 
	 */
	private Shell parentShell;

	/**
	 * @param item
	 * @param parentShell
	 * @param validator
	 * @param state
	 */
	public ImportComposite(IDialogValidator validator, TabItem item,
			Shell parentShell, IWebflowState state) {
		this.state = state;
		item.setText("Imports");
		item.setToolTipText("Define imports");
		item
				.setImage(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_IMPORT));
		this.parentShell = parentShell;
	}

	/**
	 * @param parent
	 * @return
	 */
	protected Control createDialogArea(Composite parent) {
		Group groupPropertyType = new Group(parent, SWT.NULL);
		GridLayout layoutPropMap = new GridLayout();
		layoutPropMap.marginWidth = 3;
		layoutPropMap.marginHeight = 3;
		groupPropertyType.setLayout(layoutPropMap);
		groupPropertyType.setText(" Bean Imports ");
		groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(groupPropertyType, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		tableAndButtons.setLayout(layout2);

		Table configsTable = new Table(tableAndButtons, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 250;
		data.heightHint = 150;
		configsTable.setLayoutData(data);
		TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
		columnName.setText("Resource");
		columnName.setWidth(150);
		configsTable.setHeaderVisible(true);

		configsViewer = new TableViewer(configsTable);
		String[] columnNames = new String[] { "Resource" };
		configsViewer.setColumnProperties(columnNames);
		configsViewer.setContentProvider(new ImportContentProvider(this.state,
				configsViewer));

		configsViewer.setLabelProvider(new ModelTableLabelProvider());
		configsViewer.setCellModifier(new TableCellModifier());
		configsViewer.setInput(this.state);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = new Button(buttonArea, SWT.PUSH);
		addButton.setText("Add");
		GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		addButton.setLayoutData(data1);
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IImport property = new Import();
				property.createNew(state);
				ImportEditorDialog dialog = new ImportEditorDialog(parentShell,
						property);
				if (dialog.open() == Dialog.OK) {
					state.addImport(property);
					configsViewer.refresh(true);
				}
			}
		});
		editButton = new Button(buttonArea, SWT.PUSH);
		editButton.setText("Edit");
		data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		editButton.setLayoutData(data1);
		editButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IImport) {
						ImportEditorDialog dialog = new ImportEditorDialog(
								parentShell, (IImport) selection.getFirstElement());
						dialog.open();
						configsViewer.refresh(true);
					}
				}
			}
		});

		removeButton = new Button(buttonArea, SWT.PUSH);
		removeButton.setText("Delete");
		GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		removeButton.setLayoutData(data2);
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IImport) {
						state.removeImport((IImport) selection.getFirstElement());
					}
				}
			}
		});
		removeButton.setEnabled(false);
		editButton.setEnabled(false);

		return groupPropertyType;
	}

	/**
	 * The user has selected a different configuration in table. Update button
	 * enablement.
	 */
	private void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		}
		else {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		}
	}
}
