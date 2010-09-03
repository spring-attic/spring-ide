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

import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.PartUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.UaaManager;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;

/**
 * Plug-in entry point for the UAA Plugin.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class UaaPlugin extends AbstractUIPlugin {

	public static final int FULL_DATA = PrivacyLevel.ENABLE_UAA.getNumber();

	public static final int LIMITED_DATA = PrivacyLevel.LIMITED_DATA.getNumber();

	public static final int NO_DATA = PrivacyLevel.DISABLE_UAA.getNumber();

	public static final int DEFAULT_PRIVACY_LEVEL = LIMITED_DATA;

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.uaa"; //$NON-NLS-1$

	private static UaaPlugin plugin;

	private CommandUsageMonitor commandUsageMonitor;

	private PartUsageMonitor partUsageMonitor;

	private UaaManager usageMonitorManager;

	public int getPrivacyLevel() {
		return this.usageMonitorManager.getPrivacyLevel();
	}

	public String getUserAgentContents(String value) {
		return this.usageMonitorManager.getUserAgentContents(value);
	}

	public String getUserAgentHeader() {
		if (this.usageMonitorManager != null) {
			return usageMonitorManager.getUserAgentHeader();
		}
		return DefaultHttpParams.getDefaultParams().getParameter(HttpClientParams.USER_AGENT).toString();
	}
	
	public void registerFeatureUse(String feature) {
		if (feature != null && this.usageMonitorManager != null) {
			this.usageMonitorManager.registerFeatureUse(feature);
		}
	}

	public void setPrivacyLevel(int level) {
		this.usageMonitorManager.setPrivacyLevel(level);
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		UIJob job = new UIJob("Spring UAA Starter") { //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor progressMonitor) {
				usageMonitorManager = new UaaManager();
				partUsageMonitor = new PartUsageMonitor();
				commandUsageMonitor = new CommandUsageMonitor();
				partUsageMonitor.startMonitoring();
				commandUsageMonitor.startMonitoring();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(1000);
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (partUsageMonitor != null) {
			partUsageMonitor.stopMonitoring();
		}
		if (commandUsageMonitor != null) {
			commandUsageMonitor.stopMonitoring();
		}
		super.stop(context);
	}

	public static UaaPlugin getDefault() {
		return plugin;
	}

}
