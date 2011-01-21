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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.repository.RepositoryTransport;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.UaaManager;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.LibraryUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.NatureAndBuilderUsageMonitor;
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

	private DetectedProductsJob detectedProductsJob;

	/**
	 * {@inheritDoc}
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		usageMonitorManager = new UaaManager();

		monitors.add(new PartUsageMonitor());
		monitors.add(new CommandUsageMonitor());
		monitors.add(new LibraryUsageMonitor());
		monitors.add(new ServerUsageMonitor());
		monitors.add(new NatureAndBuilderUsageMonitor());

		Job startupJob = new Job("Initializing Spring UAA") { //$NON-NLS-1$

			public IStatus run(IProgressMonitor progressMonitor) {

				// Make sure that Platform is already started; if it is not re-schedule to retry later
				if (!PlatformUI.isWorkbenchRunning()) {
					schedule(5000);
				}
				else {
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

					// Create the download job for DetectedProducts.xml
					detectedProductsJob = new DetectedProductsJob();
					// Run it directly once as this jobs schedules itself after each run
					detectedProductsJob.run(progressMonitor);
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
				}

				public void run() throws Exception {
					monitor.stopMonitoring();
				}
			});
		}

		if (detectedProductsJob != null) {
			detectedProductsJob.cancel();
		}

		super.stop(context);
	}

	public static IUaa getUAA() {
		return getDefault().usageMonitorManager;
	}

	public static UaaPlugin getDefault() {
		return plugin;
	}

	/**
	 * {@link Job} implementation that downloads the DetectedProducts.xml file.
	 */
	private static class DetectedProductsJob extends Job {

		private static final long TIME_OUT = 3600000; // 1000ms * 60s * 60m

		private static final int WAIT_TIME = 120; // 120s

		private static final int MAX_ERRORS = 5;

		private volatile int errorCount = 0;

		public DetectedProductsJob() {
			super("Initializing Spring UAA");
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final CountDownLatch resultLatch = new CountDownLatch(1);

			// Create the download runnable to keep track of used time
			Runnable downloadRunnable = new Runnable() {

				public void run() {
					InputStream is = null;
					try {

						// Check that we are allowed to update Spring UAA
						int privacyLevel = UaaPlugin.getUAA().getPrivacyLevel();
						if (privacyLevel == IUaa.DECLINE_TOU || privacyLevel == IUaa.UNDECIDED_TOU) {
							return;
						}

						RepositoryTransport transport = RepositoryTransport.getInstance();
						is = transport.stream(DetectedProducts.PRODUCT_URL.toURI(), monitor);
						DetectedProducts.setDocumentBuilderFactory(SpringCoreUtils.getDocumentBuilderFactory());
						DetectedProducts.setProducts(is);
					}
					catch (Exception e) {
						errorCount++;
						plugin.getLog().log(
								new Status(IStatus.INFO, UaaPlugin.PLUGIN_ID,
										"Network connectivity issue occured in Spring UAA", e));
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
						resultLatch.countDown();
					}
				}
			};

			// Start the download job
			new Thread(downloadRunnable).start();

			try {
				// If the latch counted down to zero we assume successful network connection; at
				// least we assume that we don't get a stale connection in case of mis-configured proxies
				// which would lead most likely to a timeout
				if (resultLatch.await(WAIT_TIME, TimeUnit.SECONDS)) {
					// Schedule again for later but only until we reach the max error count
					if (errorCount < MAX_ERRORS && !monitor.isCanceled()) {
						schedule(TIME_OUT);
						return Status.OK_STATUS;
					}
				}
			}
			catch (InterruptedException e) {
				// Ignore
			}

			return Status.CANCEL_STATUS;
		}
	}

}
