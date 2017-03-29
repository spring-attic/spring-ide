/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.launch.cli.CloudCliServiceLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

/**
 * Spring Cloud CLI local service boot dash element implementation
 *
 * @author Alex Boyko
 *
 */
public class LocalCloudServiceDashElement extends AbstractLaunchConfigurationsDashElement<String> {

	private static final EnumSet<RunState> LOCAL_CLOUD_SERVICE_RUN_GOAL_STATES = EnumSet.of(INACTIVE, RUNNING);

	private static final BootDashColumn[] COLUMNS = {BootDashColumn.NAME, BootDashColumn.LIVE_PORT, BootDashColumn.RUN_STATE_ICN};

	private static final LoadingCache<String, ILaunchConfigurationWorkingCopy> LAUNCH_CONFIG_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<String, ILaunchConfigurationWorkingCopy>() {
		@Override
		public ILaunchConfigurationWorkingCopy load(String key) throws Exception {
			return CloudCliServiceLaunchConfigurationDelegate.createLaunchConfig(key);
		}
	});

	public LocalCloudServiceDashElement(LocalBootDashModel bootDashModel, String id) {
		super(bootDashModel, id);
	}

	@Override
	public IProject getProject() {
		return null;
	}

	public ImmutableSet<ILaunch> getLaunches() {
		List<ILaunch> launches = new ArrayList<>();
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(CloudCliServiceLaunchConfigurationDelegate.TYPE_ID);
		for (ILaunch launch : launchManager.getLaunches()) {
			ILaunchConfiguration configuration = launch.getLaunchConfiguration();
			try {
				if (configuration!=null && configuration.getType() == type && delegate.equals(configuration.getAttribute(CloudCliServiceLaunchConfigurationDelegate.ATTR_CLOUD_SERVICE_ID, (String) null))) {
					launches.add(launch);
				}
			} catch (CoreException e) {
				Log.log(e);
			}
		}
		return ImmutableSet.copyOf(launches);
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
	public Object getParent() {
		return getBootDashModel();
	}

	@Override
	public String getName() {
		return delegate;
	}

	@Override
	public BootDashColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public LocalBootDashModel getBootDashModel() {
		return (LocalBootDashModel) super.getBootDashModel();
	}

	public String getId() {
		return delegate;
	}

	@Override
	protected IPropertyStore createPropertyStore() {
		return PropertyStoreFactory.createSubStore("S-"+delegate, getBootDashModel().getModelStore());
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		try {
			return ImmutableSet.of(LAUNCH_CONFIG_CACHE.get(delegate));
		} catch (ExecutionException e) {
			Log.log(e);
			return ImmutableSet.of();
		}
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return LOCAL_CLOUD_SERVICE_RUN_GOAL_STATES;
	}

}
