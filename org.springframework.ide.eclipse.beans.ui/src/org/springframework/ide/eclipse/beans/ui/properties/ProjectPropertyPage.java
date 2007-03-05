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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;

/**
 * Spring project property page.
 * 
 * @author Torsten Juergeleit
 */
public class ProjectPropertyPage extends PropertyPage {

	public static final String ID = BeansUIPlugin.PLUGIN_ID
			+ ".ui.properties.ProjectPropertyPage";

	private static final String PREFIX = "ConfigurationPropertyPage.";

	private static final String TITLE = PREFIX + "title";

	private static final String CONFIG_FILES_LABEL = PREFIX
			+ "tabConfigFiles.label";

	private static final String CONFIG_SETS_LABEL = PREFIX
			+ "tabConfigSets.label";

	private PropertiesModel model;
	private ConfigFilesTab configFilesTab;
	private ConfigSetsTab configSetsTab;
	private int selectedTab;

	public ProjectPropertyPage() {
		this(null, 0);
	}

	public ProjectPropertyPage(IProject project) {
		this(project, 0);
	}

	public ProjectPropertyPage(IProject project, int selectedTab) {
		setElement(project);
		setTitle(BeansUIPlugin.getResourceString(TITLE));
		noDefaultAndApplyButton();
		this.selectedTab = selectedTab;
	}

	protected Control createContents(Composite parent) {

		// Build temporary beans core model with a cloned "real" Spring project
		IProject project = (IProject) getElement();
		model = new PropertiesModel();
		PropertiesProject modelProject = new PropertiesProject(BeansCorePlugin
				.getModel().getProject(project));
		modelProject.setElementParent(model);
		model.addProject(modelProject);

		// Build folder with tabs
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		configFilesTab = new ConfigFilesTab(model, project);
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_FILES_LABEL));
		item.setControl(configFilesTab.createControl(folder));

		configSetsTab = new ConfigSetsTab(model, project);
		item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_SETS_LABEL));
		item.setControl(configSetsTab.createControl(folder));

		Dialog.applyDialogFont(folder);

		// Pre-select specified tab item
		folder.setSelection(selectedTab);
		return folder;
	}

	public boolean performOk() {
		IProject project = (IProject) getElement();
		IBeansProject currentProject = BeansCorePlugin.getModel().getProject(
				project);
		PropertiesProject newProject = (PropertiesProject) model
				.getProject(project);

		// At first delete all problem markers from the removed config files
		if (configFilesTab.hasUserMadeChanges()) {
			Set<IBeansConfig> currentConfigs = currentProject.getConfigs();
			for (IBeansConfig currentConfig : currentConfigs) {
				if (!newProject.hasConfig(currentConfig.getElementName())) {
					BeansModelUtils.deleteProblemMarkers(currentConfig);
				}
			}
		}

		// Now save modified project description
		if (configFilesTab.hasUserMadeChanges()
				|| configSetsTab.hasUserMadeChanges()) {
			newProject.saveDescription();
		}

		// Finally (after saving the modified project description!!!) refresh
		// the label decoration of all config files
		if (configFilesTab.hasUserMadeChanges()) {
			BeansModelLabelDecorator.update();
		}
		return super.performOk();
	}

	public void dispose() {
		if (configFilesTab != null) {
			configFilesTab.dispose();
		}
		if (configSetsTab!= null) {
			configSetsTab.dispose();
		}
		super.dispose();
	}
}
