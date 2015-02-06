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
package org.springframework.ide.eclipse.boot.launch.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.PropVal;
import org.springframework.ide.eclipse.boot.launch.livebean.LiveBeanSupport;
import org.springframework.ide.eclipse.boot.launch.test.util.LaunchUtil;
import org.springframework.ide.eclipse.boot.launch.test.util.LaunchUtil.LaunchResult;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class BootLaunchConfigurationDelegateTest extends BootLaunchTestCase {

	//TODO:
	//  - Test that launching  launch conf with the various options set has the desired effect
	//     on the launched process.

	private static final String TEST_MAIN_CLASS = "demo.DumpInfoApplication";
	private static final String TEST_PROJECT = "dump-info";

	@SuppressWarnings("restriction")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		//The following disables some nasty popups that can cause test to hang rather than fail.
		//when project has errors upon launching it.
		InstanceScope.INSTANCE.getNode(DebugPlugin.getUniqueIdentifier())
			.putBoolean(IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, false);
	}

	public void testGetSetProperties() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		assertProperties(BootLaunchConfigurationDelegate.getProperties(wc)
				/*empty*/
		);

		BootLaunchConfigurationDelegate.setProperties(wc, null); //accepts null in lieu of empty list,
		assertProperties(BootLaunchConfigurationDelegate.getProperties(wc)
				/*empty*/
		);

		//store one single property
		doGetAndSetProps(wc,
				pv("foo", "Hello", true)
		);

		//store empty property list
		doGetAndSetProps(wc
				/*empty*/
		);

		//store a few properties
		doGetAndSetProps(wc,
				pv("foo.bar", "snuffer.nazz", true),
				pv("neala", "nolo", false),
				pv("Hohoh", "Santa Claus", false)
		);

		//store properties with identical keys
		doGetAndSetProps(wc,
				pv("foo", "snuffer.nazz", true),
				pv("foo", "nolo", false),
				pv("bar", "Santa Claus", false),
				pv("bar", "Santkkk ", false)
		);
	}

	private void doGetAndSetProps(ILaunchConfigurationWorkingCopy wc, PropVal... props) {
		BootLaunchConfigurationDelegate.setProperties(wc, Arrays.asList(props));
		List<PropVal> retrieved = BootLaunchConfigurationDelegate.getProperties(wc);
		assertProperties(retrieved, props);
	}

	private PropVal pv(String name, String value, boolean isChecked) {
		return new PropVal(name, value, isChecked);
	}

	public void testSetGetProject() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		assertEquals(null, BootLaunchConfigurationDelegate.getProject(wc));
		IProject project = getProject("foo");
		BootLaunchConfigurationDelegate.setProject(wc, project);
		assertEquals(project, BootLaunchConfigurationDelegate.getProject(wc));
	}

	public void testSetGetProfile() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		assertEquals("", BootLaunchConfigurationDelegate.getProfile(wc));

		BootLaunchConfigurationDelegate.setProfile(wc, "deployment");
		assertEquals("deployment", BootLaunchConfigurationDelegate.getProfile(wc));

		BootLaunchConfigurationDelegate.setProfile(wc, null);
		assertEquals("", BootLaunchConfigurationDelegate.getProfile(wc));
	}

	public void testClearProperties() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		BootLaunchConfigurationDelegate.setProperties(wc, Arrays.asList(
				pv("some", "thing", true),
				pv("some.other", "thing", false)
		));
		assertFalse(BootLaunchConfigurationDelegate.getProperties(wc).isEmpty());
		BootLaunchConfigurationDelegate.clearProperties(wc);
		assertTrue(BootLaunchConfigurationDelegate.getProperties(wc).isEmpty());
	}

	public void testGetSetDebug() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		boolean deflt = BootLaunchConfigurationDelegate.DEFAULT_ENABLE_DEBUG_OUTPUT;
		boolean other = !deflt;
		assertEquals(deflt,
				BootLaunchConfigurationDelegate.getEnableDebugOutput(wc));

		BootLaunchConfigurationDelegate.setEnableDebugOutput(wc, other);
		assertEquals(other, BootLaunchConfigurationDelegate.getEnableDebugOutput(wc));

		BootLaunchConfigurationDelegate.setEnableDebugOutput(wc, deflt);
		assertEquals(deflt, BootLaunchConfigurationDelegate.getEnableDebugOutput(wc));
	}

	public void testGetSetLiveBean() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		boolean deflt = BootLaunchConfigurationDelegate.DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
		boolean other = !deflt;

		assertEquals(deflt, BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(wc));

		BootLaunchConfigurationDelegate.setEnableLiveBeanSupport(wc, other);
		assertEquals(other, BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(wc));

		BootLaunchConfigurationDelegate.setEnableLiveBeanSupport(wc, deflt);
		assertEquals(deflt, BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(wc));
	}

	public void testGetSetJMXPort() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		assertEquals("", BootLaunchConfigurationDelegate.getJMXPort(wc));

		BootLaunchConfigurationDelegate.setJMXPort(wc, "something");
		assertEquals("something", BootLaunchConfigurationDelegate.getJMXPort(wc));
	}

	public void testRunAsLaunch() throws Exception {
		IProject project = createLaunchReadyProject(TEST_PROJECT);

		//Creates a launch conf similar to that created by 'Run As' menu.
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		BootLaunchConfigurationDelegate.setDefaults(wc, project, TEST_MAIN_CLASS);

		LaunchResult result = LaunchUtil.synchLaunch(wc);
		System.out.println(result); //Great help in debugging this :-)
		assertContains(":: Spring Boot ::", result.out);
		assertOk(result);
	}

	public void testLaunchAllOptsDisable() throws Exception {
		createLaunchReadyProject(TEST_PROJECT);
		ILaunchConfigurationWorkingCopy wc = createBaseWorkingCopy();
		LaunchResult result = LaunchUtil.synchLaunch(wc);
		assertContains(":: Spring Boot ::", result.out);
		assertOk(result);
	}

	public void testLaunchWithDebugOutput() throws Exception {
		createLaunchReadyProject(TEST_PROJECT);
		ILaunchConfigurationWorkingCopy wc = createBaseWorkingCopy();

		BootLaunchConfigurationDelegate.setEnableDebugOutput(wc, true);

		LaunchResult result = LaunchUtil.synchLaunch(wc);

		assertContains(":: Spring Boot ::", result.out);
		assertContains("AUTO-CONFIGURATION REPORT", result.out);
		assertOk(result);
	}

	public void testLaunchWithLiveBeans() throws Exception {
		createLaunchReadyProject(TEST_PROJECT);
		ILaunchConfigurationWorkingCopy wc = createBaseWorkingCopy();

		BootLaunchConfigurationDelegate.setEnableLiveBeanSupport(wc, true);
		int port = LiveBeanSupport.randomPort();
		BootLaunchConfigurationDelegate.setJMXPort(wc, ""+port);

		LaunchResult result = LaunchUtil.synchLaunch(wc);

		System.out.println(result);

		assertContains(":: Spring Boot ::", result.out);
		//The following check doesn't real prove the live bean graph works, but at least it shows the VM args are
		//taking effect.
		assertContains("com.sun.management.jmxremote.port='"+port+"'", result.out);
		assertOk(result);
	}

	public void testLaunchWithProperties() throws Exception {
		createLaunchReadyProject(TEST_PROJECT);
		ILaunchConfigurationWorkingCopy wc = createBaseWorkingCopy();

		BootLaunchConfigurationDelegate.setProperties(wc, Arrays.asList(
				pv("foo", "foo is enabled", true),
				pv("bar", "bar is not enabled", false),
				pv("zor", "zor enabled", true),
				pv("zor", "zor disabled", false)
		));

		LaunchResult result = LaunchUtil.synchLaunch(wc);

		assertContains(":: Spring Boot ::", result.out);
		assertPropertyDump(result.out,
				"debug=null\n" +
				"zor='zor enabled'\n" +
				"foo='foo is enabled'\n" +
				"bar=null\n" +
				"com.sun.management.jmxremote.port=null"
		);
		assertOk(result);
	}

	public void testLaunchWithProfile() throws Exception {
		createLaunchReadyProject(TEST_PROJECT);
		ILaunchConfigurationWorkingCopy wc = createBaseWorkingCopy();

		BootLaunchConfigurationDelegate.setProfile(wc, "special");

		LaunchResult result = LaunchUtil.synchLaunch(wc);

		assertContains(":: Spring Boot ::", result.out);
		assertContains("foo='special foo'", result.out);
		assertOk(result);
	}

	private void assertPropertyDump(String out, String expected) {
		String BEG = ">>>properties";
		String END = "<<<properties";
		int beg = out.indexOf(BEG)+BEG.length();
		int end = out.indexOf(END);
		String found = out.substring(beg, end);
		assertEquals(expected.trim(), found.trim());
	}

	private ILaunchConfigurationWorkingCopy createBaseWorkingCopy() throws Exception {
		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();

		BootLaunchConfigurationDelegate.setProject(wc, getProject(TEST_PROJECT));
		BootLaunchConfigurationDelegate.setMainType(wc, TEST_MAIN_CLASS);
		//Explictly set all options in the config to 'disabled' irrespective of
		// their default values (tests will be more robust w.r.t changing of the defaults).
		BootLaunchConfigurationDelegate.setEnableDebugOutput(wc, false);
		BootLaunchConfigurationDelegate.setEnableLiveBeanSupport(wc, false);
		BootLaunchConfigurationDelegate.setJMXPort(wc, "");
		BootLaunchConfigurationDelegate.setProfile(wc, "");
		BootLaunchConfigurationDelegate.setProperties(wc, null);
		return wc;
	}

	///////////////////////////////////////////////////////////////////////////////

	public static void assertProperties(List<PropVal> actual, PropVal... expect) {
		assertElements(actual, expect);
	}



}
