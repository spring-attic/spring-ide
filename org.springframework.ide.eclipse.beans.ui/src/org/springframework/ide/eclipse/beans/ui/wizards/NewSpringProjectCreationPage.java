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

package org.springframework.ide.eclipse.beans.ui.wizards;

import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewSpringProjectCreationPage extends WizardNewProjectCreationPage {

	private Button isJavaButton;
	private Text sourceDirText;
	private Label sourceDirLabel;
	private Text outputDirText;
	private Label outputDirLabel;
	private Text extensionsText;

	public NewSpringProjectCreationPage(String pageName) {
		super(pageName);
	}

	public boolean isJavaProject() {
		return isJavaButton.getSelection();
	}

	public String getSourceDirectory() {
		return sourceDirText.getText();
	}

	public String getOutputDirectory() {
		return outputDirText.getText();
	}

	public Set<String> getConfigExtensions() {
		return StringUtils.commaDelimitedListToSet(extensionsText.getText());
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite control = (Composite)getControl();
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		control.setLayout(layout);

		createProjectTypeGroup(control);

		Dialog.applyDialogFont(control);
		setControl(control);
	}

	private void createProjectTypeGroup(Composite container) {
		Group springGroup = new Group(container, SWT.NONE);
		springGroup.setText(BeansWizardsMessages.NewProjectPage_springSettings);
		GridLayout dirLayout = new GridLayout();
		dirLayout.numColumns = 1;
		springGroup.setLayout(dirLayout);
		springGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		extensionsText = SpringUIUtils.createTextField(springGroup,
				BeansWizardsMessages.NewProjectPage_extensions);
		extensionsText.setText(IBeansProject.DEFAULT_CONFIG_EXTENSION);
		extensionsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});

		Group javaGroup = new Group(container, SWT.NONE);
		javaGroup.setText(BeansWizardsMessages.NewProjectPage_javaSettings);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		javaGroup.setLayout(layout);
		javaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		isJavaButton = createButton(javaGroup, SWT.CHECK, 2, 0);
		isJavaButton.setText(BeansWizardsMessages.NewProjectPage_java);
		isJavaButton.setSelection(true);
		isJavaButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = isJavaButton.getSelection();
				sourceDirLabel.setEnabled(enabled);
				sourceDirText.setEnabled(enabled);
				outputDirLabel.setEnabled(enabled);
				outputDirText.setEnabled(enabled);
				setPageComplete(validatePage());
			}
		});

		sourceDirLabel = createLabel(javaGroup,
				BeansWizardsMessages.NewProjectPage_source);
		sourceDirText = createText(javaGroup);
		IPreferenceStore store = PreferenceConstants.getPreferenceStore();
		sourceDirText.setText(store
				.getString(PreferenceConstants.SRCBIN_SRCNAME));

		outputDirLabel = createLabel(javaGroup,
				BeansWizardsMessages.NewProjectPage_output);
		outputDirText = createText(javaGroup);
		outputDirText.setText(store
				.getString(PreferenceConstants.SRCBIN_BINNAME));
	}

	private Button createButton(Composite container, int style, int span,
			int indent) {
		Button button = new Button(container, style);
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		gd.horizontalIndent = indent;
		button.setLayoutData(gd);
		return button;
	}

	private Label createLabel(Composite container, String text) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridData gd = new GridData();
		gd.horizontalIndent = 30;
		label.setLayoutData(gd);
		return label;
	}

	private Text createText(Composite container) {
		Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		text.setLayoutData(gd);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		return text;
	}

	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		if (isJavaButton.getSelection()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject dummy = workspace.getRoot().getProject("project");
			IStatus status;
			if (sourceDirText != null &&
					sourceDirText.getText().length() != 0) {
				status = workspace.validatePath(dummy.getFullPath().append(
						sourceDirText.getText()).toString(), IResource.FOLDER);
				if (!status.isOK()) {
					setErrorMessage(status.getMessage());
					return false;
				}
			}
			if (outputDirText != null &&
					outputDirText.getText().length() != 0) {
				status = workspace.validatePath(dummy.getFullPath().append(
						outputDirText.getText()).toString(), IResource.FOLDER);
				if (!status.isOK()) {
					setErrorMessage(status.getMessage());
					return false;
				}
			}
		}

		String extensions = extensionsText.getText().trim();
		if (extensions.length() == 0) {
			setErrorMessage(BeansWizardsMessages.NewProjectPage_noExtensions);
			return false;
		}
		StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
		while (tokenizer.hasMoreTokens()) {
			String extension = tokenizer.nextToken().trim();
			if (!isValidExtension(extension)) {
				setErrorMessage(BeansWizardsMessages.NewProjectPage_invalidExtensions);
				return false;
			}
		}
		return true;
	}

	private boolean isValidExtension(String extension) {
		if (extension.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < extension.length(); i++) {
				char c = extension.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					return false;
				}
			}
		}
		return true;
	}
}
