/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;

/**
 * Manages the boot installation that are configured in this workspace.
 * 
 * @author Kris De Volder
 */
public class BootInstallManager {
	
	//TODO: should use preferences to store boot installation and 
	// we should provide UI for the user to configure different installs etc.
	//For now a trivial install manager only knows about one Spring boot instal
	// and its location is injected via 'STSProperties'.
	
	private static BootInstallManager instance;
	
	private static File determineCacheDir() {
		IPath stateLocation = BootActivator.getDefault().getStateLocation();
		return stateLocation.append("installs").toFile();
	}

	
	public static BootInstallManager getInstance() throws Exception {
		if (instance==null) {
			instance = new BootInstallManager();
		}
		return instance;
	}
	
	private DownloadManager downloader;
	private BootInstall defaultInstall;
	
	private BootInstallManager() throws Exception {
		downloader = new DownloadManager(null, determineCacheDir());
		defaultInstall = new BootInstall(downloader, StsProperties.getInstance().get("spring.boot.zip.url"));
	}

	public BootInstall getDefaultInstall() {
		return defaultInstall;
	}

}
