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
import java.io.FilenameFilter;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager.DownloadRequestor;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.UIThreadDownloadDisallowed;

/**
 * An instance of this class represent a boot installation that can be
 * used to launch a 'app.groovy' spring Groovy CLI script.
 * <p>
 * The install may need to be fetched remotely befor it can be used.
 * This is done automatically the first time it is needed.
 * 
 * @author Kris De Volder
 */
public class BootInstall {

	/**
	 * TODO: DownloadableZipItem should probably be moved to commons
	 * (same place as DownloadableItem).
	 */
	public class DownloadableZipItem extends DownloadableItem {

		private String fileName; //Cached value for getFileName method.

		public DownloadableZipItem(URL url, DownloadManager downloader) {
			super(url, downloader);
		}
		
		@Override
		protected String getFileName() {
			if (fileName==null) {
				try {
					//Try to create human friendly name
					String name = new Path(getURL().getPath()).lastSegment();
					if (name!=null && name.endsWith(".zip")) {
						fileName = name;
					}
				} catch (Throwable e) {
					BootActivator.log(e);
				}
				//Ensure that filename is at least set to something.
				if (fileName==null) {
					fileName = super.getFileName();
				}
			}
			return fileName;
		}
		
		private synchronized void unzip(File zipFile, File unzipDir) throws Exception {
			if (unzipDir.exists()) {
				//Already unzipped by someone else.
				return;
			}
			try {
				ZipFileUtil.unzip(zipFile, unzipDir, null);
				return;
			} catch (Throwable e) {
				//If operation has an error or was aborted the unzipDir must be deleted because its probably junk.
				// data in it may not be valid.
				FileUtils.deleteQuietly(unzipDir);
				throw ExceptionUtil.exception(e);
			}
		}
		
		
		/**
		 * Force the item to be downloaded and unzipped locally. If an item is already downloaded
		 * the cached local file will be returned immediately. Otherwise the method will block
		 * until the download is complete or an error occurs.
		 * <p>
		 * The returned file will point to the location where the zipfile was expanded to.
		 */
		public File getFile() throws Exception {
			try {
				final File[] fileBox = new File[1];
				downloader.doWithDownload(this, new DownloadRequestor() {
					public void exec(File zipFile) throws Exception {
						String zipPath = zipFile.toString();
						Assert.isTrue(zipPath.endsWith(".zip"));
						File unzipDir = new File(zipPath.substring(0, zipPath.length()-4/*4=".zip".length()*/));
						unzip(zipFile, unzipDir);
						fileBox[0] = unzipDir;
					}
				});
				downloadStatus = Status.OK_STATUS;
				return fileBox[0];
			} catch (UIThreadDownloadDisallowed e) {
				//Shouldn't affect download status since it means download was not attempted
				throw e;
			} catch (Exception e) {
				downloadStatus = error(ExceptionUtil.getMessage(e));
				throw e;
			}
		}


	}

	private static final FilenameFilter JAR_FILE_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".jar");
		}
	};

	private DownloadableItem zip;
	private File home; //Will be set once the install is unzipped and ready for use.
	private File[] bootLibJars; //Set once we determined the location of the spring-boot jar(s) for this install.

	public BootInstall(DownloadManager downloader, String url) throws Exception {
		this.zip = new DownloadableZipItem(new URL(url), downloader);
	}

	public File getHome() throws Exception {
		if (home==null) {
			File unzipped = zip.getFile();
			//Assumes that unzipped will contain one directory 'spring-<version>'
			for (File dir : unzipped.listFiles()) {
				if (dir.isDirectory()) {
					String name = dir.getName();
					if (name.startsWith("spring-")) {
						home = dir;
					}
				}
			}
		}
		return home;
	}

	public File[] getBootLibJars() throws Exception {
		//Example: .../installs/spring-boot-cli-0.5.0.M6-bin/spring-0.5.0.M6/lib/spring-boot-cli-0.5.0.M6.jar
		
		if (bootLibJars==null) {
			File home = getHome(); //Example: .../installs/spring-boot-cli-0.5.0.M6-bin/spring-0.5.0.M6/
			//Expect to find spring-boot-cli-<version> in lib folder.
			bootLibJars = new File(home, "lib").listFiles(JAR_FILE_FILTER);
		}
		
		return bootLibJars;
	}

}
