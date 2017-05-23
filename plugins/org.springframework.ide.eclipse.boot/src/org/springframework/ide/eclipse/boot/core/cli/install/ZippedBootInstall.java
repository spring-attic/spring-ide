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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.cli.BootCliCommand;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager.DownloadRequestor;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * A Boot Installation that is located in a zip file. It must be unzipped locally
 * before it can be used.
 * <p>
 * This class takes care of downloading and unzipping it automatically to to
 * cache directory located in the workspace.
 *
 * @author Kris De Volder
 */
public class ZippedBootInstall extends BootInstall {
	
	private static final String ORG_SPRINGFRAMEWORK_CLOUD_SPRING_CLOUD_CLI = "org.springframework.cloud:spring-cloud-cli:";
	private Supplier<CloudCliInstall> cloudCliSupplier = Suppliers.memoize(() -> initCloudCliInstall());

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
					Log.log(e);
				}
				//Ensure that filename is at least set to something that ends with .zip
				if (fileName==null) {
					fileName = super.getFileName()+".zip";
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
						File unzipDir = getUnzipDir();
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

		@Override
		public void clearCache() {
			synchronized (downloader) {
				super.clearCache();
				File unzipDir = zip.getUnzipDir();
				FileUtils.deleteQuietly(unzipDir);
			}
		}
	}

	private DownloadableItem zip;
	private File home; //Will be set once the install is unzipped and ready for use.

	public ZippedBootInstall(DownloadManager downloader, String uri, String name) throws Exception {
		super(uri, name);
		this.zip = new DownloadableZipItem(new URL(uri), downloader);
	}

	@Override
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

	@Override
	public String getUrl() {
		return uriString;
	}

	@Override
	protected boolean mayRequireDownload() {
		//We can do better than just looking at the url (as the super method does).
		//We can see whether or not the zip file was dowloaded already or not.
		if (zip!=null) {
			return !zip.isDownloaded();
		} else {
			return super.mayRequireDownload();
		}
	}

	@Override
	public void clearCache() {
		if (zip!=null) {
			zip.clearCache();
		}
	}
	
	private Version getLatestCloudCliVersion() {
		Version bootVersion = Version.valueOf(getVersion());
		if (VersionRange.valueOf("1.5.3").includes(bootVersion)) {
			return Version.valueOf(StsProperties.getInstance().get("spring.boot.cloud.default.version"));
		} else if (VersionRange.valueOf("[1.4.4,1.5.2)").includes(bootVersion)) {
			return Version.valueOf("1.3.0.RELEASE");
		} else if (VersionRange.valueOf("[1.2.2,1.4.4)").includes(bootVersion)) {
			return Version.valueOf("1.2.3.RELEASE");
//		} else if (VersionRange.valueOf("[1.3.7, 1.4.1)").includes(bootVersion)) {
//			return Version.valueOf("1.1.6.RELEASE");
//		} else if (VersionRange.valueOf("[1.3.5,1.3.7)").includes(bootVersion)) {
//			return Version.valueOf("1.1.5.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.8,1.3.5)").includes(bootVersion)) {
//			return Version.valueOf("1.0.6.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.6,1.2.8)").includes(bootVersion)) {
//			return Version.valueOf("1.0.4.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.4,1.2.6)").includes(bootVersion)) {
//			return Version.valueOf("1.0.3.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.3,1.2.4)").includes(bootVersion)) {
//			return Version.valueOf("1.0.2.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.2,1.2.3)").includes(bootVersion)) {
//			return Version.valueOf("1.0.0.RELEASE");
		} else {
			return null;
		}
	}	

	private CloudCliInstall initCloudCliInstall() {
		File[] springCloudJars = findExtensionJars(CLOUD_CLI_LIB_PREFIX);
		if (springCloudJars == null || springCloudJars.length == 0) {
			try {
				BootCliCommand cmd = new BootCliCommand(getHome());
				Version latestCloudCliVersion = getLatestCloudCliVersion();
				if (latestCloudCliVersion == null) {
					return null;
				}
				int result = cmd.execute(IBootInstallExtension.INSTALL_COMMAND, ORG_SPRINGFRAMEWORK_CLOUD_SPRING_CLOUD_CLI + latestCloudCliVersion);
				if (result != 0) {
					return null;
				} else {
					springCloudJars = findExtensionJars(CLOUD_CLI_LIB_PREFIX);
				}
			} catch (Exception e) {
				BootActivator.getDefault().getLog().log(ExceptionUtil.coreException(e).getStatus());
				return null;
			}
		}
		return springCloudJars == null || springCloudJars.length == 0 ? null : new CachingCloudCliInstall(this) {
			
		};
	}

	@Override
	public CloudCliInstall getCloudCliInstall() {
		return cloudCliSupplier.get();
	}

}
