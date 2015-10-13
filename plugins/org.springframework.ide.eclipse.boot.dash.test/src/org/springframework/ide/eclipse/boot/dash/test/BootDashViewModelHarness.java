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
package org.springframework.ide.eclipse.boot.dash.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Assert;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BootDashViewModelHarness {

	public final  MockContext context;
	public final BootDashViewModel model;

	public BootDashViewModelHarness(RunTargetType... types) throws Exception {
		this.context = new MockContext();
		this.model = new BootDashViewModel(context, types);
	}

	public static class MockContext implements BootDashModelContext {

		private IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		private IScopedPropertyStore<RunTargetType> runtargetProperties = new MockPropertyStore<RunTargetType>();
		private SecuredCredentialsStore secureStore = new MockSecuredCredentialStore();
		private File stateLocation;

		public MockContext() throws Exception {
			stateLocation = StsTestUtil.createTempDirectory();
		}

		@Override
		public IWorkspace getWorkspace() {
			return ResourcesPlugin.getWorkspace();
		}

		@Override
		public ILaunchManager getLaunchManager() {
			return DebugPlugin.getDefault().getLaunchManager();
		}

		@Override
		public IPath getStateLocation() {
			return new Path(stateLocation.toString());
		}

		@Override
		public IScopedPropertyStore<IProject> getProjectProperties() {
			return projectProperties;
		}

		@Override
		public IScopedPropertyStore<RunTargetType> getRunTargetProperties() {
			return runtargetProperties;
		}

		@Override
		public SecuredCredentialsStore getSecuredCredentialsStore() {
			return secureStore;
		}

		@Override
		public void log(Exception e) {
		}

	}

	public BootDashModel getRunTargetModel(RunTargetType type) {
		List<BootDashModel> models = getRunTargetModels(type);
		Assert.assertEquals(1, models.size());
		return models.get(0);
	}

	public List<BootDashModel> getRunTargetModels(RunTargetType type) {
		ArrayList<BootDashModel> models = new ArrayList<BootDashModel>();
		for (BootDashModel m : model.getSectionModels().getValue()) {
			if (m.getRunTarget().getType().equals(type)) {
				models.add(m);
			}
		}
		return Collections.unmodifiableList(models);
	}


}
