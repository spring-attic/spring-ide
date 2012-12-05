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
import static org.junit.Assert.assertNotNull;

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
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link RequiredPropertyRule}.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @since 2.0.5
 */
public class RequiredPropertyRuleTest extends BeansCoreTestCase {

	private IResource resource;

	private IBeansConfig beansConfig;

	@Before
	public void setUp() throws Exception {
		resource = createPredefinedProjectAndGetResource("required", "src/ide-825.xml");
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
		StsTestUtil.waitForResource(resource);
	}

	@Test
	public void testRequiredAnnotationConfiguration() throws Exception {
		IBean bean = BeansModelUtils.getBean("abstractFoo", beansConfig);
		assertNotNull(bean);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals("Abstract beans are not required to be configured", -1, severity);

		bean = BeansModelUtils.getBean("goodFoo", beansConfig);
		severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals("Satisfying configuration given; no error expected", -1, severity);

		bean = BeansModelUtils.getBean("wrongFoo", beansConfig);
		severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals("Missing required configuration; error expected", IMarker.SEVERITY_WARNING,
				severity);
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertEquals(1, markers.size());
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Error message not expected '" + msg + "'",
					"Property 'bar' is required for bean 'wrongFoo'", msg);
		}
	}

}
