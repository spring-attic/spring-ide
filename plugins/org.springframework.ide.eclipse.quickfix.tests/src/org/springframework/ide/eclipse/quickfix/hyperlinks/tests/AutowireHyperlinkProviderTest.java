/*******************************************************************************
 * Copyright (c) 2005, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.hyperlinks.tests;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.quickfix.hyperlinks.AutowireBeanHyperlink;
import org.springframework.ide.eclipse.quickfix.hyperlinks.AutowireHyperlinkDetector;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class AutowireHyperlinkProviderTest extends TestCase {

	private IProject project;

	private IBeansModel model;

	private IBeansProject beansProject;

	protected void setUp() throws Exception {
		project = StsTestUtil.createPredefinedProject("autowire", "org.springframework.ide.eclipse.quickfix.tests");
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}

	protected void tearDown() throws Exception {
		try {
			project.delete(true, null);
		}
		catch (Exception e) {
			StsTestUtil.cleanUpProjects();
		}
	}

	// @Test
	// public void testOneBeanAutowireHyperlink() throws Exception {
	// BeansConfig config = new BeansConfig(beansProject,
	// "src/org/springframework/beans/factory/annotation/1-bean-context.xml",
	// IBeansConfig.Type.MANUAL);
	//
	// AutowireHyperlinkDetector detector = new AutowireHyperlinkDetector();
	// Set<AutowireBeanHyperlink> hyperlinks = new
	// HashSet<AutowireBeanHyperlink>();
	// detector.addHyperlinksHelper(config, "test.beans.TestBean", project,
	// hyperlinks);
	// assertTrue(hyperlinks.size() == 1);
	// }

	@Test
	public void testMultipleBeansAutowireHyperlink() throws Exception {
		BeansConfig config = new BeansConfig(beansProject,
				"src/org/springframework/beans/factory/annotation/2-beans-context.xml", IBeansConfig.Type.MANUAL);

		AutowireHyperlinkDetector detector = new AutowireHyperlinkDetector();
		Set<AutowireBeanHyperlink> hyperlinks = new HashSet<AutowireBeanHyperlink>();
		detector.addHyperlinksHelper(config, "test.beans.TestBean", project, hyperlinks);
		assertTrue(hyperlinks.size() == 2);
	}

	@Test
	public void testPrimitiveType() throws Exception {
		BeansConfig config = new BeansConfig(beansProject,
				"src/org/springframework/beans/factory/annotation/1-bean-context.xml", IBeansConfig.Type.MANUAL);

		AutowireHyperlinkDetector detector = new AutowireHyperlinkDetector();
		Set<AutowireBeanHyperlink> hyperlinks = new HashSet<AutowireBeanHyperlink>();
		detector.addHyperlinksHelper(config, "int", project, hyperlinks);
		assertTrue(hyperlinks.size() == 0);
	}

}
