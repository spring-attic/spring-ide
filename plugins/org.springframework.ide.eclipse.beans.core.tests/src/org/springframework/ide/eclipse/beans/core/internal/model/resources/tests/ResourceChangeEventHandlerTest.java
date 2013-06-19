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
package org.springframework.ide.eclipse.beans.core.internal.model.resources.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel.ResourceChangeEventHandler;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.core.model.generators.JavaConfigGenerator;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class ResourceChangeEventHandlerTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;
	private List<ModelChangeEvent> changedEvents;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		changedEvents = new ArrayList<ModelChangeEvent>();
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		model.addProject(beansProject);
		model.addChangeListener(new IModelChangeListener() {
			public void elementChanged(ModelChangeEvent event) {
				changedEvents.add(event);
			}
		});
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}
	
    private BeansConfigId getConfigIdForClassName(String cName) throws JavaModelException {
        return BeansConfigFactory.getConfigId(javaProject.findType(cName), project);
    }

	@Test
	public void testSimpleJavaConfigAddedHandler() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		beansProject.addConfig(getConfigIdForClassName("org.test.spring.SimpleConfigurationClass"), IBeansConfig.Type.MANUAL);
		IBeansConfig config = beansProject.getConfig(getConfigIdForClassName("org.test.spring.SimpleConfigurationClass"));
		
		ResourceChangeEventHandler handler = this.model.new ResourceChangeEventHandler();
		IResource resource = configClass.getResource();
		handler.configAdded((IFile)resource, IResourceChangeEvent.POST_BUILD, IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		assertEquals(config, configs.iterator().next());

		assertEquals(1, changedEvents.size());
		assertEquals(config, changedEvents.get(0).getElement());
	}

	@Test
	public void testInnerClassJavaConfigAddedHandler() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.OuterConfigurationClass$InnerConfigurationClass");
		beansProject.addConfig(getConfigIdForClassName("org.test.spring.OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
		IBeansConfig config = beansProject.getConfig(getConfigIdForClassName("org.test.spring.OuterConfigurationClass$InnerConfigurationClass"));
		
		ResourceChangeEventHandler handler = this.model.new ResourceChangeEventHandler();
		IResource resource = configClass.getResource();
		handler.configAdded((IFile)resource, IResourceChangeEvent.POST_BUILD, IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		assertEquals(config, configs.iterator().next());

		assertEquals(1, changedEvents.size());
		assertEquals(config, changedEvents.get(0).getElement());
	}

}
