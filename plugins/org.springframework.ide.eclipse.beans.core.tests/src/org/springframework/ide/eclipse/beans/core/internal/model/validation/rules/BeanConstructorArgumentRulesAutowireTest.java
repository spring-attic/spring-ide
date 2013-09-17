/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.MarkerUtils;

/**
 * @author Leo Dos Santos
 */
public class BeanConstructorArgumentRulesAutowireTest extends BeansCoreTestCase {
	
	private IResource resource = null;
	
	private IBeansConfig beansConfig = null;
	
	@Before
	public void setUp() throws Exception {
//		Thread.sleep(5000);
		resource = createPredefinedProjectAndGetResource("validation", "src/sts-3261.xml");
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
	}
	
	@Test
	public void testAutowireEmpty() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 0 arguments defined in class 'org.springframework.Factory'" };
		IBean bean = BeansModelUtils.getBean("autowireEmpty", beansConfig);
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
	public void testAutowireDefault() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 0 arguments defined in class 'org.springframework.Factory'" };
		IBean bean = BeansModelUtils.getBean("autowireDefault", beansConfig);
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
	public void testAutowireConstructor() throws Exception {
		IBean bean = BeansModelUtils.getBean("autowireConstructor", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
	}

	@Test
	public void testAutowireEmptyParentDefault() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 0 arguments defined in class 'org.springframework.Factory'" };
		IBean bean = BeansModelUtils.getBean("autowireEmptyParentDefault", beansConfig);
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
	public void testAutowireDefaultParentDefault() throws Exception {
		String[] errorMessages = new String[] { "No constructor with 0 arguments defined in class 'org.springframework.Factory'" };
		IBean bean = BeansModelUtils.getBean("autowireDefaultParentDefault", beansConfig);
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
	public void testAutowireConstructorParentDefault() throws Exception {
		IBean bean = BeansModelUtils.getBean("autowireConstructorParentDefault", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
	}

	@Test
	public void testAutowireEmptyParentConstructor() throws Exception {
		IBean bean = BeansModelUtils.getBean("autowireEmptyParentConstructor", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
	}
	
	@Test
	public void testAutowireDefaultParentConstructor() throws Exception {
		IBean bean = BeansModelUtils.getBean("autowireDefaultParentConstructr", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
	}
	
	@Test
	public void testAutowireConstructorParentConstructor() throws Exception {
		IBean bean = BeansModelUtils.getBean("autowireConstructorParentConstructor", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
	}
	
}
