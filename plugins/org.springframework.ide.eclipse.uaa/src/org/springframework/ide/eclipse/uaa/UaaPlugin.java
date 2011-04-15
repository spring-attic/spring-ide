/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.uaa;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.UaaManager;
import org.springframework.ide.eclipse.internal.uaa.monitor.BuildSystemUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.LibraryUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.NatureAndBuilderUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.PartUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.ServerUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.preferences.UaaDialog;
import org.springframework.uaa.client.UaaService;

/**
 * Plug-in entry point for the UAA Plugin.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.uaa"; //$NON-NLS-1$

	private static UaaPlugin plugin;

	private List<IUsageMonitor> monitors = new ArrayList<IUsageMonitor>();

	private UaaManager usageMonitorManager;

	private ServiceTracker proxyServiceTracker;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		usageMonitorManager = new UaaManager();

		Job startupJob = new Job("Initializing Spring UAA") { //$NON-NLS-1$

			public IStatus run(IProgressMonitor progressMonitor) {

				// Make sure that Platform is already started; if it is not re-schedule to retry later
				if (!PlatformUI.isWorkbenchRunning()) {
					schedule(5000);
				}
				else {
					monitors.add(new PartUsageMonitor());
					monitors.add(new CommandUsageMonitor());
					monitors.add(new LibraryUsageMonitor());
					monitors.add(new ServerUsageMonitor());
					monitors.add(new NatureAndBuilderUsageMonitor());
					monitors.add(new BuildSystemUsageMonitor());

					usageMonitorManager.start();

					for (final IUsageMonitor monitor : monitors) {
						SafeRunner.run(new ISafeRunnable() {

							public void handleException(Throwable e) {
							}

							public void run() throws Exception {
								monitor.startMonitoring(usageMonitorManager);
							}
						});
					}
				}
				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule(5000);

		if (usageMonitorManager.getPrivacyLevel() == IUaa.UNDECIDED_TOU) {

			UIJob dialogJob = new UIJob("Download consent required") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (usageMonitorManager.getPrivacyLevel() != IUaa.UNDECIDED_TOU) {
						// privacy level has changed in the meantime
						return Status.OK_STATUS;
					}

					UaaDialog dialog = UaaDialog.createDialog(UaaUtils.getStandardDisplay().getActiveShell());
					int resultCode = dialog.open();
					if (resultCode == Window.OK) {
						usageMonitorManager.setPrivacyLevel(IUaa.DEFAULT_PRIVACY_LEVEL);
					}
					else if (resultCode == 1000) {
						usageMonitorManager.setPrivacyLevel(IUaa.DECLINE_TOU);
					}

					return Status.OK_STATUS;
				}
			};
			dialogJob.schedule(5000);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		for (final IUsageMonitor monitor : monitors) {
			SafeRunner.run(new ISafeRunnable() {

				public void handleException(Throwable e) {
				}

				public void run() throws Exception {
					monitor.stopMonitoring();
				}
			});
		}

		usageMonitorManager.stop();
		super.stop(context);
	}

	public static IUaa getUAA() {
		return getDefault().usageMonitorManager;
	}
	
	public static UaaService getUaaService() {
		return getDefault().usageMonitorManager.getUaaService();
	}

	public static UaaPlugin getDefault() {
		return plugin;
	}

	public IProxyService getProxyService() {
		try {
			if (proxyServiceTracker == null) {
				proxyServiceTracker = new ServiceTracker(plugin.getBundle().getBundleContext(),
						IProxyService.class.getName(), null);
				proxyServiceTracker.open();
			}
			return (IProxyService) proxyServiceTracker.getService();
		}
		catch (Exception e) {

		}
		return null;
	}

}
