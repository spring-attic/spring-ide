/*******************************************************************************
 * Copyright (c) 2005, 2012 Spring IDE Developers
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link BeanClassRule}.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @since 2.0.5
 */
public class BeanClassRuleTest extends BeansCoreTestCase {

	private IResource resource;

	@Before
	public void setUp() throws Exception {
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-832.xml");
		StsTestUtil.waitForResource(resource);
	}

	@Test
	public void testSpecialTreatmentForOsgiClasses() throws Exception {
		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID + ".problemmarker",
				false, IResource.DEPTH_ZERO);
		assertEquals("No error messages expected as OSGi class names should get a special treatment", 0,
				markers.length);
	}

}
