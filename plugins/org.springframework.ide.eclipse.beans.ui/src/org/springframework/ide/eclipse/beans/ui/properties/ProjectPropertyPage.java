/*******************************************************************************
 * Copyright (c) 2006, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.progress.UIJob;
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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springsource.ide.eclipse.commons.core.SpringCorePreferences;

/**
 * Spring project property page.
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
	
	private AtomicBoolean contentCreated = new AtomicBoolean(false);
	
	private static long INIT_TIMEOUT = 40000;
	
	private static long INIT_WAIT_PERIOD = 300;

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
		
		final Composite contents = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(contents);
		contents.setLayout(new GridLayout());
		
		final CLabel statusLabel = new CLabel(contents, SWT.NONE);
		statusLabel.setText("Initializing Beans model, please wait...");
		statusLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
		GridDataFactory.fillDefaults().grab(true, false).exclude(false).applyTo(statusLabel);

		// Build folder with tabs
		final TabFolder folder = new TabFolder(contents, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).exclude(true).applyTo(folder);
		
		final Display display = parent.getDisplay();
		
		/*
		 * BeansModel may not be ready by the time preference page opened. Populate preference page contents asynchronously
		 * See: https://issuetracker.springsource.com/browse/STS-4175
		 */
		new Job("Wait for bean model initialization") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				final IProject project = (IProject) getElement();
				model = new PropertiesModel();
				PropertiesProject mp = null;
				for (long waitTime = 0; waitTime < INIT_TIMEOUT && mp == null; waitTime += INIT_WAIT_PERIOD) {
					if (BeansCorePlugin.getModel().isInitialized()) {
						mp = new PropertiesProject(model, BeansCorePlugin.getModel().getProject(project));
						// Build temporary beans core model with a cloned "real"
						// Spring project
						model.addProject(mp);
					} else {
						try {
							Thread.sleep(INIT_WAIT_PERIOD);
						} catch (InterruptedException e) {
							new UIJob("Show error on property page") {
								@Override
								public IStatus runInUIThread(IProgressMonitor arg0) {
									if (!statusLabel.isDisposed()) {
										statusLabel.setText("Failed to wait for Beans model initialization.");
										statusLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
									}
									return Status.OK_STATUS;
								}

							}.schedule();
							return new Status(IStatus.ERROR, BeansUIPlugin.PLUGIN_ID,
									"Failed to initialize preference page content", e);
						}
					}
				}
				final PropertiesProject modelProject = mp;
				new UIJob(display, "Create Preference Page Content") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (modelProject == null) {
							if (!statusLabel.isDisposed()) {
								statusLabel.setText("Timed out waiting for Beans model initialization.");
								statusLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
							}
						} else {
							if (!folder.isDisposed()) {
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

								// Open the scan dialog if required if
								// coming from a nature added event
								if (shouldTriggerScan) {
									configFilesTab.handleScanButtonPressed();
								}
								folder.addDisposeListener(new DisposeListener() {
									public void widgetDisposed(DisposeEvent arg0) {
										if (configFilesTab != null) {
											configFilesTab.dispose();
										}
										if (configSetsTab != null) {
											configSetsTab.dispose();
										}
									}
								});
								statusLabel.dispose();
								// Pack shell if initial control size == preferred size. It's very likely that this is the first opened page. 
								GridDataFactory.fillDefaults().grab(true, true).exclude(false).applyTo(folder);
								contents.layout();
								// Adjust sroll-bars
								Composite c = contents.getParent();
								while (c != null && !(c instanceof ScrolledComposite)) {
									c = c.getParent();
								}
								if (c != null) {
									ScrolledComposite sc = (ScrolledComposite) c;
									sc.setMinSize(getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT));
								}
								getControl().getShell().pack();
								contentCreated.set(true);
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
				return Status.OK_STATUS;
			}
		}.schedule();
		return contents;
	}

	@Override
	public boolean performOk() {
		if (contentCreated.get()) {
			IProject project = (IProject) getElement();
			IBeansProject currentProject = BeansCorePlugin.getModel().getProject(project);
			boolean userMadeChanges = configFilesTab.hasUserMadeChanges() || configSetsTab.hasUserMadeChanges();

			PropertiesProject newProject = (PropertiesProject) model.getProject(project);

			// At first delete all problem markers from the removed config files
			if (configFilesTab.hasUserMadeChanges()) {
				for (IBeansConfig currentConfig : currentProject.getConfigs()) {
					if (!newProject.hasConfig(currentConfig.getElementName())) {
						MarkerUtils.deleteAllMarkers(currentConfig.getElementResource(), SpringCore.MARKER_ID);
					}
				}
			}

			// Now save modified project description
			if (userMadeChanges) {
				SpringCorePreferences.getProjectPreferences(project.getProject(), BeansCorePlugin.PLUGIN_ID).putBoolean(
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
		}
		return super.performOk();
	}

	@SuppressWarnings("unchecked")
	public void applyData(Object data) {
		super.applyData(data);
		if (data instanceof Map) {
			this.pageData = (Map<String, Object>) data;
			if (this.pageData.containsKey(BLOCK_ID)) {
				this.selectedTab = (Integer) this.pageData.get(BLOCK_ID);
			}
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
