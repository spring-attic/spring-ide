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

import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.cli.install.BootInstall;

/**
 * Sprint Boot Installation that is found in a local
 * directory manually installed by the user.
 * 
 * @author Kris De Volder
 */
public class LocalBootInstall extends BootInstall {
	
	private File home;
	private String version; //cache to avoid rescanning contents.

	public LocalBootInstall(File home, String name) {
		super(home.toURI().toString(), name);
		this.home = home;
	}

	@Override
	public File getHome() throws Exception {
		return home;
	}

	@Override
	public String getVersion() {
		try {
			if (version==null) {
				File[] jars = getBootLibJars();
				for (File file : jars) {
					//Lookin for a jar of the form "spring-boot-*-${version}.jar
					String name = file.getName();
					if (name.startsWith("spring-boot-") && name.endsWith(".jar")) {
						int end = name.length()-4; //4 chars in ".jar"
						int start = name.lastIndexOf("-");
						if (start>=0) {
							version = name.substring(start+1, end);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		} finally {
			//No matter what, set the version before proceeding from here.
			if (version==null) {
				version = super.getVersion();
			}
		}
		return version;
	}
	
	@Override
	public void clearCache() {
		//nothing to do since this doesn't need caching as its already local and unzipped
	}
	
	@Override
	protected boolean mayRequireDownload() {
		return false;
	}
}
