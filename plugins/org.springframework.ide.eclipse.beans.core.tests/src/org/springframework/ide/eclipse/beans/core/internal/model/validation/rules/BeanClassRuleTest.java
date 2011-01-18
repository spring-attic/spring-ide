/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;

/**
 * Test case to test the {@link BeanClassRule}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanClassRuleTest extends BeansCoreTestCase {

	private IResource resource;

	@Override
	protected void setUp() throws Exception {
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-832.xml");
		Thread.sleep(5000);
	}

	public void testSpecialTreatmentForOsgiClasses() throws Exception {
		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID + ".problemmarker",
				false, IResource.DEPTH_ZERO);
		assertTrue("No error messages expected as OSGi class names should get a special treatment",
				markers.length == 0);
	}

}
