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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
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
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * 
 */
public class ExceptionHandlerComposite {

	/**
	 * 
	 */
	private class ExceptionHandlerContentProvider implements
			IStructuredContentProvider {

		/**
		 * 
		 */
		private List<IExceptionHandler> actions;

		/**
		 * 
		 * 
		 * @param actions 
		 */
		public ExceptionHandlerContentProvider(List<IExceptionHandler> actions) {
			this.actions = actions;
		}

		/**
		 * 
		 */
		public void dispose() {
		}

		/**
		 * 
		 * 
		 * @param obj 
		 * 
		 * @return 
		 */
		public Object[] getElements(Object obj) {
			return actions.toArray();
		}

		/**
		 * 
		 * 
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
	private TableViewer configsViewer;

	/**
	 * 
	 */
	private Button editButton;

	/**
	 * 
	 */
	private Button addButton;

	/**
	 * 
	 */
	private Button deleteButton;

	// private IDialogValidator validator;

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private Shell parentShell;

	/**
	 * 
	 */
	private List<IExceptionHandler> exceptionHandler;

	/**
	 * 
	 */
	private IWebflowModelElement parentElement;

	/**
	 * 
	 * 
	 * @param item 
	 * @param parentElement 
	 * @param parentShell 
	 * @param validator 
	 * @param actions 
	 */
	public ExceptionHandlerComposite(IDialogValidator validator, TabItem item,
			Shell parentShell, List<IExceptionHandler> actions,
			IWebflowModelElement parentElement) {
		this.exceptionHandler = actions;
		item.setText("Exception Handler");
		item.setToolTipText("Define element's exception handler");
		item.setImage(WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_EXCEPTION_HANDLER));
		this.parentShell = parentShell;
		this.parentElement = parentElement;
		// this.validator = validator;
	}

	/**
	 * 
	 * 
	 * @param parent 
	 * 
	 * @return 
	 */
	protected Control createDialogArea(Composite parent) {
		Group groupActionType = new Group(parent, SWT.NULL);
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Exception Handler ");
		groupActionType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(groupActionType, SWT.NONE);
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
		data.heightHint = 200;
		configsTable.setLayoutData(data);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		configsViewer = new TableViewer(configsTable);
		configsViewer.setContentProvider(new ExceptionHandlerContentProvider(
				this.exceptionHandler));
		configsViewer.setLabelProvider(new DecoratingLabelProvider(
				new WebflowModelLabelProvider(), new WebflowModelLabelDecorator()));
		configsViewer.setInput(this);

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
				ExceptionHandler action = new ExceptionHandler();
				action.createNew(parentElement);
				if (DialogUtils.openPropertiesDialog(parentElement, action,
						true) == Dialog.OK) {
					exceptionHandler.add(action);
					configsViewer.refresh();
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
					if (selection.getFirstElement() instanceof ExceptionHandler) {
						if (DialogUtils.openPropertiesDialog(parentElement,
								(ExceptionHandler) selection.getFirstElement(),
								true) == Dialog.OK) {
							configsViewer.refresh();
						}
					}
				}
			}
		});

		deleteButton = new Button(buttonArea, SWT.PUSH);
		deleteButton.setText("Delete");
		data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		deleteButton.setLayoutData(data1);
		deleteButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null
						&& selection.getFirstElement() instanceof ExceptionHandler) {
					ExceptionHandler actionElement = (ExceptionHandler) selection
							.getFirstElement();
					exceptionHandler.remove(actionElement);
					configsViewer.refresh(true);
				}
			}
		});


		editButton.setEnabled(false);
		deleteButton.setEnabled(false);

		return groupActionType;
	}

	/**
	 * 
	 */
	protected void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (selection.isEmpty()) {
			this.editButton.setEnabled(false);
			this.deleteButton.setEnabled(false);
		}
		else {
			this.editButton.setEnabled(true);
			this.deleteButton.setEnabled(true);
		}
	}
}
