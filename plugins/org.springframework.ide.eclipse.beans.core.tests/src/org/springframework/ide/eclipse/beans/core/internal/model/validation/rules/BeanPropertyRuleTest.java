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
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;

/**
 * Test case to test the {@link BeanPropertyRule}.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class BeanPropertyRuleTest extends BeansCoreTestCase {
	
	private IResource resource;
	
	@Override
	protected void setUp() throws Exception {
		resource = null;
		Thread.sleep(5000);
	}
	
	public void testInvalidPropertyWithAbstractBean() throws Exception {
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-798.xml");
		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID
				+ ".problemmarker", false, IResource.DEPTH_ZERO);

		for (IMarker marker : markers) {
			assertTrue(
					"Found property setter error on bean 'middle' but didn't expect that",
					!(marker.getAttribute(IValidationProblemMarker.ERROR_ID)
							.equals("NO_SETTER") && ((String) marker
							.getAttribute(IMarker.MESSAGE))
							.contains("org.springframework.Base")));
		}
	}

	public void testInvalidPropertyWithBean() throws Exception {
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-798.xml");
		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID
				+ ".problemmarker", false, IResource.DEPTH_ZERO);

		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue(
					"Found property setter error on bean 'childBean3' but didn't expect that",
					!(marker.getAttribute(IValidationProblemMarker.ERROR_ID)
							.equals("NO_SETTER") && msg.contains("childBean3") && msg.contains("bar")));
		}
	}

	public void testIntertypeDeclaredProperty() throws Exception {
		resource = createPredefinedProjectAndGetResource("aspectj", "src/ide-743.xml");
		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID
				+ ".problemmarker", false, IResource.DEPTH_ZERO);
		assertTrue(markers.length == 0);
	}
	
}
