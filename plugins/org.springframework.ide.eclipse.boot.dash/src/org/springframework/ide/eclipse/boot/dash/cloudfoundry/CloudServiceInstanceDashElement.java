/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Deletable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class CloudServiceInstanceDashElement extends WrappingBootDashElement<String> implements Deletable {

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME, BootDashColumn.TAGS};

	private final CFServiceInstance service;
	private final PropertyStoreApi persistentProperties;

	public CloudServiceInstanceDashElement(AbstractBootDashModel parent, CFServiceInstance service, IPropertyStore modelStore) {
		super(parent, service.getName()+"@"+parent.getRunTarget().getId());
		this.service = service;
		IPropertyStore backingStore = PropertyStoreFactory.createSubStore("S"+getName(), modelStore);
		this.persistentProperties = PropertyStoreFactory.createApi(backingStore);
	}

	@Override
	public CloudFoundryRunTarget getTarget() {
		return getBootDashModel().getRunTarget();
	}

	@Override
	public CloudFoundryBootDashModel getBootDashModel() {
		return (CloudFoundryBootDashModel) super.getBootDashModel();
	}

	@Override
	public Object getParent() {
		return getBootDashModel();
	}

	@Override
	public int getLivePort() {
		return -1;
	}

	@Override
	public String getLiveHost() {
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return null;
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
	}

	@Override
	public void openConfig(UserInteractions ui) {
	}

	@Override
	public int getActualInstances() {
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		return 0;
	}

	@Override
	public String getName() {
		return service.getName();
	}

	@Override
	public BootDashColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		return null;
	}

	@Override
	public String getUrl() {
		return service != null ? service.getDashboardUrl() : null;
	}

	public String getDocumentationUrl() {
		return service!=null ? service.getDocumentationUrl() : null;
	}

	public CFServiceInstance getCloudService() {
		return service;
	}

	public String getPlan() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getPlan();
	}

	public String getService() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getService();
	}

	public String getDescription() {
		CFServiceInstance s = getCloudService();
		return s==null?null:s.getDescription();
	}

	@Override
	public void delete(UserInteractions ui) {
		CloudFoundryBootDashModel model = getBootDashModel();
		cancelOperations();
		String serviceName = getName();
		model.runAsynch("Deleting service: " + serviceName, serviceName, (IProgressMonitor monitor) -> {
			getClient().deleteService(serviceName);
			model.removeService(serviceName);
		}, ui);
	}

	private ClientRequests getClient() {
		return getTarget().getClient();
	}

}
