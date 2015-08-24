/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

public class ApplicationDeploymentPropertiesWizard extends Wizard {

	private final IProject project;
	private final Map<String, CloudDomain> domainsAsString = new LinkedHashMap<String, CloudDomain>();
	private CloudApplicationDeploymentProperties properties;
	private CloudApplicationURL url = null;

	public ApplicationDeploymentPropertiesWizard(IProject project, List<CloudDomain> domains) {
		this.project = project;
		if (domains != null && domains.size() > 0) {
			url = new CloudApplicationURL(project.getName(), domains.get(0).getName());
			for (CloudDomain dom : domains) {
				domainsAsString.put(dom.getName(), dom);
			}
		}
		properties = new CloudApplicationDeploymentProperties(project);

		// set some default values
		properties.setAppName(project.getName());
		properties.setInstances(1);
		properties.setMemory(1024);
		if (url != null) {
			properties.setUrls(Arrays.asList(new String[] { url.getUrl() }));
		}
	}

	@Override
	public void addPages() {
		addPage(new DeploymentPropertiesPage());
	}

	public CloudApplicationDeploymentProperties getProperties() {
		return properties;
	}

	public class DeploymentPropertiesPage extends WizardPage {

		private Combo domainCombo;

		private Text hostText;

		protected DeploymentPropertiesPage() {
			super("Enter Application Deployment Properties");
			setTitle("Enter Application Deployment Properties");
			setDescription(
					"Please enter deployment properties like the application name and host for the project: " + project.getName());
			setImageDescriptor(BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));

		}

		public void createControl(Composite parent) {

			Composite composite = new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
			GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(composite);

			Label appNameLabel = new Label(composite, SWT.NONE);
			appNameLabel.setText("Application Name:");
			GridDataFactory.fillDefaults().grab(false, false).applyTo(appNameLabel);

			final Text appNameText = new Text(composite, SWT.BORDER);
			if (properties.getAppName() != null) {
				appNameText.setText(properties.getAppName());
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(appNameText);

			appNameText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					properties.setAppName(appNameText.getText());
					update();
				}
			});

			Label hostNameLabel = new Label(composite, SWT.NONE);
			hostNameLabel.setText("Host:");
			GridDataFactory.fillDefaults().grab(false, false).applyTo(hostNameLabel);

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
			GridDataFactory.fillDefaults().grab(false, false).applyTo(domainLabel);

			domainCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);

			GridDataFactory.fillDefaults().applyTo(domainCombo);

			domainCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setUrl();
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

			update();
		}

		private void update() {

			setErrorMessage(null);

			IStatus status = validateProperties();
			if (!status.isOK()) {
				setErrorMessage(status.getMessage());
			}

			if (getWizard() != null && getWizard().getContainer() != null) {
				getWizard().getContainer().updateButtons();
				setPageComplete(status.isOK());
			}
		}

		@Override
		public boolean isPageComplete() {
			return validateProperties().isOK();
		}

	}

	@Override
	public boolean performFinish() {
		return validateProperties().isOK();
	}

	protected IStatus validateProperties() {
		IStatus status = Status.OK_STATUS;
		String error = null;
		if (properties.getAppName() == null || properties.getAppName().trim().length() == 0) {
			error = "Please enter an application name";
		} else if (properties.getUrls() == null || properties.getUrls().isEmpty()) {
			error = "Please specify a host and domain for the application URL";
		}

		if (error != null) {
			status = BootDashActivator.createErrorStatus(null, error);
		}
		return status;

	}
}
