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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
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

	public static final String PROJECT_TO_APP_MAPPING = "projectToAppMapping";

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context) {
		super(target);
		RunTargetType type = target.getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(target.getId(), typeStore);
		this.opExecution = new OperationsExecution();
		this.projectAppStore = new ProjectAppStore(this.modelStore);
	}

	LiveSet<BootDashElement> elements;

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
		opExecution.runOp(op);
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
	public boolean canAccept(List<Object> source, Object target) {
		if (!source.isEmpty()) {
			for (Object obj : source) {
				if (getJavaProject(obj) == null) {
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
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		if (sources != null) {
			for (Object obj : sources) {
				IJavaProject javaProject = getJavaProject(obj);
				if (javaProject != null) {
					javaProjects.add(javaProject);
				}
			}

			performDeployment(javaProjects, ui);
		}
	}

	protected IJavaProject getJavaProject(Object source) {
		if (source instanceof IJavaProject) {
			return (IJavaProject) source;
		} else if (source instanceof BootDashElement) {
			return ((BootDashElement) source).getJavaProject();
		}
		return null;
	}

	public void performDeployment(final List<IJavaProject> projectsToDeploy, final UserInteractions ui)
			throws Exception {

		final CloudFoundryOperations client = CloudFoundryBootDashModel.this.getCloudTarget().getClient();
		Job job = new Job("Deploying projects to " + getRunTarget().getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				new ApplicationDeployer(CloudFoundryBootDashModel.this, client, ui, projectsToDeploy).deployAndStart(monitor);
				return Status.OK_STATUS;
			}

		};
		job.schedule();

	}

	public synchronized void addElement(CloudApplication app, IProject project) throws Exception {

		Set<BootDashElement> existing = elements.getValue();

		Set<BootDashElement> updated = new HashSet<BootDashElement>();

		BootDashElement addedElement = new CloudDashElement(this, app, project, opExecution, modelStore);

		// Add any existing ones that weren't replaced by the new ones
		// Replace the existing one with a new one for the given Cloud
		// Application
		for (BootDashElement element : existing) {
			if (!addedElement.getName().equals(element.getName())) {
				updated.add(element);
			}
		}

		elements.addAll(updated);
		projectAppStore.storeProjectToAppMapping(updated);
		notifyElementChanged(addedElement);
	}

	public synchronized void addElements(Map<CloudApplication, IProject> apps) throws Exception {

		Set<BootDashElement> existing = elements.getValue();

		Map<String, BootDashElement> updated = new HashMap<String, BootDashElement>();

		// Add new ones first
		for (Entry<CloudApplication, IProject> entry : apps.entrySet()) {
			BootDashElement addedElement = new CloudDashElement(this, entry.getKey(), entry.getValue(), opExecution,
					modelStore);
			updated.put(addedElement.getName(), addedElement);

		}

		// Add any existing ones that weren't replaced by the new ones
		// Replace the existing one with a new one for the given Cloud
		// Application
		for (BootDashElement element : existing) {
			if (updated.get(element.getName()) == null) {
				updated.put(element.getName(), element);
			}
		}

		elements.replaceAll(updated.values());
		projectAppStore.storeProjectToAppMapping(updated.values());
	}

	public ProjectAppStore getProjectToAppMappingStore() {
		return projectAppStore;
	}

	public OperationsExecution getCloudOpExecution() {
		return opExecution;
	}

}
