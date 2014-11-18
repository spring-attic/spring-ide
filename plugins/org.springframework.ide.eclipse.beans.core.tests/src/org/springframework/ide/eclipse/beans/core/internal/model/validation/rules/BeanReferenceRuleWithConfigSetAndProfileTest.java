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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link BeanReferenceRule}.
 * @author Martin Lippert
 * @since 3.6.3
 */
public class BeanReferenceRuleWithConfigSetAndProfileTest extends BeansCoreTestCase {

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
		resource = createPredefinedProjectAndGetResource("validation-beanreference", "src/simple-bean-ref.xml");
		StsTestUtil.waitForResource(resource);
		
		IBeansModel model = BeansCorePlugin.getModel();

		beansConfig = model.getConfig((IFile) resource);
		BeansProject project = (BeansProject) model.getProject("validation-beanreference");
		
		BeansConfigSet configSet = new BeansConfigSet(project, "testset", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("src/simple-bean-ref.xml");
		configSet.addProfile("testprofile");
		project.addConfigSet(configSet);
		
		resource.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	@Test
	public void testSimpleBeanReferenceFound() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceOk", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testSimpleBeanReferenceNotFound() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceMissing", beansConfig);
		assertNotNull(bean);

		IMarker[] markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine()).toArray(new IMarker[0]);
		assertEquals(1, markers.length);
		String msg = (String) markers[0].getAttribute(IMarker.MESSAGE);
		assertEquals("Referenced bean 'missingfoo' not found [config set: validation-beanreference/testset]", msg);
	}
	
	@Test
	public void testBeanReferenceFoundToBeanFromComposite() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceToInnerLevelOk", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testBeanReferenceFoundToBeanFromProfileComposite() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceToInnerLevelWithProfile", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testBeanReferenceFoundToOuterBean() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceToUpperLevelOk", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}

	@Test
	public void testBeanReferenceFoundWithinSameComposite() throws Exception {
		IBean bean = BeansModelUtils.getBean("referenceToSameLevelOk", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}
	
	@Test
	public void testBeanReferenceFromProfileCompositeToGeneralUpperLevel() throws Exception {
		IBean bean = BeansModelUtils.getBean("profileReferenceToUpperLevelFromProfileBeanOk", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}
	
	@Test
	public void testBeanReferenceFromProfileToOtherNonProfileCompositeBean() throws Exception {
		IBean bean = BeansModelUtils.getBean("profileReferenceToNonProfileEmbeddedBean", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}
	
	@Test
	public void testBeanReferenceFromProfileToMissingBean() throws Exception {
		IBean bean = BeansModelUtils.getBean("profileEmbdeddedReferenceMissing", beansConfig);
		assertNotNull(bean);
		
		IMarker[] markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine()).toArray(new IMarker[0]);
		assertEquals(1, markers.length);
		String msg = (String) markers[0].getAttribute(IMarker.MESSAGE);
		assertEquals("Referenced bean 'missingfoo' not found [config set: validation-beanreference/testset]", msg);
	}
	
	@Test
	public void testBeanReferenceFromProfileToBeanFromSameComposite() throws Exception {
		IBean bean = BeansModelUtils.getBean("profileEmbdeddedReferenceFromSameComposite", beansConfig);
		assertNotNull(bean);
		
		Set<IMarker> markers = MarkerUtils.getAllMarkersInRange(resource, bean.getElementStartLine(), bean.getElementEndLine());
		assertEquals(0, markers.size());
	}
	
}
