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
package org.springframework.ide.eclipse.gettingstarted.util;

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager.DownloadRequestor;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * A DownloadableItem is something that can be downloaded and
 * stored in the local file system as a file.
 * 
 * @author Kris De Volder
 */
public class DownloadableItem {
	
	private URL url;
	private DownloadManager downloader;
	private String name; //optional name. If set this name will be used as filename in the cache otherwise
						// suitable name will be computed.
	private ValidationResult downloadStatus = ValidationResult.OK; //error message if download failed. Otherwise contains 'OK'.
												                   // Not
	
	public DownloadableItem(URL url, DownloadManager downloader) {
		this.url = url;
		this.downloader = downloader;
	}

	public void setFileName(String n) {
		this.name = n;
	}
	
	/**
	 * A downloadable item must provide a URI where its contents can be fetched from.
	 */
	public URL getURL() {
		return url;
	}
	
	/**
	 * Force the item to be downloaded to a local File. If an item is already downloaded
	 * the cached local file will be returned immediately. Otherwise the method will block
	 * until the download is complete or an error occurs.
	 */
	public File getFile() throws Exception {
		try {
			final File[] fileBox = new File[1];
			downloader.doWithDownload(this, new DownloadRequestor() {
				public void exec(File downloadedFile) throws Exception {
					fileBox[0] = downloadedFile;
					//TODO; validate file contents?
				}
			});
			downloadStatus = ValidationResult.OK;
			return fileBox[0];
		} catch (UIThreadDownloadDisallowed e) {
			//Shouldn't affect download status since it means download was not attempted
			throw e;
		} catch (Exception e) {
			downloadStatus = ValidationResult.error(ExceptionUtil.getMessage(e));
			throw e;
		}
	}
	
	/**
	 * A downloadable item must provide a filename where its cached downloaded contents
	 * should be stored. The name must uniquely identify the downloadable item
	 * within the scope of the DownloadManager used to download this item.
	 * <p>
	 * The name must also be a legal file name on the current OS.
	 * <p>
	 * A default implementation is provided that uses sha1 and Base64 encoding 
	 * to generate a name from the uri.
	 * <p>
	 * These generated names are not guaranteed to be unique, but the chance of
	 * a collisions is astronomically small.
	 */
	protected String getFileName() {
		if (name==null) {
			try {
				MessageDigest sha1encoder = MessageDigest.getInstance("sha1");
				byte[] bytes = sha1encoder.digest((""+getURL()).getBytes());
				name = new String(Base64.encodeBase64(bytes));
			} catch (NoSuchAlgorithmException e) {
				//This should not be possible
				throw new Error(e);
			}
		}
		return name;
	}
	
	@Override
	public String toString() {
		return url.toString();
	}

	public boolean isDownloaded() {
		return downloader!=null && downloader.isDownloaded(this);
	}

	/**
	 * Error message if download failed. Other
	 * @return
	 */
	public ValidationResult getDownloadStatus() {
		return downloadStatus;
	}

}
