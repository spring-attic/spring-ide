/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.repository.RepositoryTransport;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.LibraryUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.PartUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.ProjectContributionUsageMonitor;
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
		monitors.add(new ProjectContributionUsageMonitor());

		UIJob startupJob = new UIJob("Initializing Spring UAA") { //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor progressMonitor) {
				usageMonitorManager.start();

				for (IUsageMonitor monitor : monitors) {
					monitor.startMonitoring(usageMonitorManager);
				}

				URL url = DetectedProducts.PRODUCT_URL;
				RepositoryTransport transport = RepositoryTransport.getInstance();
				InputStream is = null;
				try {
					if (usageMonitorManager.canOpenUrl(url)) {
						is = transport.stream(url.toURI(), progressMonitor);
						DetectedProducts.setDocumentBuilderFactory(SpringCoreUtils.getDocumentBuilderFactory());
						DetectedProducts.setProducts(is);
					}
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

				if (usageMonitorManager.getPrivacyLevel() == IUaa.UNDECIDED_TOU) {
					UaaDialog dialog = UaaDialog.createDialog(SpringUIUtils.getStandardDisplay().getActiveShell());
					int resultCode = dialog.open();
					if (resultCode == Window.OK) {
						usageMonitorManager.setPrivacyLevel(IUaa.DEFAULT_PRIVACY_LEVEL);
					}
					else if (resultCode == 1000) {
						usageMonitorManager.setPrivacyLevel(IUaa.DECLINE_TOU);
					}
				}

				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule(5000);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		usageMonitorManager.stop();
		for (IUsageMonitor monitor : monitors) {
			monitor.stopMonitoring();
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
