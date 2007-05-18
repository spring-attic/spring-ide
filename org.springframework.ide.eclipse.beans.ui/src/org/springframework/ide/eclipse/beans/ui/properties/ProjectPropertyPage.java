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

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
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

	@Override
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

	@Override
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

		// now rebuild the project
		if (configFilesTab.hasUserMadeChanges()
				|| configSetsTab.hasUserMadeChanges()) {
			runInBackground(project, ResourcesPlugin.getWorkspace()
					.getRuleFactory().buildRule(),
					new Object[] { ResourcesPlugin.FAMILY_MANUAL_BUILD });
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

	private void runInBackground(final IProject project, ISchedulingRule rule,
			final Object[] jobFamilies) {
		Job job = new WorkspaceJob("Build project") {

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				if (jobFamilies == null || family == null) {
					return false;
				}
				for (int i = 0; i < jobFamilies.length; i++) {
					if (family.equals(jobFamilies[i])) {
						return true;
					}
				}
				return false;
			}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				return execute(project, monitor);
			}
		};
		if (rule != null) {
			job.setRule(rule);
		}
		job.setUser(true);
		job.schedule();
	}

	private IStatus execute(IProject project, IProgressMonitor monitor) {
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e) {
			return new MultiStatus(BeansUIPlugin.PLUGIN_ID, 1,
					"Error during build of project [" + project.getName() + "]",
					e);
		}
	}
}
