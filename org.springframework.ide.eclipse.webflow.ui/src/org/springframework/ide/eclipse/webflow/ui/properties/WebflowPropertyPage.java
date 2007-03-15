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

package org.springframework.ide.eclipse.webflow.ui.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowPropertyPage extends PropertyPage {

	/**
	 * 
	 */
	public static final String ID = "org.springframework.ide.eclipse.webflow."
			+ "ui.properties.ConfigurationPropertyPage";

	/**
	 * 
	 */
	private static final String TITLE = "ConfigurationPropertyPage.title";

	/**
	 * 
	 */
	private static final String CONFIG_FILES_LABEL = "ConfigurationPropertyPage.tabConfigFiles.label";

	/**
	 * 
	 */
	private WebflowConfigTab configFilesBlock;

	/**
	 * 
	 */
	public WebflowPropertyPage() {
		this(null);
	}

	/**
	 * 
	 * 
	 * @param project 
	 */
	public WebflowPropertyPage(IProject project) {
		setElement(project);
		setTitle(Activator.getResourceString(TITLE));
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		// Build project model
		IWebflowProject project = getSpringProject();

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		configFilesBlock = new WebflowConfigTab(project, getElement());
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(Activator.getResourceString(CONFIG_FILES_LABEL));
		item.setControl(configFilesBlock.createControl(folder));

		Dialog.applyDialogFont(folder);

		return folder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		IWebflowProject project = getSpringProject();

		// Save config files from model in project
		if (configFilesBlock.hasUserMadeChanges()) {

			Set<IWebflowConfig> files = configFilesBlock.getConfigFiles();
			Map<IWebflowConfig, Set<IBeansConfig>> filesToConfig = configFilesBlock
					.getConfigFilesToBeansConfigs();
			Map<IWebflowConfig, String> names = configFilesBlock.getConfigFilesToNames();
			List<IWebflowConfig> webflowConfigs = new ArrayList<IWebflowConfig>();
			for (IWebflowConfig file : files) {
				WebflowConfig webflowConfig = new WebflowConfig(project);
				webflowConfig.setBeansConfigs(filesToConfig.get(file));
				webflowConfig.setResource(file.getResource());
				if (names.get(file) != null) {
					webflowConfig.setName(names.get(file));
				}
				webflowConfigs.add(webflowConfig);
			}

			project.setConfigs(webflowConfigs);

			// Refresh label decoration of Spring project and config files
			// BeansUILabelDecorator.update();
		}
		return super.performOk();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected IWebflowProject getSpringProject() {
		return (IWebflowProject) org.springframework.ide.eclipse.webflow.core.Activator
				.getModel().getProject((IProject) getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
	}
}
