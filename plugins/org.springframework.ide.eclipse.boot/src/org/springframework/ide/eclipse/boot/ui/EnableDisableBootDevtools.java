/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

public class EnableDisableBootDevtools implements IObjectActionDelegate {

	private static final String SPRING_BOOT_DEVTOOLS = "spring-boot-devtools";
	private IProject project;
	private IWorkbenchPart activePart;
	private ISpringBootProject bootProject;

	@Override
	public void run(IAction action) {
		try {
			SpringBootStarter devtools = getUsedDevtools(bootProject);
			if (devtools!=null) {
				bootProject.removeStarter(devtools);
			} else {
				devtools = getAvaibleDevtools(bootProject);
				if (devtools!=null) {
					bootProject.addStarter(devtools);
				} else {
					MessageDialog.openError(activePart.getSite().getShell(), "Boot Devtools Dependency could not be added", explainFailure());
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(activePart.getSite().getShell(), "Unexpected failure",
					"The action to add/remove devtools unexpectedly failed with an error:\n" +
					ExceptionUtil.getMessage(e) + "\n" +
					"The error log may contain further information.");
			BootActivator.log(e);
		}
	}

	private String explainFailure() throws Exception {
		if (project==null) {
			return "No project selected";
		} else if (!BootPropertyTester.isBootProject(project)) {
			return "Project '"+project.getProject().getName()+"' does not seem to be a Spring Boot project";
		} else if (!project.hasNature(SpringBootCore.M2E_NATURE)) {
			return "Project '"+project.getProject().getName()+"' is not an Maven/m2e enabled project. This action's implementation requires m2e to add/remove "
					+ "the Devtools as a dependency to your project.";
		} else {
			String version = bootProject.getBootVersion();
			return "Boot Devtools are provided by Spring Boot version 1.3.0 or later. "
					+ "Project '"+project.getProject().getName()+"' uses Boot Version "+version;
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			project = getProject(selection);
			bootProject = getBootProject(project);
		} catch (Exception e) {
			BootActivator.log(e);
		}
		action.setEnabled(project!=null);
		if (bootProject!=null) {
			boolean hasDevtools = null!=getUsedDevtools(bootProject);
			action.setText(hasDevtools?"Remove Boot Devtools":"Add Boot Devtools");
		} else if (project!=null) {
			//action shouldn't really be enabled, but it is enabled so that it can
			// fail with an explanation when the user tries it.
			action.setText("Add/Remove Boot Devtools");
		}
	}

	private SpringBootStarter getAvaibleDevtools(ISpringBootProject project) {
		try {
			if (project!=null) {
				return getStarter(SPRING_BOOT_DEVTOOLS, project.getKnownStarters());
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private SpringBootStarter getUsedDevtools(ISpringBootProject project) {
		try {
			if (project!=null) {
				return getStarter(SPRING_BOOT_DEVTOOLS, project.getBootStarters());
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private SpringBootStarter getStarter(String id, List<SpringBootStarter> starters) {
		for (SpringBootStarter s : starters) {
			if (s.getArtifactId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	private IProject getProject(ISelection selection) {
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size()==1) {
					Object el = ss.getFirstElement();
					if (el instanceof IProject) {
						IProject p = (IProject) el;
						if (p.isAccessible() && p.hasNature(SpringCore.NATURE_ID)) {
							//only interested in spring projects.
							return p;
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private ISpringBootProject getBootProject(IProject project) {
		try {
			if (project!=null) {
				return SpringBootCore.create(project);
			}
		} catch (Exception e) {
			if (!isExpected(e)) {
				BootActivator.log(e);
			}
		}
		return null;
	}

	private boolean isExpected(Exception e) {
		//See https://issuetracker.springsource.com/browse/STS-4263
		String msg = ExceptionUtil.getMessage(e);
		return msg!=null && msg.contains("only implemented for m2e");
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		activePart = targetPart;
	}

}
