/*
 * Copyright 2002-2006 the original author or authors.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelSorter;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class ConfigSetDialog extends Dialog {

	private static final String PREFIX = "ConfigSetDialog.";

	private static final String TITLE_NEW = PREFIX + "title.new";
	private static final String TITLE_EDIT = PREFIX + "title.edit";
	private static final String ERROR_INVALID_NAME =
												  PREFIX + "error.invalidName";
	private static final String ERROR_USED_NAME = PREFIX + "error.usedName";
	private static final String NAME_LABEL = PREFIX + "name.label";
	private static final String OVERRIDE_LABEL = PREFIX + "override.label";
	private static final String INCOMPLETE_LABEL = PREFIX + "incomplete.label";
	private static final String VIEWER_LABEL = PREFIX + "viewer.label";
	private static final int LIST_VIEWER_WIDTH = 400;
	private static final int LIST_VIEWER_HEIGHT = 250;

	private Text nameText;
	private Button overrideButton;
	private Button incompleteButton;
	private CheckboxTableViewer configsViewer;
	private Label errorLabel;
	private Button okButton;

	private ProjectNode project;
	private ConfigSetNode configSet;
	private String configSetName;
	private String title;

	public ConfigSetDialog(Shell parent, ProjectNode project,
						   String configSetName) {
		super(parent);
		this.project = project;
		this.configSetName = configSetName;
		if (configSetName == null) {
			configSet = new ConfigSetNode(project);
			title = BeansUIPlugin.getResourceString(TITLE_NEW);
		} else {
			configSet = project.getConfigSet(configSetName);
			title = BeansUIPlugin.getResourceString(TITLE_EDIT);
		}
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	protected Control createDialogArea(Composite parent) {

		// group composite for options
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite optionsGroup = new Composite(composite, SWT.NULL);
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels( 
											 IDialogConstants.VERTICAL_MARGIN); 
		layout.marginWidth = convertHorizontalDLUsToPixels( 
										   IDialogConstants.HORIZONTAL_MARGIN); 
		optionsGroup.setLayout(layout); 
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

		// labeled name text field
		nameText = SpringUIUtils.createTextField(optionsGroup, 
								  BeansUIPlugin.getResourceString(NAME_LABEL)); 
		nameText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateName();
				}
			}
		);

		// labeled checkboxes
//		Composite checkboxGroup = new Composite(composite, SWT.NULL);
//		checkboxGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		overrideButton = SpringUIUtils.createCheckBox(optionsGroup, 
							  BeansUIPlugin.getResourceString(OVERRIDE_LABEL));
		overrideButton.setSelection(configSet.isOverrideEnabled());

		incompleteButton = SpringUIUtils.createCheckBox(optionsGroup, 
							BeansUIPlugin.getResourceString(INCOMPLETE_LABEL));
		incompleteButton.setSelection(configSet.isIncomplete());

		// config set list viewer
		Label viewerLabel = new Label(composite, SWT.NONE);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | 
											   GridData.HORIZONTAL_ALIGN_FILL); 
		viewerLabel.setLayoutData(gd); 
		viewerLabel.setText(BeansUIPlugin.getResourceString(VIEWER_LABEL)); 

		configsViewer = CheckboxTableViewer.newCheckList(composite, 
														 SWT.BORDER); 
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = LIST_VIEWER_HEIGHT;

		configsViewer.getTable().setLayoutData(gd);
		configsViewer.setContentProvider(new ConfigFilesContentProvider(
														   createConfigList()));
		configsViewer.setLabelProvider(new ModelLabelProvider());
		configsViewer.setSorter(new ModelSorter(true));
		configsViewer.setInput(this);	// activate content provider
		configsViewer.setCheckedElements(configSet.getConfigs().toArray());

		// error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
											  GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(
						  JFaceColors.getErrorBackground(parent.getDisplay()));
		applyDialogFont(composite); 
		return composite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
								IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
					 IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		nameText.setFocus();
		String name = configSet.getName();
		if (name != null && name.trim().length() != 0) {
			nameText.setText(name);
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String name = nameText.getText();

			// Keep a copy of the original list of configs in the config set
			List oldConfigs = new ArrayList(configSet.getConfigs());

			// Update config set
			configSet.clear();
			configSet.setName(name);
			configSet.setOverrideEnabled(overrideButton.getSelection());
			configSet.setIncomplete(incompleteButton.getSelection());

			// At first add the originally and still selected configs to the
			// config set
			List newConfigs = new ArrayList(Arrays.asList(
										  configsViewer.getCheckedElements()));
			Iterator configs = oldConfigs.iterator();
			while (configs.hasNext()) {
				ConfigNode config = (ConfigNode) configs.next();
				if (newConfigs.contains(config)) {
					configSet.addConfig(config);
					newConfigs.remove(config);
				}
			}

			// Finally add the newly selected configs to the config set
			configSet.addConfigs(newConfigs);

			// Add newly created config set to project or re-add existing one
			if (configSetName == null) {
				configSet.setParent(project);
				configSet.setOverrideEnabled(overrideButton.getSelection());
				configSet.setIncomplete(incompleteButton.getSelection());
			} else if (!configSetName.equals(name)) {
				project.removeConfigSet(configSetName);
			}
			project.addConfigSet(configSet);
		}
		super.buttonPressed(buttonId);
	}

	private List createConfigList() {

		// Create new list with config files from this config set 
		List configs = new ArrayList(configSet.getConfigs());

		// Add missing configs from project
		Iterator iter = project.getConfigs().iterator();
		while (iter.hasNext()) {
			ConfigNode config = (ConfigNode) iter.next();
			if (!configSet.hasConfig(config.getName())) {
				configs.add(new ConfigNode(configSet, config.getName()));
			}
		}

		// Add all configs from referenced projects
		IBeansModel model = BeansCorePlugin.getModel();
		try {
			IProject[] projects = project.getProject().getProject()
					.getReferencedProjects();
			for (int i = 0; i < projects.length; i++) {
				IBeansProject project = model.getProject(projects[i]);
				if (project != null) {
					iter = project.getConfigs().iterator();
					while (iter.hasNext()) {
						IBeansConfig config = (IBeansConfig) iter.next();
						String projectPath = ModelUtils
								.getResourcePath(config.getElementParent());
						if (projectPath != null) {

							// Create the full qualified path of the config
							// (with support for configs stored in JAR files)  
							String name = projectPath + "/"
									+ config.getElementName();
							if (!configSet.hasConfig(name)) {
								configs.add(new ConfigNode(configSet, name));
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			// we can't do anything here
		}
		return configs;
	}

	private void validateName() {
		boolean isEnabled = false;

		String name = nameText.getText();
		if (name == null || name.trim().length() == 0 ||
										   !StringUtils.isAlphaNumeric(name)) {
			errorLabel.setText(BeansUIPlugin.getResourceString(
														   ERROR_INVALID_NAME));
		} else if (configSetName == null || !name.equals(configSetName)) {
			if (project.hasConfigSet(name)) {
				errorLabel.setText(BeansUIPlugin.getResourceString(
															  ERROR_USED_NAME));
			} else {
				errorLabel.setText("");
				isEnabled = true;
			}
		} else {
			errorLabel.setText("");
			isEnabled = true;
		}

		okButton.setEnabled(isEnabled);
		errorLabel.getParent().update();
	}

	private class ConfigFilesContentProvider
										 implements IStructuredContentProvider {
		private List configs;
	
		public ConfigFilesContentProvider(List configs) {
			this.configs = configs;
		}
	
		public Object[] getElements(Object obj) {
			return configs.toArray();
		}
	
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	
		public void dispose() {
		}
	}
}
