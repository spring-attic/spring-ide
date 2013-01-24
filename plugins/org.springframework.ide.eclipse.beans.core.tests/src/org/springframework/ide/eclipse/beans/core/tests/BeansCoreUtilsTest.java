/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;

/**
 * @author Martin Lippert
 */
public class BeansCoreUtilsTest extends BeansCoreTestCase {

	private IProject project1;
	private IProject project2;

	@Before
	public void setUp() throws Exception {
		project2 = createPredefinedProject("isconfigtests2");
		project1 = createPredefinedProject("isconfigtests");
	}

	@Test
	public void testSimpleBeansConfigLookup() {
		IResource configResource1 = project1.findMember("src/import-one.xml");
		IResource configResource2 = project1.findMember("src/test1/import-two.xml");
		IResource configResource3 = project1.findMember("src2/test1/not-configured-config.xml");
		IResource configResource4 = project2.findMember("src/test1/anotherconfig.xml");
		
		IResource javaResource1 = project1.findMember("src/test1/TestClass.java");
		IResource javaResource2 = project2.findMember("src/test1/TestAspect.java");

		assertNotNull(configResource1);
		assertNotNull(configResource2);
		assertNotNull(configResource3);
		assertNotNull(configResource4);
		assertTrue(configResource1.exists());
		assertTrue(configResource2.exists());
		assertTrue(configResource3.exists());
		assertTrue(configResource4.exists());

		assertTrue(BeansCoreUtils.isBeansConfig(configResource1, true));
		assertTrue(BeansCoreUtils.isBeansConfig(configResource2, true));
		assertFalse(BeansCoreUtils.isBeansConfig(configResource3, true));
		assertTrue(BeansCoreUtils.isBeansConfig(configResource4, true));
		
		assertNotNull(javaResource1);
		assertNotNull(javaResource1);
		assertTrue(javaResource1.exists());
		assertTrue(javaResource2.exists());
		assertFalse(BeansCoreUtils.isBeansConfig(javaResource1, true));
		assertFalse(BeansCoreUtils.isBeansConfig(javaResource2, true));
	}

}
