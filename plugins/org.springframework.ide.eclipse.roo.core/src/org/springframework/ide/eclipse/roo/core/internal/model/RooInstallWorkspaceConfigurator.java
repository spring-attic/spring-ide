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
package org.springframework.ide.eclipse.roo.core.internal.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.core.model.RooInstallManager;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceLocationConfiguratorParticipant;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.0
 */
public class RooInstallWorkspaceConfigurator extends WorkspaceLocationConfiguratorParticipant {

	private class RooExtension extends ConfigurableExtension {

		private File location;

		public RooExtension(String id, File location) {
			super(id);
			this.location = location;
			setLocation(location.getAbsolutePath());
			setLabel("Roo");
			try {
				setConfigured(getRooInstall() != null);
			}
			catch (IOException e) {
				// ignore
			}
		}

		@Override
		public String getBundleId() {
			Version version = getVersion(location.getName());
			if ("RELEASE".equals(version.getQualifier())) {
				return "org.springframework.roo.bundle";
			}
			return "org.springframework.roo.bundle.development";
		}
		
		public IRooInstall getRooInstall() throws IOException {
			RooInstallManager installManager = RooCoreActivator.getDefault().getInstallManager();
			String path = location.getCanonicalPath();
			for (IRooInstall existingInstall : installManager.getAllInstalls()) {
				if (new File(existingInstall.getHome()).getCanonicalPath().equals(path)) {
					return existingInstall;
				}
			}
			return null;
		}

		@Override
		public IStatus configure(IProgressMonitor monitor) {
			RooInstallManager installManager = RooCoreActivator.getDefault().getInstallManager();
			try {
				String path = location.getCanonicalPath();
				
				// Check existing installs if path is already configured
				if (getRooInstall() != null) {						
					return new Status(IStatus.INFO, RooCoreActivator.PLUGIN_ID, NLS.bind("Roo runtime already configured at {0}", path));
				}

				// Save the old install path for later
				oldDefaultPath = (installManager.getDefaultRooInstall() != null ? installManager.getDefaultRooInstall()
						.getHome() : null);

				/*
				 * Generate the name for new install 
				 * spring-roo-1.0.2.RELEASE -> Roo 1.0.2.RELEASE 
				 * roo-1.0.2.RELASE -> Roo 1.0.2.RELEASE
				 * spring-roo-1.0.2.BUILD-201005061233 -> Roo 1.0.2.BUILD-201005061233 
				 * roo-1.0.2.BUILD-201005061233 -> Roo 1.0.2.BUILD-201005061233
				 */
				String name = getName(location.getName());

				// Create a new install and make it default
				IRooInstall newInstall = new DefaultRooInstall(path, name, true);

				// Set installs
				Set<IRooInstall> installs = new HashSet<IRooInstall>();
				installs.add(newInstall);
				for (IRooInstall install : installManager.getAllInstalls()) {
					installs.add(new DefaultRooInstall(install.getHome(), install.getName(), false));
				}
				installManager.setRooInstalls(installs);
				
				setConfigured(true);
				return new Status(IStatus.OK, RooCoreActivator.PLUGIN_ID, NLS.bind("Roo runtime successfully configured at {0}", location));
			}
			catch (IOException e) {
				RooCoreActivator.log(e);
				return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "Unexpected error during Roo runtime configuration", e);
			}
		}

		@Override
		public IStatus unConfigure(IProgressMonitor monitor) {
			RooInstallManager installManager = RooCoreActivator.getDefault().getInstallManager();
			try {
				String path = location.getCanonicalPath();

				Set<IRooInstall> installs = new HashSet<IRooInstall>();
				for (IRooInstall install : installManager.getAllInstalls()) {
					if (!path.equals(install.getHome())) {
						installs.add(new DefaultRooInstall(install.getHome(), install.getHome(), install.getHome().equals(
								oldDefaultPath)));
					}
				}

				installManager.setRooInstalls(installs);
			}
			catch (IOException e) {
				RooCoreActivator.log(e);
			}
			return new Status(IStatus.OK, RooCoreActivator.PLUGIN_ID, NLS.bind("Roo runtime successfully unconfigured at {0}", location));
		}
		

		private String generateName(String name) {
			if (!isDuplicateName(name)) {
				return name;
			}

			if (name.matches(".*\\(\\d*\\)")) {
				int start = name.lastIndexOf('(');
				int end = name.lastIndexOf(')');
				String stringInt = name.substring(start + 1, end);
				int numericValue = Integer.parseInt(stringInt);
				String newName = name.substring(0, start + 1) + (numericValue + 1) + ")"; //$NON-NLS-1$
				return generateName(newName);
			}
			else {
				return generateName(name + " (1)");
			}
		}

		private String getName(String name) {
			for (String prefix : getPaths()) {
				if (name.startsWith(prefix)) {
					String newName = name.substring(prefix.length());
					int ix = newName.indexOf('-');
					return generateName("Roo " + newName.substring(ix + 1));
				}
			}
			return name;
		}

		private boolean isDuplicateName(String name) {
			for (IRooInstall vm : RooCoreActivator.getDefault().getInstallManager().getAllInstalls()) {
				if (vm.getName().equals(name)) {
					return true;
				}
			}
			return false;
		}

	}
	
	private String oldDefaultPath = null;

	public String[] getPaths() {
		return new String[] { "roo-", "spring-roo-" };
	}

	public String getVersionRange() {
		return IRooInstall.SUPPORTED_VERSION;
	}
	
	@Override
	protected ConfigurableExtension doCreateExtension(File location, IProgressMonitor monitor) {
		return new RooExtension(location.getName(), location);
	}

}
