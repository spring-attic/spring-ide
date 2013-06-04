package org.springframework.ide.eclipse.gettingstarted.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager.DownloadRequestor;
import org.springframework.util.Assert;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

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
	 * until the download is complete.
	 */
	public File getFile() throws IOException {
		final File[] fileBox = new File[1];
		try {
			downloader.doWithDownload(this, new DownloadRequestor() {
				public void exec(File downloadedFile) throws Exception {
					fileBox[0] = downloadedFile;
					//TODO; validate file contents?
				}
			});
		} catch (Exception e) {
			throw new IOException("Download of "+url+" failed", e);
		}
		return fileBox[0];
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
		return downloader.isDownloaded(this);
	}
	
}
