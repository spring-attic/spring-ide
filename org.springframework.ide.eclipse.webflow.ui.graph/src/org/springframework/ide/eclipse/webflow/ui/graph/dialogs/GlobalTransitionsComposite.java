/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.internal.model.Attribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.Import;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.Variable;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * 
 */
@SuppressWarnings("unused")
public class GlobalTransitionsComposite {

	/**
	 * 
	 */
	private class GlobalTransitionsContentProvider implements
			IStructuredContentProvider {

		/**
		 * 
		 */
		private List<IStateTransition> actions;

		/**
		 * @param actions
		 */
		public GlobalTransitionsContentProvider(List<IStateTransition> actions) {
			this.actions = actions;
		}

		/**
		 * 
		 */
		public void dispose() {
		}

		/**
		 * @param obj
		 * @return
		 */
		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		/**
		 * @param arg1
		 * @param arg0
		 * @param arg2
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	/**
	 * 
	 */
	private List<IStateTransition> transitions;

	/**
	 * 
	 */
	private IWebflowModelElement parentElement;

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

	private IWebflowState webflowState;

	/**
	 * @param item
	 * @param parentShell
	 * @param validator
	 * @param state
	 */
	public GlobalTransitionsComposite(IDialogValidator validator, TabItem item,
			Shell parentShell, List<IStateTransition> transitions,
			IWebflowModelElement parentElement, IWebflowState webflowState) {
		this.transitions = transitions;
		this.parentElement = parentElement;
		this.webflowState = webflowState;
		item.setText("Global Transitions");
		item.setToolTipText("Define global Transitions");
		item.setImage(WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_TRANSITION));
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
		groupPropertyType.setText(" Global Transitions ");
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
		TableColumn columnOn = new TableColumn(configsTable, SWT.NONE);
		columnOn.setText("On");
		columnOn.setWidth(150);
		TableColumn columnTo = new TableColumn(configsTable, SWT.NONE);
		columnTo.setText("To");
		columnTo.setWidth(150);
		TableColumn columnOnException = new TableColumn(configsTable, SWT.NONE);
		columnOnException.setText("On-Exception");
		columnOnException.setWidth(150);
		configsTable.setHeaderVisible(true);

		configsViewer = new TableViewer(configsTable);
		String[] columnNames = new String[] { "On", "To", "On-Exception" };
		configsViewer.setColumnProperties(columnNames);
		configsViewer.setContentProvider(new GlobalTransitionsContentProvider(
				this.transitions));

		configsViewer.setLabelProvider(new ModelTableLabelProvider());
		configsViewer.setCellModifier(new TableCellModifier());
		configsViewer.setInput(this.transitions);
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
				IStateTransition trans = new StateTransition();
				trans.createNew(parentElement,
						webflowState);
				StateTransitionPropertiesDialog dialog = new StateTransitionPropertiesDialog(
						parentShell, parentElement, trans, true);
				if (dialog.open() == Dialog.OK) {
					transitions.add(trans);
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
					if (selection.getFirstElement() instanceof IStateTransition) {
						StateTransitionPropertiesDialog dialog = new StateTransitionPropertiesDialog(
								parentShell, parentElement,
								(IStateTransition) selection.getFirstElement(), true);
						dialog.open();
						configsViewer.refresh();
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
					if (selection.getFirstElement() instanceof IStateTransition) {
						transitions.remove(selection.getFirstElement());
						configsViewer.refresh();
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
