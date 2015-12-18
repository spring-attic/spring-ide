/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockSecuredCredentialStore;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class TestBootDashModelContext implements BootDashModelContext {

	private File stateLoc;
	private ILaunchManager launchManager;
	private IWorkspace workspace;
	private IScopedPropertyStore<IProject> projectProperties;
	private IScopedPropertyStore<RunTargetType> runTargetProperties;
	private LiveVariable<Pattern> bootProjectExclusion = new LiveVariable<>(BootPreferences.DEFAULT_BOOT_PROJECT_EXCLUDE);

	public TestBootDashModelContext(IWorkspace workspace, ILaunchManager launchMamager) {
		try {
			this.workspace = workspace;
			this.launchManager = launchMamager;
			stateLoc = StsTestUtil.createTempDirectory("plugin-state", null);
			this.projectProperties = new MockScopedPropertyStore<IProject>();
			this.runTargetProperties = new MockScopedPropertyStore<RunTargetType>();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public IPath getStateLocation() {
		return new Path(stateLoc.toString());
	}

	public IWorkspace getWorkspace() {
		return workspace;
	}

	public void teardownn() throws Exception {
		FileUtils.deleteQuietly(stateLoc);
	}

	public ILaunchManager getLaunchManager() {
		return launchManager;
	}

	@Override
	public void log(Exception e) {
		// No implementation we'll use Mockito to spy on the method instead.
	}

	@Override
	public IScopedPropertyStore<IProject> getProjectProperties() {
		return projectProperties;
	}

	@Override
	public IScopedPropertyStore<RunTargetType> getRunTargetProperties() {
		return runTargetProperties;
	}

	@Override
	public SecuredCredentialsStore getSecuredCredentialsStore() {
		return new MockSecuredCredentialStore();
	}

	@Override
	public LiveExpression<Pattern> getBootProjectExclusion() {
		return bootProjectExclusion;
	}

}
