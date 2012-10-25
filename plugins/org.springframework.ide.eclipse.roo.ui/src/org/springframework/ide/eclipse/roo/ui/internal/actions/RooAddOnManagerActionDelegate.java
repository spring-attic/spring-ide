/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellTab;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;
import org.springframework.ide.eclipse.roo.ui.internal.wizard.RooAddOnManagerDialog;
import org.springframework.roo.shell.eclipse.Bootstrap;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.actions.AbstractActionDelegate;
import org.springsource.ide.eclipse.commons.ui.UiUtil;


/**
 * Action to open the Roo Add-on Manager.
 * 
 * @author Steffen Pingel
 * @since 2.6
 */
public class RooAddOnManagerActionDelegate extends AbstractActionDelegate {

	public void run(IAction action) {
		try {
			IWorkbenchPart workbench = JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
			RooShellView view = (RooShellView) workbench.getSite().getPage()
					.showView(RooShellView.VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			run(view);
		}
		catch (PartInitException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, RooUiActivator.PLUGIN_ID, "Unexpected error opening add-on manager", e),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}

	public void run(RooShellView view) {
		List<IProject> projects = getSelectedProjects();
		RooAddOnManagerDialog dialog = new RooAddOnManagerDialog(UiUtil.getShell(), projects, view);
		if (dialog.open() == Window.OK && dialog.getTab() != null) {
			performInstall(dialog, dialog.getTab());
		}
	}

	private void performInstall(RooAddOnManagerDialog dialog, final RooShellTab tab) {
		final Collection<PluginVersion> toInstall = dialog.getSelectedToInstall();
		final Collection<PluginVersion> toUninstall = dialog.getSelectedtoUninstall();
		final Bootstrap bootstrap = tab.getBootstrap();
		Job installJob = new Job("Installing Roo Add-ons") {
			public IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Performing operations", toInstall.size() + toUninstall.size());
					return doInstall(tab, bootstrap, toInstall, toUninstall, monitor);
				}
				finally {
					monitor.done();
				}
			}
		};
		PlatformUI.getWorkbench().getProgressService().showInDialog(getShell(), installJob);
		installJob.schedule();
	}

	private IStatus doInstall(RooShellTab tab, Bootstrap bootstrap, Collection<PluginVersion> toInstall,
			Collection<PluginVersion> toUninstall, IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(RooUiActivator.PLUGIN_ID, 0, "One or more install operations failed.",
				null);

		if (!monitor.isCanceled()) {
			for (PluginVersion pluginVersion : toInstall) {
				monitor.subTask(NLS.bind("Installing {0}", pluginVersion.toString()));
				
				
				IStatus result;
				if (pluginVersion.getParent().isInstalled()) {
					// Get some output onto the console
					tab.getStyledTextAppender().append("addon upgrade bundle --bundleSymbolicName " + pluginVersion.getName() + ";" + 
							pluginVersion.getVersion() + System.getProperty("line.separator"), Level.INFO.intValue());
					result = bootstrap.update(pluginVersion);
				}
				else {
					// Get some output onto the console
					tab.getStyledTextAppender().append("addon install bundle --bundleSymbolicName " + pluginVersion.getName() + ";" + 
							pluginVersion.getVersion() + System.getProperty("line.separator"), Level.INFO.intValue());
					result = bootstrap.install(pluginVersion);
				}
				if (!result.isOK()) {
					MultiStatus child = new MultiStatus(RooUiActivator.PLUGIN_ID, 0, NLS.bind(
							"Installation of {0} failed", pluginVersion.toString()), null);
					child.add(result);
					status.add(child);
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		}

		if (!monitor.isCanceled()) {
			for (PluginVersion pluginVersion : toUninstall) {
				monitor.subTask(NLS.bind("Uninstalling {0}", pluginVersion.toString()));

				IStatus result = bootstrap.uninstall(pluginVersion);
				if (!result.isOK()) {
					MultiStatus child = new MultiStatus(RooUiActivator.PLUGIN_ID, 0, NLS.bind(
							"Uninstallation of {0} failed", pluginVersion.toString()), null);
					child.add(result);
					status.add(child);
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		}

		return status;
	}

}
