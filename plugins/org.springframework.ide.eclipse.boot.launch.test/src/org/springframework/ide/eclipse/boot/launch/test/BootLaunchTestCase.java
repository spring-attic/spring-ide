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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.test.util.LaunchUtil.LaunchResult;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

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

	public static void assertOk(LaunchResult result) {
		assertEquals(0, result.terminationCode);
	}

	public static <T> void assertElements(T[] actual, T... expect) {
		assertElements(Arrays.asList(actual), expect);
	}

	public static <T> void assertElements(Collection<T> actual, T... expect) {
		Set<T> expectedSet = new HashSet<T>(Arrays.asList(expect));

		for (T propVal : actual) {
			if (!expectedSet.remove(propVal)) {
				fail("Unexpected element: "+propVal);
			}
		}

		if (!expectedSet.isEmpty()) {
			StringBuilder missing = new StringBuilder();
			for (T propVal : expectedSet) {
				missing.append(propVal+"\n");
			}
			fail("Missing elements: \n"+missing);
		}
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

	/**
	 * Tests that want to launch something from a project should use this rather to
	 * make sure project is built and has no errors.
	 * <p>
	 * Projects with errors shouldn't be launched as they will just cause launcher tests to
	 * fail in confusing and unpredictabled ways.
	 */
	public IProject createLaunchReadyProject(String projectName) throws Exception {
		IProject project = createPredefinedProject(projectName);
		StsTestUtil.assertNoErrors(project);
		return project;
	}

}
