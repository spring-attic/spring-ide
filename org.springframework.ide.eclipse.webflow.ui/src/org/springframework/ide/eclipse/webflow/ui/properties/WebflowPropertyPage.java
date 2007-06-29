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
package org.springframework.ide.eclipse.webflow.ui.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;
import org.springframework.ide.eclipse.webflow.ui.model.WebflowModelLabelDecorator;

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
			Map<IWebflowConfig, Set<IModelElement>> filesToConfig = configFilesBlock
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

			// At first delete all problem markers from the removed config files
			List<IWebflowConfig> currentConfigs = project.getConfigs();
			for (IWebflowConfig currentConfig : currentConfigs) {
				if (getConfig(currentConfig.getResource(), webflowConfigs) == null) {
					MarkerUtils.deleteMarkers(currentConfig
							.getResource(), WebflowValidator.MARKER_ID);
				}
			}
			project.setConfigs(webflowConfigs);

			// Refresh label decoration of Spring project and config files
			WebflowModelLabelDecorator.update();
		}
		return super.performOk();
	}
	
	public IWebflowConfig getConfig(IFile file, List<IWebflowConfig> configs) {
		if (configs != null) {
			for (IWebflowConfig config : configs) {
				if (config.getResource().equals(file)) {
					return config;
				}
			}
		}
		return null;
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
