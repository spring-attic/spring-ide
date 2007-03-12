/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * 
 */
@SuppressWarnings("unused")
public class OutputMapperComposite {

	/**
	 * 
	 */
	private class OutputMapperContentProvider implements
			IStructuredContentProvider {

		/**
		 * 
		 */
		private List<IOutputAttribute> actions;

		/**
		 * 
		 * 
		 * @param actions 
		 */
		public OutputMapperContentProvider(List<IOutputAttribute> actions) {
			this.actions = actions;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	/**
	 * 
	 */
	private class MappingContentProvider implements IStructuredContentProvider {

		/**
		 * 
		 */
		private List<IMapping> actions;

		/**
		 * 
		 * 
		 * @param actions 
		 */
		public MappingContentProvider(List<IMapping> actions) {
			this.actions = actions;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	/**
	 * 
	 */
	private IOutputMapper state;

	/**
	 * 
	 */
	private List<IOutputAttribute> outputAttributes;

	/**
	 * 
	 */
	private Button attributeRemoveButton;

	/**
	 * 
	 */
	private Button attributeAddButton;

	/**
	 * 
	 */
	private Button attributeEditButton;

	/**
	 * 
	 */
	private Button mappingRemoveButton;

	/**
	 * 
	 */
	private Button mappingAddButton;

	/**
	 * 
	 */
	private Button mappingEditButton;

	/**
	 * 
	 */
	private TableViewer attributeViewer;

	/**
	 * 
	 */
	private TableViewer mappingViewer;

	/**
	 * 
	 */
	private IDialogValidator validator;

	/**
	 * 
	 */
	private Shell parentShell;

	/**
	 * 
	 */
	private List<IMapping> mappings;

	/**
	 * 
	 * 
	 * @param item 
	 * @param outputAttributes 
	 * @param parentShell 
	 * @param validator 
	 * @param state 
	 * @param mappings 
	 */
	public OutputMapperComposite(IDialogValidator validator, TabItem item,
			Shell parentShell, List<IOutputAttribute> outputAttributes,
			List<IMapping> mappings, IOutputMapper state) {
		this.state = state;
		this.outputAttributes = outputAttributes;
		this.mappings = mappings;
		item.setText("Output Mapper");
		item.setToolTipText("Define element's output mapper");
		item
				.setImage(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_OUTPUT));
		this.parentShell = parentShell;
	}

	/**
	 * 
	 * 
	 * @param superParent 
	 * 
	 * @return 
	 */
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

		Table configsTable = new Table(tableAndButtons, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
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
		attributeViewer.setContentProvider(new OutputMapperContentProvider(
				this.outputAttributes));

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
		data1.widthHint = 40;
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
		data1.widthHint = 40;
		attributeEditButton.setLayoutData(data1);
		attributeEditButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) attributeViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IOutputAttribute) {
						if (DialogUtils.openPropertiesDialog(state,
								(IOutputAttribute) selection.getFirstElement(),
								true) == Dialog.OK) {
							attributeViewer.refresh(true);
						}
					}
				}
			}
		});

		attributeRemoveButton = new Button(buttonArea, SWT.PUSH);
		attributeRemoveButton.setText("Delete");
		GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data2.widthHint = 40;
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

		Group groupPropertyType2 = new Group(parent, SWT.NULL);
		layoutPropMap = new GridLayout();
		layoutPropMap.marginWidth = 3;
		layoutPropMap.marginHeight = 3;
		groupPropertyType2.setLayout(layoutPropMap);
		groupPropertyType2.setText(" Mapping ");
		groupPropertyType2
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons2 = new Composite(groupPropertyType2, SWT.NONE);
		tableAndButtons2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		tableAndButtons2.setLayout(layout2);

		Table mappingConfigsTable = new Table(tableAndButtons2, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
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
		mappingViewer.setContentProvider(new MappingContentProvider(
				this.mappings));

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
		data1.widthHint = 40;
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
		data1.widthHint = 40;
		mappingEditButton.setLayoutData(data1);
		mappingEditButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) mappingViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IMapping) {
						if (DialogUtils.openPropertiesDialog(state,
								(IMapping) selection.getFirstElement(), false) == Dialog.OK) {
							mappingViewer.refresh(true);
						}
					}
				}
			}
		});

		mappingRemoveButton = new Button(buttonArea2, SWT.PUSH);
		mappingRemoveButton.setText("Delete");
		data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data2.widthHint = 40;
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

		return parent;
	}

	/**
	 * The user has selected a different configuration in table. Update button
	 * enablement.
	 */
	private void handleAttributeTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) attributeViewer
				.getSelection();
		if (selection.isEmpty()) {
			attributeRemoveButton.setEnabled(false);
			attributeEditButton.setEnabled(false);
		}
		else {
			attributeRemoveButton.setEnabled(true);
			attributeEditButton.setEnabled(true);
		}
	}

	/**
	 * 
	 */
	private void handleMappingTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) mappingViewer
				.getSelection();
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
