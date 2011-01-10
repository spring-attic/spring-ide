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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.repository.RepositoryTransport;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.UaaManager;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.LibraryUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.PartUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.ServerUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.preferences.UaaDialog;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.uaa.client.DetectedProducts;

/**
 * Plug-in entry point for the UAA Plugin.
 * @author Christian Dupuis
 * @since 2.5.2
 */
@SuppressWarnings("restriction")
public class UaaPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.uaa"; //$NON-NLS-1$

	private static UaaPlugin plugin;

	private List<IUsageMonitor> monitors = new ArrayList<IUsageMonitor>();

	private UaaManager usageMonitorManager;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		usageMonitorManager = new UaaManager();

		monitors.clear();
		monitors.add(new PartUsageMonitor());
		monitors.add(new CommandUsageMonitor());
		monitors.add(new LibraryUsageMonitor());
		monitors.add(new ServerUsageMonitor());

		Job startupJob = new Job("Initializing Spring UAA") { //$NON-NLS-1$

			public IStatus run(IProgressMonitor progressMonitor) {
				usageMonitorManager.start();

				for (final IUsageMonitor monitor : monitors) {
					SafeRunner.run(new ISafeRunnable() {

						public void handleException(Throwable e) {
							UaaPlugin
									.getDefault()
									.getLog()
									.log(new Status(IStatus.WARNING, UaaPlugin.PLUGIN_ID,
											"Error occured starting Spring UAA usage monitor", e));
						}

						public void run() throws Exception {
							monitor.startMonitoring(usageMonitorManager);
						}
					});
				}

				URL url = DetectedProducts.PRODUCT_URL;
				RepositoryTransport transport = RepositoryTransport.getInstance();
				InputStream is = null;
				try {
					is = transport.stream(url.toURI(), progressMonitor);
					DetectedProducts.setDocumentBuilderFactory(SpringCoreUtils.getDocumentBuilderFactory());
					DetectedProducts.setProducts(is);
				}
				catch (Exception e) {
					plugin.getLog().log(
							new Status(IStatus.ERROR, UaaPlugin.PLUGIN_ID,
									"Error downloading Spring UAA detected products", e));
				}
				finally {
					if (is != null) {
						try {
							is.close();
						}
						catch (IOException e) {
							// Ignore
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule(1000);

		if (usageMonitorManager.getPrivacyLevel() == IUaa.UNDECIDED_TOU) {

			UIJob dialogJob = new UIJob("Download consent required") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					UaaDialog dialog = UaaDialog.createDialog(SpringUIUtils.getStandardDisplay().getActiveShell());
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
		usageMonitorManager.stop();
		for (final IUsageMonitor monitor : monitors) {
			SafeRunner.run(new ISafeRunnable() {

				public void handleException(Throwable e) {
					UaaPlugin
							.getDefault()
							.getLog()
							.log(new Status(IStatus.WARNING, UaaPlugin.PLUGIN_ID,
									"Error occured stopping Spring UAA usage monitor", e));
				}

				public void run() throws Exception {
					monitor.stopMonitoring();
				}
			});
		}
		super.stop(context);
	}

	public static IUaa getUAA() {
		return getDefault().usageMonitorManager;
	}

	public static UaaPlugin getDefault() {
		return plugin;
	}

}
