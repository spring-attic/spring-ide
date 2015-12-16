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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.util.FactoryWithParam;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

import com.google.common.collect.ImmutableSet;

/**
 * Concrete {@link BootDashElement} that wraps a launch config.
 *
 * @author Kris De Volder
 */
public class LaunchConfDashElement extends AbstractLaunchConfigurationsDashElement<ILaunchConfiguration, LaunchConfDashElement> implements ElementStateListener {

	public LaunchConfDashElement(LocalBootDashModel bootDashModel, ILaunchConfiguration delegate) {
		super(bootDashModel, delegate);
	}

	@Override
	protected FactoryWithParam<ILaunchConfiguration, LaunchConfDashElement> getFactory() {
		return getBootDashModel().getLaunchConfElementFactory();
	}

	@Override
	protected IPropertyStore createPropertyStore() {
		return PropertyStoreFactory.createFor(delegate);
	}

	@Override
	protected ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		return ImmutableSet.of(delegate);
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
	public IProject getProject() {
		return BootLaunchConfigurationDelegate.getProject(delegate);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	protected ImmutableSet<ILaunch> getLaunches() {
		return ImmutableSet.copyOf(LaunchUtils.getLaunches(delegate));
	}

}
