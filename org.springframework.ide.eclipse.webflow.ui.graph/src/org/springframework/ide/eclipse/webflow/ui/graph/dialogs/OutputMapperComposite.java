/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.springframework.ide.eclipse.webflow.core.internal.model.Mapping;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("unused")
public class OutputMapperComposite {

	private class OutputMapperContentProvider implements IStructuredContentProvider {

		private List<IOutputAttribute> actions;

		public OutputMapperContentProvider(List<IOutputAttribute> actions) {
			this.actions = actions;
		}

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	private class MappingContentProvider implements IStructuredContentProvider {

		private List<IMapping> actions;

		public MappingContentProvider(List<IMapping> actions) {
			this.actions = actions;
		}

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	private IOutputMapper state;

	private List<IOutputAttribute> outputAttributes;

	private Button attributeRemoveButton;

	private Button attributeAddButton;

	private Button attributeEditButton;

	private Button mappingRemoveButton;

	private Button mappingAddButton;

	private Button mappingEditButton;

	private TableViewer attributeViewer;

	private TableViewer mappingViewer;

	private IDialogValidator validator;

	private Shell parentShell;

	private List<IMapping> mappings;

	public OutputMapperComposite(IDialogValidator validator, TabItem item, Shell parentShell,
			List<IOutputAttribute> outputAttributes, List<IMapping> mappings, IOutputMapper state) {
		this.state = state;
		this.outputAttributes = outputAttributes;
		this.mappings = mappings;
		item.setText("Output Mapping");
		item.setToolTipText("Define element's output mapping");
		item.setImage(WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_OUTPUT));
		this.parentShell = parentShell;
	}

	protected Control createDialogArea(Composite superParent) {
		Composite parent = new Composite(superParent, SWT.NULL);
		GridLayout layoutPropMap = new GridLayout();
		layoutPropMap.marginWidth = 0;
		layoutPropMap.marginHeight = 0;
		parent.setLayout(layoutPropMap);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupPropertyType = new Group(parent, SWT.NULL);
		layoutPropMap = new GridLayout();
		layoutPropMap.marginWidth = 3;
		layoutPropMap.marginHeight = 3;
		groupPropertyType.setLayout(layoutPropMap);
		groupPropertyType.setText(" Output Attributes ");
		groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(groupPropertyType, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		tableAndButtons.setLayout(layout2);

		Table configsTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		// data.widthHint = 250;
		data.heightHint = 75;
		configsTable.setLayoutData(data);
		TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
		columnName.setText("Name");
		columnName.setWidth(150);
		TableColumn columnValue = new TableColumn(configsTable, SWT.NONE);
		columnValue.setText("Scope");
		columnValue.setWidth(120);
		TableColumn columnType = new TableColumn(configsTable, SWT.NONE);
		columnType.setText("Required");
		columnType.setWidth(80);
		configsTable.setHeaderVisible(true);

		attributeViewer = new TableViewer(configsTable);
		String[] columnNames = new String[] { "Name", "Value", "Required" };
		attributeViewer.setColumnProperties(columnNames);
		attributeViewer.setContentProvider(new OutputMapperContentProvider(this.outputAttributes));
		attributeViewer.setCellEditors(new org.eclipse.jface.viewers.CellEditor[0]);		
		attributeViewer.setLabelProvider(new ModelTableLabelProvider());
		attributeViewer.setCellModifier(new TableCellModifier());
		attributeViewer.setInput(this.state);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleAttributeTableSelectionChanged();
			}
		});
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		attributeAddButton = new Button(buttonArea, SWT.PUSH);
		attributeAddButton.setText("Add");
		GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		attributeAddButton.setLayoutData(data1);
		attributeAddButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				OutputAttribute attr = new OutputAttribute();
				attr.createNew(state);
				if (DialogUtils.openPropertiesDialog(state, attr, true) == Dialog.OK) {
					outputAttributes.add(attr);
					attributeViewer.refresh();
				}
			}
		});
		attributeEditButton = new Button(buttonArea, SWT.PUSH);
		attributeEditButton.setText("Edit");
		data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		attributeEditButton.setLayoutData(data1);
		attributeEditButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) attributeViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IOutputAttribute) {
						if (DialogUtils.openPropertiesDialog(state, (IOutputAttribute) selection
								.getFirstElement(), true) == Dialog.OK) {
							attributeViewer.refresh(true);
						}
					}
				}
			}
		});

		attributeRemoveButton = new Button(buttonArea, SWT.PUSH);
		attributeRemoveButton.setText("Delete");
		GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		attributeRemoveButton.setLayoutData(data2);
		attributeRemoveButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) attributeViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IOutputAttribute) {
						outputAttributes.remove(selection.getFirstElement());
						attributeViewer.refresh(true);
					}
				}
			}
		});
		attributeRemoveButton.setEnabled(false);
		attributeEditButton.setEnabled(false);

		if (WebflowModelXmlUtils.isVersion1Flow(state)) {

			Group groupPropertyType2 = new Group(parent, SWT.NULL);
			layoutPropMap = new GridLayout();
			layoutPropMap.marginWidth = 3;
			layoutPropMap.marginHeight = 3;
			groupPropertyType2.setLayout(layoutPropMap);
			groupPropertyType2.setText(" Mapping ");
			groupPropertyType2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Composite tableAndButtons2 = new Composite(groupPropertyType2, SWT.NONE);
			tableAndButtons2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout2 = new GridLayout();
			layout2.marginHeight = 0;
			layout2.marginWidth = 0;
			layout2.numColumns = 2;
			tableAndButtons2.setLayout(layout2);

			Table mappingConfigsTable = new Table(tableAndButtons2, SWT.MULTI | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
			data = new GridData(GridData.FILL_BOTH);
			// data.widthHint = 250;
			data.heightHint = 75;
			mappingConfigsTable.setLayoutData(data);
			TableColumn mcolumnName = new TableColumn(mappingConfigsTable, SWT.NONE);
			mcolumnName.setText("Source");
			mcolumnName.setWidth(250);
			mappingConfigsTable.setHeaderVisible(true);

			mappingViewer = new TableViewer(mappingConfigsTable);
			String[] mcolumnNames = new String[] { "Source" };
			mappingViewer.setColumnProperties(mcolumnNames);
			mappingViewer.setContentProvider(new MappingContentProvider(this.mappings));
			mappingViewer.setCellEditors(new org.eclipse.jface.viewers.CellEditor[0]);
			mappingViewer.setLabelProvider(new ModelTableLabelProvider());
			mappingViewer.setCellModifier(new TableCellModifier());
			mappingViewer.setInput(this.state);
			mappingConfigsTable.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					handleMappingTableSelectionChanged();
				}
			});
			Composite buttonArea2 = new Composite(tableAndButtons2, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			buttonArea2.setLayout(layout);
			buttonArea2.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			mappingAddButton = new Button(buttonArea2, SWT.PUSH);
			mappingAddButton.setText("Add");
			data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			mappingAddButton.setLayoutData(data1);
			mappingAddButton.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					Mapping attr = new Mapping();
					attr.createNew(state);
					if (DialogUtils.openPropertiesDialog(state, attr, true) == Dialog.OK) {
						mappings.add(attr);
						mappingViewer.refresh(true);
					}
				}
			});
			mappingEditButton = new Button(buttonArea2, SWT.PUSH);
			mappingEditButton.setText("Edit");
			data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			mappingEditButton.setLayoutData(data1);
			mappingEditButton.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) mappingViewer
							.getSelection();
					if (selection.getFirstElement() != null) {
						if (selection.getFirstElement() instanceof IMapping) {
							if (DialogUtils.openPropertiesDialog(state, (IMapping) selection
									.getFirstElement(), false) == Dialog.OK) {
								mappingViewer.refresh(true);
							}
						}
					}
				}
			});

			mappingRemoveButton = new Button(buttonArea2, SWT.PUSH);
			mappingRemoveButton.setText("Delete");
			data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			mappingRemoveButton.setLayoutData(data2);
			mappingRemoveButton.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) mappingViewer
							.getSelection();
					if (selection.getFirstElement() != null) {
						if (selection.getFirstElement() instanceof IMapping) {
							mappings.remove(selection.getFirstElement());
							mappingViewer.refresh(true);
						}
					}
				}
			});
			mappingRemoveButton.setEnabled(false);
			mappingEditButton.setEnabled(false);
		}
		return parent;
	}

	private void handleAttributeTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) attributeViewer.getSelection();
		if (selection.isEmpty()) {
			attributeRemoveButton.setEnabled(false);
			attributeEditButton.setEnabled(false);
		}
		else {
			attributeRemoveButton.setEnabled(true);
			attributeEditButton.setEnabled(true);
		}
	}

	private void handleMappingTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) mappingViewer.getSelection();
		if (selection.isEmpty()) {
			mappingRemoveButton.setEnabled(false);
			mappingEditButton.setEnabled(false);
		}
		else {
			mappingRemoveButton.setEnabled(true);
			mappingEditButton.setEnabled(true);
		}
	}
}
