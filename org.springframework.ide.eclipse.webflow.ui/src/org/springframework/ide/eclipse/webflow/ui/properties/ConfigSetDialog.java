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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConfigSetDialog extends Dialog {

	private static final int BEANS_CONFIG_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer beansConfigSetViewer;

	private Text nameText;

	private Button okButton;

	private Label errorLabel;

	private String title;

	private IProject project;

	private Set<IBeansConfig> beansConfig;

	private List<String> config;
	
	private IFile file;

	/**
	 * 
	 * 
	 * @param project
	 * @param beansConfig
	 * @param parent
	 */
	public ConfigSetDialog(Shell parent, IProject project,
			Set<IBeansConfig> beansConfig, List<String> config, IFile file) {
		super(parent);
		this.project = project;
		this.config = config;
		this.beansConfig = beansConfig;
		this.title = "Link to Spring configs";
		
		String name = config.get(0);
		if (name == null) {
			int i = file.getName().lastIndexOf('.');
			if (i > 0) {
				name = file.getName().substring(0, i);
			}
			config.add(0, name);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText("Specify a flow id");
		nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		if (this.config != null && this.config.size() > 0
				&& this.config.get(0) != null) {
			this.nameText.setText(this.config.get(0));
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateName();
			}
		});

		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel.setText("Link Spring Beans configs to Web Flow flow");
		// config set list viewer
		beansConfigSetViewer = CheckboxTableViewer.newCheckList(composite,
				SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = BEANS_CONFIG_LIST_VIEWER_HEIGHT;
		beansConfigSetViewer.getTable().setLayoutData(gd);
		beansConfigSetViewer.setContentProvider(new ConfigFilesContentProvider(
				createBeansConfigList()));
		beansConfigSetViewer.setLabelProvider(new BeansLabelProvider());
		beansConfigSetViewer.setInput(this); // activate content provider
		if (this.beansConfig != null) {
			beansConfigSetViewer.setCheckedElements(this.beansConfig.toArray());
		}

		// Create error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent
				.getDisplay()));

		applyDialogFont(composite);

		return composite;
	}

	/**
	 * 
	 * 
	 * @param group
	 * @param labelText
	 * 
	 * @return
	 */
	protected Button createCheckBox(Composite group, String labelText) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		group.setLayout(layout);

		Button button = new Button(group, SWT.CHECK);

		Label label = new Label(group, SWT.NONE);
		label.setText(labelText);

		return button;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		okButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@SuppressWarnings("unchecked")
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			List configs = Arrays.asList(beansConfigSetViewer
					.getCheckedElements());
			this.beansConfig.clear();
			this.beansConfig.addAll(configs);
			this.config.add(0, nameText.getText());
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private Set<IBeansConfig> createBeansConfigList() {
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(
				project);
		// Create new list with config files from this config set
		return beansProject.getConfigs();
	}

	/**
	 * 
	 */
	private static class ConfigFilesContentProvider implements
			IStructuredContentProvider {

		/**
		 * 
		 */
		private Set<IBeansConfig> configs;

		/**
		 * 
		 * 
		 * @param configs
		 */
		public ConfigFilesContentProvider(Set<IBeansConfig> configs) {
			this.configs = configs;
		}

		/**
		 * 
		 * 
		 * @param obj
		 * 
		 * @return
		 */
		public Object[] getElements(Object obj) {
			return configs.toArray();
		}

		/**
		 * 
		 * 
		 * @param newInput
		 * @param viewer
		 * @param oldInput
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * 
		 */
		public void dispose() {
		}
	}

	/**
	 * 
	 */
	private static class BeansLabelProvider extends BeansModelLabelProvider {

		/**
		 * 
		 */
		public BeansLabelProvider() {
			super(true);
		}

		/**
		 * 
		 * 
		 * @param obj
		 * 
		 * @return
		 */
		public String getText(Object obj) {
			if (obj instanceof IBeansConfig) {
				return super.getText(((IBeansConfig) obj).getElementResource());
			}
			else {
				return super.getText(obj);
			}
		}
	}

	private void validateName() {
		boolean isEnabled = true;

		String name = nameText.getText();
		if (name == null || name.trim().length() == 0) {
			errorLabel.setText("Please specify a valid flow id");
			isEnabled = false;
		}
		else {
			IWebflowProject wp = Activator.getModel().getProject(project);
			for (IWebflowConfig config : wp.getConfigs()) {
				if (name.equals(config.getName()) && !config.getResource().equals(file)){
					errorLabel.setText("Flow id already in use in this project");
					isEnabled = false;
				}
			}
		}
		
		okButton.setEnabled(isEnabled);
		errorLabel.getParent().update();
	}

	public String getName() {
		return this.nameText.getText();
	}
}