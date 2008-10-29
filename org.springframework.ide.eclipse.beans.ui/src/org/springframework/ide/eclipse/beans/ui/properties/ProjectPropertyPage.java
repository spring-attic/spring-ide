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
package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Spring project property page.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class ProjectPropertyPage extends PropertyPage {

	public static final String ID = BeansUIPlugin.PLUGIN_ID + ".properties.ProjectPropertyPage";

	public static final String BLOCK_ID = ID + ".blockId";

	public static final String SCAN = ID + ".scan";

	public static final String SELECTED_RESOURCE = ID + ".selectedResource";

	private static final String PREFIX = "ConfigurationPropertyPage.";

	private static final String TITLE = PREFIX + "title";

	private static final String CONFIG_FILES_LABEL = PREFIX + "tabConfigFiles.label";

	private static final String CONFIG_SETS_LABEL = PREFIX + "tabConfigSets.label";

	private static final String CONFIG_LOCATORS_LABEL = PREFIX + "tabConfigLocators.label";

	private PropertiesModel model;

	private ConfigFilesTab configFilesTab;

	private ConfigSetsTab configSetsTab;

	private int selectedTab;

	private IModelElement selectedModelElement;

	private Map<String, Object> pageData;

	private ConfigLocatorTab configLocatorTab;

	private boolean shouldTriggerScan = false;

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

	@Override
	protected Control createContents(Composite parent) {

		// Build temporary beans core model with a cloned "real" Spring project
		IProject project = (IProject) getElement();
		model = new PropertiesModel();
		PropertiesProject modelProject = new PropertiesProject(model, BeansCorePlugin.getModel()
				.getProject(project));
		model.addProject(modelProject);

		// Build folder with tabs
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		configFilesTab = new ConfigFilesTab(model, modelProject, selectedModelElement);
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_FILES_LABEL));
		item.setControl(configFilesTab.createControl(folder));

		configSetsTab = new ConfigSetsTab(model, modelProject, selectedModelElement);
		item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_SETS_LABEL));
		item.setControl(configSetsTab.createControl(folder));

		if (BeansConfigLocatorFactory.hasEnabledBeansConfigLocatorDefinitions(project)) {
			configLocatorTab = new ConfigLocatorTab(modelProject.getProject());
			item = new TabItem(folder, SWT.NONE);
			item.setText(BeansUIPlugin.getResourceString(CONFIG_LOCATORS_LABEL));
			item.setControl(configLocatorTab.createContents(folder));
		}
		Dialog.applyDialogFont(folder);

		// Pre-select specified tab item
		folder.setSelection(selectedTab);

		// Open the scan dialog if required if coming from a nature added event
		if (shouldTriggerScan) {
			configFilesTab.handleScanButtonPressed();
		}

		return folder;
	}

	@Override
	public boolean performOk() {
		IProject project = (IProject) getElement();
		IBeansProject currentProject = BeansCorePlugin.getModel().getProject(project);
		boolean userMadeChanges = configFilesTab.hasUserMadeChanges()
				|| configSetsTab.hasUserMadeChanges();

		if (userMadeChanges && !currentProject.isUpdatable()) {
			MessageDialog
					.openInformation(
							getShell(),
							"Project cannot be updated",
							"The project properties cannot be changed because the project "
									+ "description file '.springBeans' is not accessible or writable. \n\nPlease ensure that this file is writable.");
			return super.performOk();
		}

		PropertiesProject newProject = (PropertiesProject) model.getProject(project);

		// At first delete all problem markers from the removed config files
		if (configFilesTab.hasUserMadeChanges()) {
			for (IBeansConfig currentConfig : currentProject.getConfigs()) {
				if (!newProject.hasConfig(currentConfig.getElementName())) {
					MarkerUtils.deleteAllMarkers(currentConfig.getElementResource(),
							SpringCore.MARKER_ID);
				}
			}
		}

		// Now save modified project description
		if (userMadeChanges) {
			SpringCorePreferences.getProjectPreferences(project.getProject(),
					BeansCorePlugin.PLUGIN_ID).putBoolean(
					BeansCorePlugin.IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY,
					configFilesTab.shouldIgnoreMissingNamespaceHandler());
			newProject.saveDescription();
		}

		if (configLocatorTab != null) {
			configLocatorTab.performOk();
		}

		// Finally (after saving the modified project description!!!) refresh
		// the label decoration of all config files
		if (configFilesTab.hasUserMadeChanges()) {
			BeansModelLabelDecorator.update();
		}

		return super.performOk();
	}

	@Override
	public void dispose() {
		if (configFilesTab != null) {
			configFilesTab.dispose();
		}
		if (configSetsTab != null) {
			configSetsTab.dispose();
		}
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	public void applyData(Object data) {
		super.applyData(data);
		if (data instanceof Map) {
			this.pageData = (Map<String, Object>) data;
			this.selectedTab = (Integer) this.pageData.get(BLOCK_ID);
			if (this.pageData.containsKey(SELECTED_RESOURCE)
					&& this.pageData.get(SELECTED_RESOURCE) instanceof IModelElement) {
				this.selectedModelElement = (IModelElement) this.pageData.get(SELECTED_RESOURCE);
			}
			if (this.pageData.containsKey(SCAN) && this.pageData.get(SCAN) instanceof Boolean) {
				this.shouldTriggerScan = (Boolean) this.pageData.get(SCAN);
			}
		}
	}
}
