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
package org.springframework.ide.eclipse.webflow.ui.graph.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.ModelTableLabelProvider;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.PropertiesContentProvider;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class AttributeSection extends AbstractPropertySection implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private IAttributeEnabled state;

	/**
	 * 
	 */
	private IAttributeEnabled oldState;

	/**
	 * 
	 */
	private TableViewer configsViewer;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);
		FormData data;
		Table configsTable = getWidgetFactory().createTable(composite,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(100, 0);
		data.height = 70;
		configsTable.setLayoutData(data);
		TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
		columnName.setText("Name");
		columnName.setWidth(150);
		TableColumn columnValue = new TableColumn(configsTable, SWT.NONE);
		columnValue.setText("Value");
		columnValue.setWidth(120);
		TableColumn columnType = new TableColumn(configsTable, SWT.NONE);
		columnType.setText("Type");
		columnType.setWidth(80);
		configsTable.setHeaderVisible(true);

		configsViewer = new TableViewer(configsTable);
		String[] columnNames = new String[] { "Name", "Value", "Type" };
		configsViewer.setColumnProperties(columnNames);
		configsViewer.setContentProvider(new PropertiesContentProvider(
				this.state, configsViewer));

		configsViewer.setLabelProvider(new ModelTableLabelProvider());
		configsViewer.setInput(this.state);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				// handleTableSelectionChanged();
			}
		});

		CLabel labelLabel = getWidgetFactory().createCLabel(composite,
				"Attributes:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(configsTable,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(configsTable, 0, SWT.TOP);
		labelLabel.setLayoutData(data);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		if (oldState != null) {
			oldState.removePropertyChangeListener(this);
		}
		if (input instanceof AbstractStatePart
				&& ((AbstractStatePart) input).getModel() instanceof IAttributeEnabled) {
			state = (IAttributeEnabled) ((AbstractStatePart) input).getModel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	public void refresh() {
		configsViewer.setContentProvider(new PropertiesContentProvider(
				this.state, configsViewer));
		configsViewer.setInput(this.state);
		configsViewer.refresh();
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
	 */
	public void dispose() {
		if (state != null) {
			state.removePropertyChangeListener(this);
		}
		super.dispose();
	}
}
