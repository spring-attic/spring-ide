/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.test;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.project.SpringProjectNature;

/**
 * Test addition and removal of Spring Beans nature
 * @author Loren Rosen
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */	
public class NatureTests extends AbstractSpringIdeTest {

	public NatureTests(String name) {
		super(name);
	}
	
	/** 
	 * test the addition and removal of Spring Beans nature
	 * currently just check:
	 *    -- that after adding the nature, eclipse internal
	 *       data structures say it has the nature
	 *    -- that after adding the nature, the project
	 *       has the Spring Beans validator associated with it
	 *    -- that after removing the nature, eclipse internal
	 *       data structures say it no longer has the nature
	 * we don't check that after the removal any Spring Beans
	 * validation messages are removed, though we probably should
	 * @throws CoreException
	 */
	public void testNatureAddAndRemove() throws CoreException {

		IProject eclipseProject = project.getProject();
		assertNotNull(eclipseProject);
		
		IBeansModel model = BeansCorePlugin.getModel();
		assertNotNull(model);
		
		assertFalse(hasBeansProjectNature());
		assertNull(model.getProject(eclipseProject));
		assertFalse(hasBeansBuilder());

		SpringCoreUtils.addProjectNature(eclipseProject, SpringCore.NATURE_ID, new NullProgressMonitor());
		project.waitForAutoBuild();
		
		assertNotNull (eclipseProject.getNature(SpringCore.NATURE_ID));
		assertTrue ( eclipseProject.getNature(SpringCore.NATURE_ID) instanceof SpringProjectNature);

		assertTrue(hasBeansProjectNature());
		assertNotNull(model.getProject(eclipseProject));
		assertTrue(hasBeansBuilder());
		
		BeansProject proj = (BeansProject) model.getProject(eclipseProject);
		assertNotNull(proj);
		
		assertTrue(hasBeansDecorator());

		SpringCoreUtils.removeProjectNature(eclipseProject, SpringCore.NATURE_ID, new NullProgressMonitor());
		project.waitForAutoBuild();
			
		assertFalse(hasBeansProjectNature());
		assertNull(model.getProject(eclipseProject));
		assertFalse(hasBeansBuilder());

	}

	/**
	 * 
	 * @return true if project has Spring Beans nature, false otherwise
	 * @throws CoreException
	 */
	private boolean hasBeansProjectNature() throws CoreException {
		return SpringCoreUtils.isSpringProject(project.getProject());
		// return project.getProject().hasNature(BeansProjectNature.NATURE_ID);
	}
	
	/**
	 * 
	 * @return true if project has the Spring Beans validator
	 * associated with it, false otherwise
	 * @throws CoreException
	 */
	private boolean hasBeansBuilder() throws CoreException {
		ICommand[] commands = project.getProject().getDescription()
				.getBuildSpec();

		boolean found = false;
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(SpringCore.BUILDER_ID))
				found = true;
		}

		return found;
	}
	

	/**
	 * 
	 * @return true if the eclipse workbench knows about the
	 * Spring Beans label decorator, false otherwise
	 */
	private boolean hasBeansDecorator() {
		DecoratorManager decoratorManager = WorkbenchPlugin.getDefault().getDecoratorManager();
        IBaseLabelProvider b = decoratorManager.getBaseLabelProvider("org.springframework.ide.eclipse.beans.ui.model.beansModelLabelDecorator");
        return b != null;
	}
}
