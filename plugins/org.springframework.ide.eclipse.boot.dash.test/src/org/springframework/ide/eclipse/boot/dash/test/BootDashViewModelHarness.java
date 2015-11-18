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
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Assert;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockPropertyStore;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockSecuredCredentialStore;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BootDashViewModelHarness {

	public final BootDashModelContext context;
	public BootDashViewModel model;
	private RunTargetType[] types;

	public BootDashViewModelHarness(BootDashModelContext context, RunTargetType... types) throws Exception {
		this.types = types;
		this.context = context;
		this.model = new BootDashViewModel(context, types);
	}

	/**
	 * Dipose model and reinitialze it reusing the same stores (for testing functionality
	 * around persisting stuff)
	 */
	public void reload() {
		dispose();
		this.model = new BootDashViewModel(context, types);
	}

	public BootDashViewModelHarness(RunTargetType... types) throws Exception {
		this(new MockContext(), types);
	}

	public static class MockContext implements BootDashModelContext {

		private IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		private IScopedPropertyStore<RunTargetType> runtargetProperties = new MockPropertyStore<RunTargetType>();
		private SecuredCredentialsStore secureStore = new MockSecuredCredentialStore();
		private File stateLocation;
		private LiveVariable<Pattern> bootProjectExclusion = new LiveVariable<>(BootPreferences.DEFAULT_BOOT_PROJECT_EXCLUDE);

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

		@Override
		public LiveExpression<Pattern> getBootProjectExclusion() {
			return bootProjectExclusion;
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

	public void dispose() {
		model.dispose();
	}

	public List<RunTarget> getRunTargets() {
		return model.getRunTargets().getValues();
	}

	public RunTarget getRunTarget(RunTargetType targetType) {
		List<RunTarget> targets = getRunTargets(targetType);
		Assert.assertEquals(1, targets.size());
		return targets.get(0);
	}

	private List<RunTarget> getRunTargets(RunTargetType targetType) {
		ArrayList<RunTarget> list = new ArrayList<RunTarget>();
		for (RunTarget runTarget : model.getRunTargets().getValues()) {
			if (runTarget.getType().equals(targetType)) {
				list.add(runTarget);
			}
		}
		return list;
	}

}
