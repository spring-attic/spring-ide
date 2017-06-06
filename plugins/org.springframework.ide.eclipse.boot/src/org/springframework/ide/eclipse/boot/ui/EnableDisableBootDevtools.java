/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class EnableDisableBootDevtools implements IObjectActionDelegate {

	private static final VersionRange DEVTOOLS_SUPPORTED = new VersionRange("1.3.0");
	public static final String SPRING_BOOT_DEVTOOLS_AID = "spring-boot-devtools";
	public static final String SPRING_BOOT_DEVTOOLS_GID = "org.springframework.boot";
	private static final SpringBootStarter DEVTOOLS_STARTER = new SpringBootStarter("devtools",
		new MavenCoordinates(SPRING_BOOT_DEVTOOLS_GID, SPRING_BOOT_DEVTOOLS_AID, null),
		"compile", /*bom*/null, /*repo*/null
	);
	private SpringBootCore springBootCore;

	private IProject project;
	private IWorkbenchPart activePart;
	private ISpringBootProject bootProject;

	/**
	 * Constructor that eclipse calls when it instantiates the delegate
	 */
	public EnableDisableBootDevtools() {
		this(SpringBootCore.getDefault());
	}

	/**
	 * Constructor that test code can use to inject mocks etc.
	 */
	public EnableDisableBootDevtools(SpringBootCore springBootCore) {
		this.springBootCore = springBootCore;
	}

	@Override
	public void run(IAction action) {
		try {
			SpringBootStarter devtools = getAvaibleDevtools(bootProject);
			if (hasDevTools(bootProject)) {
				bootProject.removeMavenDependency(devtools.getMavenId());
			} else {
				if (devtools!=null) {
					bootProject.addMavenDependency(devtools.getDependency(), /*preferManaged*/true);
				} else {
					MessageDialog.openError(activePart.getSite().getShell(), "Boot Devtools Dependency could not be added", explainFailure());
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
			MessageDialog.openError(activePart.getSite().getShell(), "Unexpected failure",
					"The action to add/remove devtools unexpectedly failed with an error:\n" +
					ExceptionUtil.getMessage(e) + "\n" +
					"The error log may contain further information.");
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
			try {
				action.setText(fastHasDevTools(bootProject)?"Remove Boot Devtools":"Add Boot Devtools");
			} catch (TimeoutException | InterruptedException e) {
				action.setText("Add/Remove Boot Devtools");
			} catch (Exception e) {
				//Unexpected
				Log.log(e);
			}
		} else if (project!=null) {
			//action shouldn't really be enabled, but it is enabled so that it can
			// fail with an explanation when the user tries it.
			action.setText("Add/Remove Boot Devtools");
		}
	}

	/**
	 * Like hasDevTools, but suitable for calling on the UI thread. This operation may fail
	 * with a {@link TimeoutException} if it can not be readily determined whether a project
	 * has dev tools as a dependency (this may happen, for example because m2e is still in the process of
	 * resolving the dependencies). It would be undesirable to block on the UI thread to wait for this
	 * process to complete.
	 */
	private boolean fastHasDevTools(ISpringBootProject bootProject) throws TimeoutException, InterruptedException, ExecutionException {
		CompletableFuture<Boolean> result = new CompletableFuture<>();
		Job job = new Job("Check for devtools") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					result.complete(hasDevTools(bootProject));
				} catch (Throwable e) {
					result.completeExceptionally(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		return result.get(100, TimeUnit.MILLISECONDS);
	}

	private boolean hasDevTools(ISpringBootProject bootProject) {
		try {
			List<IMavenCoordinates> deps = bootProject.getDependencies();
			if (deps!=null) {
				for (IMavenCoordinates d : deps) {
					if (SPRING_BOOT_DEVTOOLS_AID.equals(d.getArtifactId())) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return false;
	}

	private SpringBootStarter getAvaibleDevtools(ISpringBootProject project) {
		try {
			String versionString = project.getBootVersion();
			if (StringUtils.isNotBlank(versionString) && DEVTOOLS_SUPPORTED.includes(new Version(versionString))) {
				return DEVTOOLS_STARTER;
			}
		} catch (Exception e) {
			BootActivator.log(e);
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
						if (p.isAccessible() && p.hasNature(SpringCoreUtils.NATURE_ID)) {
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
				return springBootCore.project(project);
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
