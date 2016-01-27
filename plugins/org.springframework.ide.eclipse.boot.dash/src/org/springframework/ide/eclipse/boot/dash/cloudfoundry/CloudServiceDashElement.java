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

import org.cloudfoundry.client.lib.domain.CloudService;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class CloudServiceDashElement extends WrappingBootDashElement<String> {

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME};

	private final CloudService service;
	private final PropertyStoreApi persistentProperties;

	public CloudServiceDashElement(CloudFoundryBootDashModel parent, CloudService service, IPropertyStore modelStore) {
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

}
