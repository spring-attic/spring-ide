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

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import static org.mockito.Mockito.*;

public class TestBootDashModelContext implements BootDashModelContext {

	private File stateLoc;

	private IWorkspaceRoot root = mock(IWorkspaceRoot.class);
	private IWorkspace workspace = mock(IWorkspace.class);

	private ILaunchManager launchManager = mock(ILaunchManager.class);

	public TestBootDashModelContext() {
		try {
			stateLoc = StsTestUtil.createTempDirectory("plugin-state", null);
			when(workspace.getRoot()).thenReturn(root);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IPath getStateLocation() {
		return new Path(stateLoc.toString());
	}

	@Override
	public IWorkspace getWorkspace() {
		return workspace;
	}

	public void teardownn() throws Exception {
		FileUtils.deleteQuietly(stateLoc);
	}

	@Override
	public ILaunchManager getLaunchManager() {
		return launchManager;
	}

}
