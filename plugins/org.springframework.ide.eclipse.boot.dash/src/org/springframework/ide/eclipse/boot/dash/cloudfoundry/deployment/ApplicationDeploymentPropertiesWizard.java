/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationURL;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class ApplicationDeploymentPropertiesWizard extends Wizard {

	private final IProject project;
	private final Map<String, CloudDomain> domainsAsString = new LinkedHashMap<String, CloudDomain>();
	private CloudApplicationDeploymentProperties properties;
	private CloudApplicationURL url = null;
	private DeploymentPropertiesPage page;

	public ApplicationDeploymentPropertiesWizard(IProject project, List<CloudDomain> domains) {
		this.project = project;
		if (domains != null && domains.size() > 0) {
			url = new CloudApplicationURL(project.getName(), domains.get(0).getName());
			for (CloudDomain dom : domains) {
				domainsAsString.put(dom.getName(), dom);
			}
		}
		properties = new CloudApplicationDeploymentProperties();
		properties.getValidator().addListener(new WizardValidator());

		properties.setProject(project);

		// set some default values
		properties.setAppName(project.getName());

		if (url != null) {
			properties.setUrls(Arrays.asList(new String[] { url.getUrl() }));
		}
	}

	@Override
	public void addPages() {
		addPage(this.page = new DeploymentPropertiesPage());
	}

	public CloudApplicationDeploymentProperties getProperties() {
		return properties;
	}

	public class DeploymentPropertiesPage extends WizardPage {

		private Combo domainCombo;

		private Text hostText;

		private Text memoryText;

		protected DeploymentPropertiesPage() {
			super("Enter Application Deployment Properties");
			setTitle("Enter Application Deployment Properties");
			setDescription("Please enter deployment properties like the application name and host for the project: "
					+ project.getName());
			setImageDescriptor(BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));

		}

		public void createControl(Composite parent) {

			Composite composite = new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
			GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(composite);

			Label appNameLabel = new Label(composite, SWT.NONE);
			appNameLabel.setText("Application Name:");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(appNameLabel);

			final Text appNameText = new Text(composite, SWT.BORDER);
			if (properties.getAppName() != null) {
				appNameText.setText(properties.getAppName());
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(appNameText);

			appNameText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					properties.setAppName(appNameText.getText());
				}
			});

			Label hostNameLabel = new Label(composite, SWT.NONE);
			hostNameLabel.setText("Host:");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(hostNameLabel);

			hostText = new Text(composite, SWT.BORDER);
			if (url != null && url.getSubdomain() != null) {
				hostText.setText(url.getSubdomain());
			}
			GridDataFactory.fillDefaults().grab(false, false).applyTo(hostText);

			hostText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					setUrl();
				}

			});

			Label domainLabel = new Label(composite, SWT.NONE);
			domainLabel.setText("Domain:");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(domainLabel);

			domainCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);

			GridDataFactory.fillDefaults().applyTo(domainCombo);

			domainCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setUrl();
				}
			});

			Label memoryLabel = new Label(composite, SWT.NONE);
			memoryLabel.setText("Memory (Mb):");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(memoryLabel);

			memoryText = new Text(composite, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(memoryText);
			memoryText.setText(properties.getMemory() < 0 ? "" : String.valueOf(properties.getMemory()));
			/*
			 * Text length limit is set such that it would always be a number
			 * less than MAX_INT Note that only numbers are accepted input chars
			 */
			memoryText.setTextLimit(9);
			memoryText.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					String string = e.text;
					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9')) {
							e.doit = false;
							return;
						}
					}
				}
			});
			memoryText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (memoryText.getText().isEmpty()) {
						properties.setMemory(-1);
					} else {
						properties.setMemory(Integer.valueOf(memoryText.getText()));
					}
				}
			});

			final Button saveManifest = new Button(composite, SWT.CHECK);
			saveManifest.setText("Save to manifest file");
			GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).grab(false, false).applyTo(saveManifest);
			saveManifest.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					properties.setWriteManifest(saveManifest.getSelection());
				}

			});

			String[] comboItems = new String[domainsAsString.keySet().size()];
			int i = 0;
			for (String domainName : domainsAsString.keySet()) {
				if (i < comboItems.length) {
					comboItems[i] = domainName;
				}
				i++;
			}

			domainCombo.setItems(comboItems);
			if (comboItems.length > 0) {
				domainCombo.select(0);
			}
			setUrl();
			Dialog.applyDialogFont(composite);
			setControl(composite);

		}

		private void setUrl() {
			CloudApplicationURL updatedUrl = null;
			if (domainCombo != null && !domainCombo.isDisposed()) {
				int index = domainCombo.getSelectionIndex();

				if (index >= 0) {
					String domainName = domainCombo.getItem(index);
					if (hostText != null && hostText.getText() != null && hostText.getText().trim().length() > 0) {
						updatedUrl = new CloudApplicationURL(hostText.getText(), domainName);
					}
				}

			}

			ApplicationDeploymentPropertiesWizard.this.url = updatedUrl;

			if (url != null) {
				try {
					// validate the URL but do not add the full URL to the list
					// (i.e. omit the protocol)
					new URL("http://" + url.getUrl());
					properties.setUrls(Arrays.asList(new String[] { url.getUrl() }));
				} catch (MalformedURLException e) {
					setErrorMessage(e.getMessage());
					return;
				}
			} else {
				properties.setUrls(new ArrayList<String>(0));
			}

		}

		@Override
		public boolean isPageComplete() {
			return isValid();
		}

	}

	@Override
	public boolean performFinish() {
		return isValid();
	}

	protected boolean isValid() {
		return properties.getValidator().getValue() != null && properties.getValidator().getValue().isOk();
	}

	class WizardValidator implements ValueListener<ValidationResult> {

		@Override
		public void gotValue(LiveExpression<ValidationResult> exp, ValidationResult status) {

			if (status == null) {
				status = ValidationResult.OK;
			}

			if (ApplicationDeploymentPropertiesWizard.this.page == null) {
				return;
			}

			String message = status.isOk() ? null : status.msg;

			ApplicationDeploymentPropertiesWizard.this.page.setErrorMessage(message);

			if (ApplicationDeploymentPropertiesWizard.this.getContainer() != null) {
				ApplicationDeploymentPropertiesWizard.this.getContainer().updateButtons();
				ApplicationDeploymentPropertiesWizard.this.page.setPageComplete(status.isOk());
			}

		}

	}

}
