/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.PropVal;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import org.springframework.ide.eclipse.core.StringUtils;

/**
 * An IPageSection that contains a table-based properties editor.
 * 
 * @author Kris De Volder
 */
public class PropertiesTableSection extends WizardPageSection implements ILaunchConfigurationTabSection {

	private static final char OID_SEPERATOR = ':';

	public class CellEditorSupport extends EditingSupport {

		//TODO: add content assist support to the text cell editor
		//See here for sample code http://javafind.appspot.com/model?id=318036 

		private CellEditor editor;
		private int col;

		public CellEditorSupport(int col) {
			super(tableViewer);
			this.col = col;
			this.editor = new TextCellEditor(tableViewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			String val = getColumnValue(element, col);
			return val!=null;
		}

		@Override
		protected Object getValue(Object element) {
			return getColumnValue(element, col);
		}

		@Override
		protected void setValue(Object element, Object value) {
			setColumnValue(element, col, value);
			tableViewer.refresh(element);
		}
	}

	private class CheckStateSynchronizer implements ICheckStateProvider, ICheckStateListener {
		@Override
		public boolean isGrayed(Object element) {
			return false;
		}

		@Override
		public boolean isChecked(Object element) {
			if (element instanceof PropVal) {
				PropVal prop = (PropVal) element;
				return prop.isChecked;
			}
			return false;
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			PropVal element = (PropVal) event.getElement();
			element.isChecked = event.getChecked();
			dirtyState.setValue(true);
		}
	}

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);

	@Override
	public LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}
	
	public PropertiesTableSection(IPageWithSections owner) {
		super(owner);
	}

	private static final String[] COLUMN_NAMES = {"property", "value"};
	private CheckboxTableViewer tableViewer;

	private List<PropVal> props = new ArrayList<PropVal>();

	private CheckStateSynchronizer checkStateProvider = new CheckStateSynchronizer();
	
	/**
	 * Remembers element that was last clicked by mouse, so that we
	 * can use it in executing context menu actions on it.
	 */
	private PropVal lastMouseDownTarget;

	private MouseListener clickListener = new MouseAdapter() {
		@Override
		public void mouseDown(MouseEvent e) {
			PropVal clickedElement = getElementUnder(e);
			lastMouseDownTarget = clickedElement;
			if (e.button==1) {
//				System.out.println("---- mouse down -----");
//				System.out.println("Button = "+e.button);
//				System.out.println("x = "+e.x);
//				System.out.println("y = "+e.y);
				//Tricky: just asking for the 'cell' at event position returns null
				// when click is received on a checkbox in the CheckBoxTableViewer.
				//Since we really only want to know if we are clicking in some 
				//existing row we can just 'ignore' event.x position and set 
				//it to the middle of the viewer.
				
				if (clickedElement==null) {
					lastMouseDownTarget = null;
//					System.out.println("NO cell");
					addNewRow(true);
				}
			}
		}

		private PropVal getElementUnder(MouseEvent e) {
			ViewerCell cell = tableViewer.getCell(new Point(
					tableViewer.getTable().getBounds().width/2,
					e.y
			));
			if (cell!=null) {
				return (PropVal) cell.getElement();
			}
			return null;
		}
	};

	@Override
	public void createContents(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);
		tableViewer.setColumnProperties(COLUMN_NAMES);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setCheckStateProvider(checkStateProvider);
		tableViewer.addCheckStateListener(checkStateProvider);

		tableViewer.getTable().addMouseListener(clickListener);

		createColumns(); // make sure to call before 'setInput' or labels won't be updated.

		createContextMenu();

		tableViewer.setInput(props);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getTable());
	}

	private void createContextMenu() {
		Table table = tableViewer.getTable();
		Menu contextMenu = new Menu(table);
		addMenu(contextMenu, "Delete Row", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropVal toRemove = lastMouseDownTarget;
				if (toRemove!=null) {
					props.remove(toRemove);
					tableViewer.remove(toRemove);
					dirtyState.setValue(true);
				}
			}
		});
		addMenu(contextMenu, "Add Row", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewRow(false);
			}
		});
		table.setMenu(contextMenu);  
	}

	/**
	 * Add a new row and start editing it. If reuseExistingEmptyRow
	 * is true then will check if the last row in table is a 'empty'
	 * row and reuse that rather than create another empty row.
	 */
	private void addNewRow(boolean reuseExistingEmptyRow) {
		PropVal newProp = null;
		if (reuseExistingEmptyRow) {
			newProp = findEmptyProp();
		}
		if (newProp==null) {
			newProp = new PropVal("", "", true);
			props.add(newProp);
			tableViewer.add(newProp);
		}
		tableViewer.editElement(newProp, 0);
	}

	private PropVal findEmptyProp() {
		if (!props.isEmpty()) {
			PropVal last = props.get(props.size()-1);
			if (!StringUtils.hasText(last.name)) {
				return last;
			}
		}
		return null;
	}

	private void addMenu(Menu contextMenu, String label, SelectionAdapter handler) {
		MenuItem mnu = new MenuItem(contextMenu, SWT.PUSH);
		mnu.setText(label);
		mnu.addSelectionListener(handler);
	}

	private String getColumnValue(Object element, final int col) {
		if (element instanceof PropVal) {
			if (col==0) {
				return ((PropVal) element).name;
			}
			return ((PropVal) element).value;
		}
		return null;
	}

	private void setColumnValue(Object element, int col, Object value) {
		if (element instanceof PropVal) {
			if (col==0) {
				((PropVal) element).name = (String)value;
			} else {
				((PropVal) element).value = (String)value;
			}
			dirtyState.setValue(true);
		}
	}

	private void createColumns() {
		for (int i = 0; i < COLUMN_NAMES.length; i++) {
			final int _i = i;
			String colname = COLUMN_NAMES[i];
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
			col.getColumn().setWidth(200);
			col.getColumn().setText(colname);
			col.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					Object element = cell.getElement();
					cell.setText(getColumnValue(element, _i));
				}
			});
			col.setEditingSupport(new CellEditorSupport(i));
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		try {
			props = BootLaunchConfigurationDelegate.getProperties(conf);
			dirtyState.setValue(false);
			if (tableViewer!=null) {
				tableViewer.setInput(props);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.setProperties(conf, props);
		dirtyState.setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.clearProperties(conf);
	}

}
