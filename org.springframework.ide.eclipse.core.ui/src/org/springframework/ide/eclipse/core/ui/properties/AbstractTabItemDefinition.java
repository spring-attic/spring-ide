/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.ide.eclipse.core.ui.properties;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * Base class for a property tab extension
 * 
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractTabItemDefinition implements ITabItemDefinition {

	private IAdaptable element;

	private TabItem tabItem;

	private static FontMetrics fontMetrics;

	/**
	 * @return Returns the fontMetrics.
	 */
	protected static FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	/**
	 * @param fontMetrics
	 *            The fontMetrics to set.
	 */
	protected static void setFontMetrics(FontMetrics fontMetrics) {
		AbstractTabItemDefinition.fontMetrics = fontMetrics;
	}

	/**
	 * @param tabItem
	 *            The tabItem to set.
	 */
	protected void setTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
	}

	/**
	 * @param element
	 *            The element to set.
	 */
	protected void setElement(IAdaptable element) {
		this.element = element;
	}

	/**
	 * @param parent
	 * @param treeStyle
	 */
	public void createTabItem(TabFolder parent, IAdaptable element) {
		this.element = element;

		tabItem = new TabItem(parent, SWT.NONE);
		tabItem.setImage(createImage());
		tabItem.setText(createTitle());

		Composite composite = new Composite(parent, SWT.NONE);
		if (getFontMetrics() == null) {
			setFontMetrics(new GC(composite).getFontMetrics());
		}
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		createContents(composite);

		getProperties();
		tabItem.setControl(composite);
	}

	/**
	 * @param parent
	 * @return
	 */
	protected static Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	protected Table createTable(Composite parent, String label, String toolTip, String[] columnsTitles) {
		Label labelWidget = new Label(parent, SWT.NONE);
		labelWidget.setText(label);
		labelWidget.setToolTipText(toolTip);
		Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
		data.heightHint = 100;
		table.setLayoutData(data);
		for (int i = 0; i < columnsTitles.length; i++) {
			createTableColumn(i, 200, table, columnsTitles[i]);
		}
		return table;
	}

	protected TableColumn createTableColumn(int columnIndex, int width, Table table, String title) {
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(title);
		tc.setResizable(true);
		tc.setWidth(width);
		final Table tmpTable = table;
		final int tmpColumnIndex = columnIndex;
		tc.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				// sort column 1
				TableItem[] items = tmpTable.getItems();
				Collator collator = Collator.getInstance(Locale.getDefault());
				for (int i = 1; i < items.length; i++) {
					String value1 = items[i].getText(tmpColumnIndex);
					for (int j = 0; j < i; j++) {
						String value2 = items[j].getText(tmpColumnIndex);
						if (collator.compare(value1, value2) < 0) {
							String[] values = { items[i].getText(0), items[i].getText(1) };
							items[i].dispose();
							TableItem item = new TableItem(tmpTable, SWT.NONE, j);
							item.setText(values);
							items = tmpTable.getItems();
							break;
						}
					}
				}
			}
		});
		return tc;
	}

	protected void addTableContents(Table table, Object[] items) {
		for (int i = 0; i < items.length; i++) {
			String[] item = (String[]) items[i];
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(item);
		}
	}

	protected static Text createLabelAndTextField(Composite parent, String label, String toolTip, int textFieldWidth) {
		Composite composite = createDefaultComposite(parent);
		Label labelWidget = new Label(composite, SWT.NONE);
		labelWidget.setText(label);
		labelWidget.setToolTipText(toolTip);
		Text textWidget = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = Dialog.convertWidthInCharsToPixels(getFontMetrics(), textFieldWidth);
		textWidget.setLayoutData(gd);
		return textWidget;
	}

	protected static Button createLabelAndCheckBoxField(Composite parent, String label, String toolTip, int textFieldWidth) {
		Composite composite = createDefaultComposite(parent);
		Button checkBoxWidget = new Button(composite, SWT.CHECK | SWT.LEFT);
		checkBoxWidget.setText(label);
		checkBoxWidget.setToolTipText(toolTip);
		return checkBoxWidget;
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.properties.ITabItemContainer#performDefaults()
	 */
	public void performDefaults() {
		setDefaults();
		try {
			storeProperties();
		} catch (CoreException e) {
			SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.WARNING, "using default properties for this project"));
		}
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.properties.ITabItemContainer#performOk()
	 */
	public boolean performOk() {
		try {
			storeProperties();
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public IAdaptable getElement() {
		return element;
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.properties.ITabItemContainer#getTabItem()
	 */
	public TabItem getTabItem() {
		return tabItem;
	}

	protected abstract Image createImage();

	protected abstract String createTitle();

	protected abstract void createContents(Composite composite);

	protected abstract void setDefaults();

	protected abstract void storeProperties() throws CoreException;

	protected abstract void getProperties();

	protected abstract void init();
}