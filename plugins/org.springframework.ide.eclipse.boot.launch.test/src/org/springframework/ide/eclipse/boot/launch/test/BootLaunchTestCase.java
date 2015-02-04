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
package org.springframework.ide.eclipse.boot.launch.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

/**
 * @author Kris De Volder
 */
public class BootLaunchTestCase extends StsTestCase {

	/**
	 * Create an empty project no nature, no nothing
	 */
	public static IProject createGeneralProject(String name) throws Exception {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		p.create(new NullProgressMonitor());
		p.open(new NullProgressMonitor());
		assertTrue(p.exists());
		assertTrue(p.isAccessible());
		return p;
	}

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.boot.launch.test";
	}

	protected ILaunchConfigurationWorkingCopy createWorkingCopy() throws CoreException {
		String name = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName("test");
		ILaunchConfigurationWorkingCopy wc = DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(BootLaunchConfigurationDelegate.LAUNCH_CONFIG_TYPE_ID)
			.newInstance(null, name);
		return wc;
	}

	public IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public void assertError(String snippet, LiveExpression<ValidationResult> validator) {
		ValidationResult value = validator.getValue();
		assertEquals(IStatus.ERROR, value.status);
		assertContains(snippet, value.msg);
	}

	public void assertContains(String needle, String haystack) {
		if (haystack==null || !haystack.contains(needle)) {
			fail("Not found: "+needle+"\n in \n"+haystack);
		}
	}

	public void assertOk(LiveExpression<ValidationResult> validator) {
		ValidationResult status = validator.getValue();
		if (!status.isOk()) {
			fail(status.toString());
		}
	}

}
