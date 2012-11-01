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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link RequiredPropertyRule}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class RequiredPropertyRuleTest extends BeansCoreTestCase {

	private IResource resource;

	private IBeansConfig beansConfig;

	@Override
	protected void setUp() throws Exception {
		resource = createPredefinedProjectAndGetResource("required", "src/ide-825.xml");
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		StsTestUtil.waitForResource(resource);
	}

	public void testRequiredAnnotationConfiguration() throws Exception {
		IBean bean = BeansModelUtils.getBean("abstractFoo", beansConfig);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("Abstract beans are not required to be configured", severity == -1);

		bean = BeansModelUtils.getBean("goodFoo", beansConfig);
		severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("Satisfying configuration given; no error expected", severity == -1);

		bean = BeansModelUtils.getBean("wrongFoo", beansConfig);
		severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("Missing required configuration; error expected",
				severity == IMarker.SEVERITY_WARNING);
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue(markers.size() == 1);
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Error message not expected '" + msg + "'",
					"Property 'bar' is required for bean 'wrongFoo'".equals(msg));
		}

	}

}
