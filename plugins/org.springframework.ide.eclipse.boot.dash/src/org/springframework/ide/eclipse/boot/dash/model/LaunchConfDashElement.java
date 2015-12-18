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
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

import com.google.common.collect.ImmutableSet;

/**
 * Concrete {@link BootDashElement} that wraps a launch config.
 *
 * @author Kris De Volder
 */
public class LaunchConfDashElement extends AbstractLaunchConfigurationsDashElement<ILaunchConfiguration> {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	public LaunchConfDashElement(LocalBootDashModel bootDashModel, ILaunchConfiguration delegate) {
		super(bootDashModel, delegate);
	}

	@Override
	protected IPropertyStore createPropertyStore() {
		return PropertyStoreFactory.createFor(delegate);
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		return ImmutableSet.of(delegate);
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

	@Override
	public void dispose() {
		super.dispose();
		debug("Disposing: "+this);
	}

}
