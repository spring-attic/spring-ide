/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUILabelDecorator;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

public class MigrateSpringBeansProject implements IObjectActionDelegate {

	private static final String BEANS_CORE_PLUGIN_ID =
								  "org.springframework.ide.eclipse.beans.core";
	private static final String BEANS_NATURE_ID = BEANS_CORE_PLUGIN_ID +
																".beansnature";
	private static final String BEANS_BUILDER_ID = BEANS_CORE_PLUGIN_ID +
															 ".beansvalidator";
	private static final String BEANS_PROJECT_DESCRIPTION = ".springBeansProject";

	private List selected = new ArrayList();

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		if (selection instanceof IStructuredSelection) {
			boolean enabled = true;
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				if (obj instanceof IProject) {
					IProject project = (IProject) obj;
					if (!project.isOpen()) {
						enabled = false;
						break;
					} else {
						selected.add(project);
					}
				} else {
					enabled = false;
					break;
				}
			}
			action.setEnabled(enabled);
		}
	}

	public void run(IAction action) {
		Iterator iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = (IProject) iter.next();
			migrateProject(project);
		}

		// Refresh label decoration for Spring beans project and config files
		BeansUILabelDecorator.update();
	}

	/**
	 * Removes SpringUI's nature and builder (validation) from given project.
	 * The SpringUI project description is migrated to the Beans project format.
	 * All SpringUI problem markers are deleted and finally the Beans project
	 * nature is added.
	 */
	private void migrateProject(IProject project) {

		// remove SpringUI nature and buidler
		SpringCoreUtils.removeProjectBuilder(project, BEANS_BUILDER_ID);
		SpringCoreUtils.removeProjectNature(project, BEANS_NATURE_ID);

		// rename Beans project description
		IFile file = project.getFile(new Path(BEANS_PROJECT_DESCRIPTION));
		if (file.isAccessible()) {
			try {
				file.move(new Path(IBeansProject.DESCRIPTION_FILE), true, null);
			} catch (CoreException e) {
				BeansUIPlugin.log(e);
			}
		}

		// finally add beans project nature
		SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID);
	}
}
