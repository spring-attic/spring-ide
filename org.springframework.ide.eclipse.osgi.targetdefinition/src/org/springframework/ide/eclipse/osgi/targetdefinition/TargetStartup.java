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
package org.springframework.ide.eclipse.osgi.targetdefinition;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.springframework.util.FileCopyUtils;

/**
 * {@link IStartup} that copies the contents of the target definition shipped with this plugin into the plugins state
 * location.
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class TargetStartup implements IStartup {

	@SuppressWarnings("unchecked")
	public void earlyStartup() {
		try {
			Bundle bundle = Activator.getDefault().getBundle();

			IPath root = Activator.getDefault().getStateLocation().append(
					bundle.getHeaders().get("Bundle-Version").toString()).append("target");
			File rootFolder = root.toFile();
			Enumeration<String> paths = bundle.getEntryPaths("/release/target/plugins/");
			while (paths.hasMoreElements()) {
				String path = paths.nextElement();
				if (path.endsWith(".jar")) {
					URL url = bundle.getEntry(path);
					int ix = url.getFile().lastIndexOf('/');
					File copy = new File(rootFolder, url.getFile().substring(ix));
					if (!copy.exists()) {
						if (!rootFolder.exists()) {
							rootFolder.mkdirs();
						}
						FileCopyUtils.copy(url.openStream(), new FileOutputStream(copy));
					}
				}
			}
		}
		catch (Exception e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, "Error provisioning Spring DM target", e));
		}
	}

}
