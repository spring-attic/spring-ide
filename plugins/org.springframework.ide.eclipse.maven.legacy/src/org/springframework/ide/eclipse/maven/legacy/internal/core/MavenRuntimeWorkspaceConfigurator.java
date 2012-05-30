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
package org.springframework.ide.eclipse.maven.legacy.internal.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.embedder.MavenRuntime;
import org.maven.ide.eclipse.embedder.MavenRuntimeManager;
import org.springframework.ide.eclipse.maven.legacy.MavenCorePlugin;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceLocationConfiguratorParticipant;


/**
 * {@link WorkspaceLocationConfiguratorParticipant} to configure external installed maven runtimes with M2Eclipse.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.5.0
 */
public class MavenRuntimeWorkspaceConfigurator extends WorkspaceLocationConfiguratorParticipant {

	/**
	 * Inner class to prevent binary dependency to M2Eclipse.
	 */
	private class M2EclipseDependentRuntimeExtension extends ConfigurableExtension {

		private File location;

		private MavenRuntime oldDefaultRuntime;

		public M2EclipseDependentRuntimeExtension(String id, File location) {
			super(id);
			this.location = location;
			setLocation(location.getAbsolutePath());
			setLabel("Maven Runtime");
			try {
				setConfigured(isRuntimeConfigured());
			} catch (IOException e) {
				// ignore
			}
		}

		public boolean isRuntimeConfigured() throws IOException {
			MavenRuntimeManager runtimeManager = MavenPlugin.getDefault().getMavenRuntimeManager();
			String path = location.getCanonicalPath();
			MavenRuntime runtime = runtimeManager.getRuntime(path);
			return (runtime != null && runtime.isAvailable());
		}
		
		public IStatus configure(IProgressMonitor monitor) {
			try {
				MavenRuntimeManager runtimeManager = MavenPlugin.getDefault().getMavenRuntimeManager();

				String path = location.getCanonicalPath();
				MavenRuntime runtime = runtimeManager.getRuntime(path);

				// Get the existing default runtime in case we need to revert later
				oldDefaultRuntime = runtimeManager.getDefaultRuntime();

				// If a runtime at the given path already exists make it the default
				if (runtime != null && runtime.isAvailable()) {
					if (!runtime.equals(oldDefaultRuntime)) {
						runtimeManager.setDefaultRuntime(runtime);
					}
					setConfigured(true);
					return new Status(IStatus.INFO, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, NLS.bind("Maven already configured at {0}", path));
				}
				else {
					// Create a new runtime; install it and make the default
					runtime = MavenRuntimeManager.createExternalRuntime(location.getCanonicalPath());
					List<MavenRuntime> runtimes = runtimeManager.getMavenRuntimes();
					runtimes.add(runtime);
					runtimeManager.setRuntimes(runtimes);
					runtimeManager.setDefaultRuntime(runtime);
					setConfigured(true);
					return new Status(IStatus.OK, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, NLS.bind("Maven successfully configured at {0}", location));
				}
			}
			catch (Exception e) {
				MavenCorePlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, 1, e.getMessage(), e));
				return new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, "Unexpected error during Maven runtime configuration", e);
			}
		}

		public IStatus unConfigure(IProgressMonitor monitor) {
			try {
				MavenRuntimeManager runtimeManager = MavenPlugin.getDefault().getMavenRuntimeManager();

				String path = location.getCanonicalPath();
				MavenRuntime runtime = runtimeManager.getRuntime(path);

				// Remove the newly added runtime
				if (runtime != null) {
					List<MavenRuntime> runtimes = runtimeManager.getMavenRuntimes();
					runtimes.remove(runtime);
					runtimeManager.setRuntimes(runtimes);
				}

				// Revert the default runtime
				if (oldDefaultRuntime != null) {
					runtimeManager.setDefaultRuntime(oldDefaultRuntime);
				}
			}
			catch (Exception e) {
				MavenCorePlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, 1, e.getMessage(), e));
			}
			return new Status(IStatus.OK, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, NLS.bind("Maven successfully unconfigured at {0}", location));
		}

	}

	@Override
	public String getPath() {
		return "maven-";
	}

	@Override
	public String[] getPaths() {
		return new String[] { getPath(), "apache-maven-" };
	}

	@Override
	public String getVersionRange() {
		return "[2.2.0,3.1.0)";
	}

	@Override
	protected ConfigurableExtension doCreateExtension(File location,
			IProgressMonitor monitor) {
		if (MavenCorePlugin.IS_M2ECLIPSE_PRESENT) {
			return new M2EclipseDependentRuntimeExtension(location.getName(), location);
		}
		return null;
	}

}
