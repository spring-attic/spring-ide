/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
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
import org.springframework.ide.eclipse.web.flow.core.internal.model.Property;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class PropertiesComposite {
    
    private IPropertyEnabled state;

    private Button removeButton;
    
    private Button addButton;
    
    private TableViewer configsViewer;
    
    private IDialogValidator validator;
    
    public PropertiesComposite(IDialogValidator validator, TabItem item, Shell parentShell,IPropertyEnabled state) {
        this.state = state;
        item.setText("Properties");
        item.setToolTipText("Define element properties");
        item.setImage(WebFlowImages.getImage(WebFlowImages.IMG_OBJS_PROPERTIES));
    }
    
    
    protected Control createDialogArea(Composite parent) {
        Group groupPropertyType = new Group(parent, SWT.NULL);
        GridLayout layoutPropMap = new GridLayout();
        layoutPropMap.marginWidth = 3;
        layoutPropMap.marginHeight = 3;
        groupPropertyType.setLayout(layoutPropMap);
        groupPropertyType.setText(" Properties ");
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
        //data.widthHint = 250;
        data.heightHint = 50;
        configsTable.setLayoutData(data);
        TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
        columnName.setText("Name");
        columnName.setWidth(150);
        TableColumn columnValue = new TableColumn(configsTable, SWT.NONE);
        columnValue.setText("Value");
        columnValue.setWidth(220);
        configsTable.setHeaderVisible(true);

        configsViewer = new TableViewer(configsTable);
        String[] columnNames = new String[] { "Name", "Value" };
        configsViewer.setColumnProperties(columnNames);
        configsViewer.setContentProvider(new PropertiesContentProvider(
                this.state, configsViewer));
        CellEditor[] editors = new CellEditor[2];
        TextCellEditor textEditor = new TextCellEditor(configsViewer.getTable());
        TextCellEditor textEditor1 = new TextCellEditor(configsViewer
                .getTable());
        editors[0] = textEditor;
        editors[1] = textEditor1;
        configsViewer.setCellEditors(editors);
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

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                new Property(state, "<name>", "<value>");
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
                    if (selection.getFirstElement() instanceof IProperty) {
                        state.removeProperty((IProperty) selection
                                .getFirstElement());
                    }
                }
            }
        });
        removeButton.setEnabled(false);
        
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
        } else {
            removeButton.setEnabled(true);
        }
    }
}
