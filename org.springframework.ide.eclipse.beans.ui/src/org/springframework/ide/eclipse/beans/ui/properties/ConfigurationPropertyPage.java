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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUILabelDecorator;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.ProjectNode;

public class ConfigurationPropertyPage extends PropertyPage {

	public static final String ID = "org.springframework.ide.eclipse.beans." +
									"ui.properties.ConfigurationPropertyPage";
	private static final String TITLE = "ConfigurationPropertyPage.title";
	private static final String CONFIG_FILES_LABEL =
							   "ConfigurationPropertyPage.tabConfigFiles.label";
	private static final String CONFIG_SETS_LABEL =
								"ConfigurationPropertyPage.tabConfigSets.label";
	private ProjectNode projectModel;
	private ConfigFilesBlock configFilesBlock;
	private ConfigSetsBlock configSetsBlock;
	private int selectedBlock;

	public ConfigurationPropertyPage() {
		this(null, 0);
	}

	public ConfigurationPropertyPage(IProject project) {
		this(project, 0);
	}

	public ConfigurationPropertyPage(IProject project, int selectedBlock) {
		setElement(project);
		setTitle(BeansUIPlugin.getResourceString(TITLE));
		noDefaultAndApplyButton();
		this.selectedBlock = selectedBlock;
	}

	protected Control createContents(Composite parent)  {

		// Build project model
		IBeansProject project = getSpringProject();
		projectModel = new ProjectNode(null, project.getElementName());
		projectModel.setConfigs(project.getConfigNames());
		projectModel.setConfigSets(project.getConfigSets());

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		configFilesBlock = new ConfigFilesBlock(projectModel, getElement());
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_FILES_LABEL));
		item.setControl(configFilesBlock.createControl(folder));

		configSetsBlock = new ConfigSetsBlock(projectModel, getElement());
		item = new TabItem(folder, SWT.NONE);
		item.setText(BeansUIPlugin.getResourceString(CONFIG_SETS_LABEL));
		item.setControl(configSetsBlock.createControl(folder));

		Dialog.applyDialogFont(folder);

		// Pre-select specified tab item
		folder.setSelection(this.selectedBlock);
		return folder;
	}

	public boolean performOk() {
		BeansProject project = getSpringProject();

		// Save config files from model in project
		if (configFilesBlock.hasUserMadeChanges()) {
			project.setConfigs(projectModel.getConfigNames());

			// Refresh label decoration of Spring project and config files
			BeansUILabelDecorator.update();
		}

		// Save modified config sets in project
		if (configSetsBlock.hasUserMadeChanges()) {
			List configSets = new ArrayList();
			Iterator iter = projectModel.getConfigSets().iterator();
			while (iter.hasNext()) {
				ConfigSetNode node = (ConfigSetNode) iter.next();
				BeansConfigSet configSet = new BeansConfigSet(project,
										 node.getName(), node.getConfigNames());
				configSet.setAllowBeanDefinitionOverriding(
													  node.isOverrideEnabled());
				configSet.setIncomplete(node.isIncomplete());
				configSets.add(configSet);
			}
			project.setConfigSets(configSets);
		}
		return super.performOk();
	}

	protected BeansProject getSpringProject() {
		return (BeansProject) BeansCorePlugin.getModel().getProject(
													   (IProject) getElement());
	}

	public void dispose() {
		configFilesBlock.dispose();
		configSetsBlock.dispose();
		super.dispose();
	}
}
