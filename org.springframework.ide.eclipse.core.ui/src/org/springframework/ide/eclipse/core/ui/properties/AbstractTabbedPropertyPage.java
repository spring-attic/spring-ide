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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.dialogs.internal.TabFolderLayout;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * Base class for tabbed property pages.
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractTabbedPropertyPage extends PropertyPage {

	private List tabItemsContainers;

	private TabFolder folder;

	/**
	 * @return Returns the tabItemsContainers.
	 */
	protected List getTabItemsContainers() {
		return tabItemsContainers;
	}

	/**
	 * @param tabItemsContainers
	 *            The tabItemsContainers to set.
	 */
	protected void setTabItemsContainers(List tabItemsContainers) {
		this.tabItemsContainers = tabItemsContainers;
	}

	protected Control createContents(Composite parent) {

		folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		initTabItemContainers();
		createTabItems(folder);
		return folder;
	}

	protected void createTabItems(TabFolder folder) {
		Iterator it = getTabItemsContainers().iterator();
		SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.OK, "creating " + getTabItemsContainers().size() + " tabItemContainers."));
		while (it.hasNext()) {
			try {
				ITabItemDefinition tabItemContainer = (ITabItemDefinition) it.next();
				SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.OK, "adding tab " + tabItemContainer.getClass().getName()));
				tabItemContainer.createTabItem(folder, getElement());
			} catch (Exception e) {
				SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, "cannot add..." + e));
			}
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		Iterator it = getTabItemsContainers().iterator();
		while (it.hasNext()) {
			ITabItemDefinition tabItemContainer = (ITabItemDefinition) it.next();
			tabItemContainer.performDefaults();
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		Iterator it = getTabItemsContainers().iterator();
		while (it.hasNext()) {
			ITabItemDefinition tabItemContainer = (ITabItemDefinition) it.next();
			tabItemContainer.performOk();
		}
		return true;
	}

	/**
	 * This method must be implemented and must set the tabItemContainers protected attribute.
	 * 
	 * @param tabItemContainers
	 * @see AbstractTabbedPropertyPage#tabItemsContainers
	 */
	public abstract void initTabItemContainers();
}
