/*******************************************************************************
 * Copyright (c) 2005, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Test case for {@link BeanConstructorArgumentRule} validation rule
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanConstructorArgumentRuleTest extends BeansCoreTestCase {

	private IResource resource = null;

	private IBeansConfig beansConfig = null;

	@BeforeClass
	public static void setUpAll() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Set non-locking class-loader for windows testing
			 */
			InstanceScope.INSTANCE.getNode(SpringCore.PLUGIN_ID).putBoolean(
					SpringCore.USE_NON_LOCKING_CLASSLOADER, true);
		}
	}

	@Before
	public void setUp() throws Exception {
		Thread.sleep(5000);
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-855.xml");
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
	}

	@Test
	public void testCorrectArgumentCount() throws Exception {
		IBean bean = BeansModelUtils.getBean("correct", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);

	}

	@Test
	public void testCorrectArgumentCountOnChildBean() throws Exception {
		IBean bean = BeansModelUtils.getBean("correctChild", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);

	}

	@Test
	public void testNoArgumentValues() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 0 arguments defined in class 'org.springframework.Factory'" };

		IBean bean = BeansModelUtils.getBean("incorrect", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals(IMarker.SEVERITY_ERROR, severity);

		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals(1, markers.size());
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Error message not expected '" + msg + "'", Arrays.asList(errorMessages)
					.contains(msg));
		}
	}

	@Test
	public void testTooFewArgumentValues() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 3 arguments defined in class 'org.springframework.Factory'" };
		
		IBean bean = BeansModelUtils.getBean("incorrectWithArgs", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals(IMarker.SEVERITY_ERROR, severity);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals(1, markers.size());
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Error message not expected '" + msg + "'", Arrays.asList(errorMessages)
					.contains(msg));
		}
	}

}
