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
package org.springframework.ide.eclipse.boot.core.cli.install;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.ArrayEncoder;

public abstract class BootInstall implements IBootInstall {

	private static final File[] NO_FILES = new File[0];

	protected final String uriString;

	/**
	 * Creates a BootInstall pointing to given url and a optional 
	 * name. If the name is null or empty then a name will be
	 * generated automatically as needed.
	 */
	public BootInstall(String urlString, String name) {
		Assert.isNotNull(urlString);
		if (name!=null && !"".equals(name.trim())) {
			this.name = name;
		}
		this.uriString = urlString;
	}
	
	static final FilenameFilter JAR_FILE_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".jar");
		}
	};

	private static final String UNKNOWN_VERSION = "Unknown";

	File[] bootLibJars; //Set once we determined the location of the spring-boot jar(s) for this install.

	private String name;

	public abstract File getHome() throws Exception;

	public File[] getBootLibJars() throws Exception {
		//Example: .../installs/spring-boot-cli-0.5.0.M6-bin/spring-0.5.0.M6/lib/spring-boot-cli-0.5.0.M6.jar
		
		if (bootLibJars==null) {
			File home = getHome(); //Example: .../installs/spring-boot-cli-0.5.0.M6-bin/spring-0.5.0.M6/
			//Expect to find spring-boot-cli-<version> in lib folder.
			bootLibJars = new File(home, "lib").listFiles(JAR_FILE_FILTER);
			if (bootLibJars==null) {
				bootLibJars = NO_FILES;
			}
		}
		
		return bootLibJars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uriString == null) ? 0 : uriString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BootInstall other = (BootInstall) obj;
		if (uriString == null) {
			if (other.uriString != null)
				return false;
		} else if (!uriString.equals(other.uriString))
			return false;
		return true;
	}

	@Override
	public String getUrl() {
		return uriString;
	}
	
	/**
	 * Create a name from the uri. Let the name be 'short' but reflect interesting bits such
	 * as the uri scheme (if not file) and the last segment of the path including extension.
	 * <p>
	 * That way it is possible to see if a distro is:
	 *   remote <-> local
	 *   zip <-> folder 
	 */
	public String getName() {
		if (name==null) {
			name = defaultName();
		}
		return name;
	}

	/**
	 * Try to generate a good name for this boot install based on what is known about it
	 * (e.g. version, location etc).
	 */
	protected String defaultName() {
		String version = getVersion();
		if (version!=null && !version.equals(UNKNOWN_VERSION)) {
			return "Boot "+version;
		}
		String lastSegment = lastSegment();
		if (lastSegment!=null) {
			return lastSegment;
		}
		return "Boot";
	}

	public String lastSegment() {
		try {
			URI uri = new URI(uriString);
			String lastSegment = new Path(uri.getPath()).lastSegment();
			return lastSegment;
		} catch (Exception e) {
			BootActivator.log(e);
			return null;
		}
	}

	/**
	 * Try to determine the version of this installation based on what is known about it.
	 * Note that if a install is not downloaded yet we basically only have access to
	 * the Url String because it would be udesirable to download the entire zip just 
	 * to check the version.
	 */
	@Override
	public String getVersion() {
		String lastSegment = lastSegment();
		if (lastSegment!=null) {
			//Example: spring-boot-cli-0.5.0.M6-bin.zip
			if (lastSegment.toLowerCase().endsWith(".zip")) {
				//Expect format: <artifact-id>-<version>-<classifier>.zip
				int end = lastSegment.length()-4; //4 = '.zip'.length
				end = lastSegment.lastIndexOf('-', end); 
				//end is now at start of -<classifier>
				if (end>=0) {
					int start = lastSegment.lastIndexOf('-', end-1);
					if (start>=0) {
						//start at the - before the version string
						return lastSegment.substring(start+1, end);
					}
				}
			} else {
				//TODO: not a .zip assume its a folder.
			}
		}
		return UNKNOWN_VERSION;
	}
	
	@Override
	public IStatus validate() {
		try {
			if (mayRequireDownload()) {
				//don't validate when it might trigger a download in the UI thread.
				return Status.OK_STATUS;
			} else {
				File[] jars = getBootLibJars();
				if (jars==null || jars.length==0) {
					return new Status(IStatus.ERROR, BootActivator.PLUGIN_ID, "No boot install found at: "+getUrl());
				} else {
					//Anything that has lib folder with some jars will produce jars here....
					// so check at leats on jar name is the expected spring-boot-cli jar.
					for (File file : jars) {
						if (file.getName().startsWith("spring-boot-cli")) {
							return Status.OK_STATUS;
						}
					}
					//found some lib jars so probably ok
					return new Status(IStatus.ERROR, BootActivator.PLUGIN_ID, "spring-boot-cli jar not found in "+getUrl());
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
			return ExceptionUtil.status(e);
		}
	}

	/**
	 * For content that may be remote this method
	 * should return true. False should only be returned in the case
	 * where the content is known to be local or already cached.
	 * <p>
	 * In the case of uncertainty the method should conservatively return true (assuming the worst case, 
	 * scenario where a dowload will be required.
	 * <p>
	 * This method is used to determine whether it is safe to call methods that require
	 * the content of the install without triggering a lenghty download operation in the
	 * UI thread.
	 */
	protected boolean mayRequireDownload() {
		String url = getUrl();
		boolean isCertainlyLocal = url!=null && url.startsWith("file:");
		return !isCertainlyLocal;
	}
}
