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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellTab;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;
import org.springframework.ide.eclipse.roo.ui.internal.RooUiUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins.PluginManagerDialog;


/**
 * @author Steffen Pingel
 */
public class RooAddOnManagerDialog extends PluginManagerDialog {

	private RooShellTab tab;

	private final RooShellView view;

	public RooAddOnManagerDialog(Shell parentShell, List<IProject> projects, RooShellView view) {
		super(parentShell, projects);
		this.view = view;
	}

	public RooShellTab getTab() {
		return tab;
	}

	@Override
	protected Collection<IProject> updateProjects() {
		return RooUiUtil.getAllRooProjects();
	}

	@Override
	protected Collection<? extends Plugin> updatePlugins(boolean aggressive, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Retrieving Roo Add-ons", IProgressMonitor.UNKNOWN);

			monitor.subTask("Initializing Roo Shell");
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					tab = view.openShell(getSelectedProject(), null);
				}
			});
			try {
				IStatus status = tab.waitForInitialization(monitor);
				if (!status.isOK()) {
					// FIXME report error
					return Collections.emptyList();
				}
			}
			catch (InterruptedException e) {
				return Collections.emptyList();
			}

			monitor.subTask("Downloading List of Add-ons");
			return tab.getBootstrap().searchAddOns(null, aggressive, true, true);
		}
		finally {
			monitor.done();
		}
	}

	@Override
	protected boolean isPreinstalled(PluginVersion version) {
		return false;
	}

	@Override
	public String getMessage() {
		return "Browse Roo add-ons, and schedule add-ons to be installed, uninstalled, or updated. Changes take effect on dialog close.";
	}

	@Override
	public String getTitle() {
		return "Roo Add-on Manager";
	}

}
