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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.OperationsExecution;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ProjectsDeployer;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class CloudFoundryBootDashModel extends BootDashModel implements ModifiableModel {

	IPropertyStore modelStore;

	private OperationsExecution opExecution;

	private ProjectAppStore projectAppStore;

	private CloudAppCache cloudAppCache;

	public static final String PROJECT_TO_APP_MAPPING = "projectToAppMapping";

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context) {
		super(target);
		RunTargetType type = target.getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(target.getId(), typeStore);
		this.opExecution = new OperationsExecution();
		this.projectAppStore = new ProjectAppStore(this.modelStore);
		this.cloudAppCache = new CloudAppCache(this);
	}

	LiveSet<BootDashElement> elements;

	public CloudAppCache getAppCache() {
		return cloudAppCache;
	}

	@Override
	public LiveSet<BootDashElement> getElements() {

		if (elements == null) {
			elements = new LiveSet<BootDashElement>();

			asyncRefreshElements();
		}
		return elements;
	}

	protected void asyncRefreshElements() {
		if (elements == null) {
			return;
		}

		Operation<Void> op = new CloudApplicationRefreshOperation(this);
		opExecution.runOpAsynch(op);
	}

	@Override
	public void dispose() {
		elements = null;
	}

	@Override
	public void refresh() {
		asyncRefreshElements();
	}

	public CloudFoundryRunTarget getCloudTarget() {
		return (CloudFoundryRunTarget) getRunTarget();
	}

	@Override
	public boolean canBeAdded(List<Object> source, Object target) {
		if (!source.isEmpty()) {
			for (Object obj : source) {
				// IMPORTANT: to avoid drag/drop into the SAME target, be sure
				// all sources are from a different target
				if (getProject(obj) == null || !isFromDifferentTarget(obj)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void add(List<Object> sources, Object target, UserInteractions ui) throws Exception {
		// For now, only support new deployments (no mapping to existing apps in
		// CF)
		// Therefore ignore the target
		Map<IProject, BootDashElement> projects = new LinkedHashMap<IProject, BootDashElement>();
		if (sources != null) {
			for (Object obj : sources) {
				IProject project = null;
				if (obj instanceof IProject) {
					project = (IProject) obj;
				} else if (obj instanceof IJavaProject) {
					project = ((IJavaProject) obj).getProject();
				} else if (obj instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
				} else if (obj instanceof BootDashElement) {
					project = ((BootDashElement) obj).getProject();
				}

				if (project != null) {
					projects.put(project, null);
				}
			}
			BootDashElement element = target instanceof BootDashElement ? (BootDashElement) target : null;

			// If only one project was drag/dropped to element, map it
			Iterator<Entry<IProject, BootDashElement>> it = projects.entrySet().iterator();
			if (projects.size() == 1 && it.hasNext()) {
				Entry<IProject, BootDashElement> entry = it.next();
				entry.setValue(element);
			}

			performDeployment(projects, ui);
		}
	}

	protected IProject getProject(Object source) {
		if (source instanceof IProject) {
			return (IProject) source;
		} else if (source instanceof IJavaProject) {
			return ((IJavaProject) source).getProject();
		} else if (source instanceof BootDashElement) {
			return ((BootDashElement) source).getProject();
		}
		return null;
	}

	protected boolean isFromDifferentTarget(Object dropSource) {
		if (dropSource instanceof BootDashElement) {
			return ((BootDashElement) dropSource).getParent() != this;
		}

		return false;
	}

	public void performDeployment(final Map<IProject, BootDashElement> projectsToDeploy, final UserInteractions ui)
			throws Exception {

		final CloudFoundryOperations client = CloudFoundryBootDashModel.this.getCloudTarget().getClient();

		// When deploying or mapping app to project, always prompt user if they
		// want to replace the existing app
		boolean shouldAutoReplaceApp = false;

		opExecution.runOpAsynch(new ProjectsDeployer(CloudFoundryBootDashModel.this, client, ui, projectsToDeploy,
				shouldAutoReplaceApp));

	}

	public synchronized BootDashElement addElement(CloudApplication app, IProject project) throws Exception {

		Set<BootDashElement> existing = elements.getValue();

		Set<BootDashElement> updated = new HashSet<BootDashElement>();

		BootDashElement addedElement = new CloudDashElement(this, app.getName(), project, opExecution, modelStore);

		updated.add(addedElement);

		// Add any existing ones that weren't replaced by the new ones
		// Replace the existing one with a new one for the given Cloud
		// Application
		for (BootDashElement element : existing) {
			if (!addedElement.getName().equals(element.getName())) {
				updated.add(element);
			}
		}

		elements.replaceAll(updated);
		projectAppStore.storeProjectToAppMapping(updated);

		notifyElementChanged(addedElement);
		return addedElement;
	}

	public synchronized CloudDashElement getElement(String appName) {

		Set<BootDashElement> existing = elements.getValue();

		// Add any existing ones that weren't replaced by the new ones
		// Replace the existing one with a new one for the given Cloud
		// Application
		for (BootDashElement element : existing) {
			if (appName.equals(element.getName()) && element instanceof CloudDashElement) {
				return (CloudDashElement) element;
			}
		}

		return null;
	}

	/**
	 *
	 * @param apps
	 *            updated list of applications. Elements will be created for the
	 *            Cloud applications and replace all existing elements in the
	 *            model
	 * @throws Exception
	 */
	public synchronized void replaceElements(Map<CloudApplication, IProject> apps) throws Exception {

		List<BootDashElement> updated = new ArrayList<BootDashElement>();

		// Add new ones first
		for (Entry<CloudApplication, IProject> entry : apps.entrySet()) {
			BootDashElement addedElement = new CloudDashElement(this, entry.getKey().getName(), entry.getValue(),
					opExecution, modelStore);
			updated.add(addedElement);
		}

		elements.replaceAll(updated);
		projectAppStore.storeProjectToAppMapping(updated);
		getAppCache().updateAll(apps.keySet());

	}

	public ProjectAppStore getProjectToAppMappingStore() {
		return projectAppStore;
	}

	public OperationsExecution getCloudOpExecution() {
		return opExecution;
	}

	@Override
	public void delete(List<BootDashElement> toRemove, UserInteractions ui) {

		if (toRemove == null || toRemove.isEmpty()) {
			return;
		}

		if (ui.confirmOperation("Deleting Applications",
				"Are you sure that you want to delete the selected applications from this target? The applications will be permanently removed.")) {

			Set<BootDashElement> existing = elements.getValue();

			Set<BootDashElement> updated = new HashSet<BootDashElement>();

			Set<String> toRemoveNames = new HashSet<String>();

			for (BootDashElement element : toRemove) {
				if (element instanceof CloudDashElement) {
					CloudDashElement cloudElement = (CloudDashElement) element;
					try {
						cloudElement.delete(ui);
						toRemoveNames.add(element.getName());

					} catch (Exception e) {
						// Allow deletion to continue
						BootDashActivator.log(e);
					}
				}
			}

			// Add any existing ones that weren't replaced by the new ones
			// Replace the existing one with a new one for the given Cloud
			// Application
			for (BootDashElement element : existing) {
				if (!toRemoveNames.contains(element.getName())) {
					updated.add(element);
				}
			}

			elements.replaceAll(updated);
			try {
				projectAppStore.storeProjectToAppMapping(updated);
			} catch (Exception e) {
				ui.errorPopup("Error saving project to application mappings", e.getMessage());
			}
		}

	}

}
