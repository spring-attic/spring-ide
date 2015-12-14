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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

/**
 * Concrete {@link BootDashElement} that wraps a launch config.
 *
 * @author Kris De Volder
 */
public class BootDashLaunchConfElement extends WrappingBootDashElement<ILaunchConfiguration> {

	private PropertyStoreApi persistentProperties;

	public BootDashLaunchConfElement(BootDashModel bootDashModel, ILaunchConfiguration delegate) {
		super(bootDashModel, delegate);
		IPropertyStore backingStore = PropertyStoreFactory.createFor(delegate);
		this.persistentProperties = PropertyStoreFactory.createApi(backingStore);
	}

	@Override
	public IProject getProject() {
		return BootLaunchConfigurationDelegate.getProject(delegate);
	}

	@Override
	public RunState getRunState() {
		// TODO Auto-generated method stub
		return RunState.UNKNOWN;
	}

	@Override
	public RunTarget getTarget() {
		return getBootDashModel().getRunTarget();
	}

	@Override
	public int getLivePort() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public String getLiveHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return delegate;
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		return delegate;
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {
		//This operation is not supported since the element is tied to a specific launch config
		//For convenience we allow the caller to set the element anyways as long as they set it
		//to its only legal value.
		Assert.isLegal(delegate.equals(config));
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void openConfig(UserInteractions ui) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getActualInstances() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}


}
