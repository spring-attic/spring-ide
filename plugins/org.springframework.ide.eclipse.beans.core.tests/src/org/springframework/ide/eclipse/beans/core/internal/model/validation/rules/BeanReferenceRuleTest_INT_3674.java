/*******************************************************************************
 * Copyright (c) 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import static org.junit.Assert.*;

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
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link BeanReferenceRule}.
 * @author Martin Lippert
 * @since 3.6.3
 */
public class BeanReferenceRuleTest_INT_3674 extends BeansCoreTestCase {

	private static final String PROJECT_NAME = "validation-beanreference-INT-3674";
	private IResource resource;
	private IBeansConfig beansConfig;

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
		resource = createPredefinedProjectAndGetResource(PROJECT_NAME, "src/main/resources/integration-config.xml");
		StsTestUtil.waitForResource(resource);
		beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
	}

	@Test
	public void test_INT_3674() throws Exception {
		IBean bean = BeansModelUtils.getBean("errorChannel", beansConfig);
		assertNotNull(bean);

		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertNoMarkers(markers);
	}

}
