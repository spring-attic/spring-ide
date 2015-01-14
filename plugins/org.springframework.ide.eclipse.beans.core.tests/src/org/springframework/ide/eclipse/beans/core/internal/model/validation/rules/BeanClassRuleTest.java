/*******************************************************************************
 * Copyright (c) 2005, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
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
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link BeanClassRule}.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @autohr Martin Lippert
 * @since 2.0.5
 */
public class BeanClassRuleTest extends BeansCoreTestCase {

	private IProject project;

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
		project = createPredefinedProject("validation");
	}

	@Test
	public void testSimpleBeanClass() throws Exception {
		IResource resource = project.findMember("src/bean-class-rule-tests.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		IBean bean = BeansModelUtils.getBean("fine", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testInterfaceBeanClass() throws Exception {
		IResource resource = project.findMember("src/bean-class-rule-tests.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		IBean bean = BeansModelUtils.getBean("interfaceNotAllowed", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(1, markers.size());
		
		IMarker marker = markers.iterator().next();
		assertEquals(IMarker.SEVERITY_WARNING, marker.getAttribute(IMarker.SEVERITY, -1));
		assertEquals("Class 'org.springframework.FooInterface' is an interface", marker.getAttribute(IMarker.MESSAGE));
	}

	@Test
	public void testAbstractMarkedInterfaceBeanClass() throws Exception {
		IResource resource = project.findMember("src/bean-class-rule-tests.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		IBean bean = BeansModelUtils.getBean("abstractMarkedInterface", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testAbstractBeanClass() throws Exception {
		IResource resource = project.findMember("src/bean-class-rule-tests.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		IBean bean = BeansModelUtils.getBean("abstractClassNotAllowed", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(1, markers.size());
		
		IMarker marker = markers.iterator().next();
		assertEquals(IMarker.SEVERITY_WARNING, marker.getAttribute(IMarker.SEVERITY, -1));
		assertEquals("Class 'org.springframework.AbstractClass' is abstract", marker.getAttribute(IMarker.MESSAGE));
	}

	@Test
	public void testAbstractFactoryBeanClass() throws Exception {
		IResource resource = project.findMember("src/bean-class-rule-tests.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		IBean bean = BeansModelUtils.getBean("abstractFactoryClass", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testSpecialTreatmentForOsgiClasses() throws Exception {
		IResource resource = project.findMember("src/ide-832.xml");
		StsTestUtil.waitForResource(resource);

		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID + ".problemmarker",
				false, IResource.DEPTH_ZERO);
		assertEquals("No error messages expected as OSGi class names should get a special treatment", 0,
				markers.length);
	}

}
