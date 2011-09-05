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

import java.util.Arrays;
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

/**
 * Test case for {@link BeanInitDestroyMethodRule} validation rule
 * @author Christian Dupuis
 * @since 2.0.4
 */
public class BeanInitDestroyMethodRuleTest extends BeansCoreTestCase {
	
	private IResource resource = null;
	private IBeansConfig beansConfig = null;
	
	@Override
	protected void setUp() throws Exception {
		Thread.sleep(5000);
		resource = createPredefinedProjectAndGetResource("validation", "src/ide-771.xml");
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
	}
	
	public void testCorrectInitAndDestroyMethodUsage() throws Exception {
		IBean bean = BeansModelUtils.getBean("correct", beansConfig);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue("No error expected for bean", severity != IMarker.SEVERITY_ERROR
				&& severity != IMarker.SEVERITY_WARNING);
		
	}

	public void testNotExistingInitAndDestroyMethods() throws Exception {
		String[] errorMessages = new String[] {
				"Destroy-method 'notExisting' not found in bean class 'org.springframework.Base'",
				"Init-method 'notExisting' not found in bean class 'org.springframework.Base'",
				"Static factory method 'notExisting' with 0 arguments not found in factory bean class 'org.springframework.Base'" };

		IBean bean = BeansModelUtils.getBean("incorrect", beansConfig);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue(severity == IMarker.SEVERITY_ERROR);

		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue(markers.size() == 3);
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Error message not expected '" + msg + "'", Arrays.asList(errorMessages)
					.contains(msg));
		}
	}

	public void testExistingInitAndDestroyMethodsWithWrongParameterCount() throws Exception {
		String[] errorMessages = new String[] {
				"Destroy-method 'initWithParameters' not found in bean class 'org.springframework.Base'",
				"Init-method 'initWithParameters' not found in bean class 'org.springframework.Base'" };

		IBean bean = BeansModelUtils.getBean("incorrectWithParameters", beansConfig);
		int severity = MarkerUtils.getHighestSeverityFromMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue(severity == IMarker.SEVERITY_ERROR);

		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean
				.getElementStartLine(), bean.getElementEndLine());
		assertTrue(markers.size() == 2);
		for (IMarker marker : markers) {
			String msg = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Error message not expected '" + msg + "'", Arrays.asList(errorMessages)
					.contains(msg));
		}
	}
	
}
