package org.springframework.ide.eclipse.gettingstarted.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;

/**
 * A CodeSet stored in a Downloadable Zip File. The interesting data in the
 * code set may be somewhere inside the zip file and is pointed to by
 * a path relative to the zipfile root.
 */
public class ZipFileCodeSet extends CodeSet {

	private DownloadableItem zipDownload;
	private IPath root;
	
	private Map<String, CodeSetEntry> entries = null;
	
	/**
	 * Ensures that zip file is downloaded and entries are parsed
	 * into a map. Only the first time this method is called do
	 * we do any actual work. The zip entries are cached
	 * after that.
	 */
	private void ensureEntryCache() throws Exception {
		if (entries==null) {
			entries = new HashMap<String, CodeSetEntry>(1024);
			each(new Processor<Void>() {
				@Override
				public Void doit(CodeSetEntry e) throws Exception {
					entries.put(e.getPath().toString(), e);
					return null;
				}
			});
		}
	}

	ZipFileCodeSet(String name, DownloadableItem zip, IPath root) {
		super(name);
		this.zipDownload = zip;
		this.root = root.makeRelative();
	}
	
	@Override
	public String toString() {
		return "ZipCodeSet("+name+", "+zipDownload.getURL()+"@"+root+")";
	}

	@Override
	public boolean exists() throws Exception {
		ensureEntryCache();
		return !entries.isEmpty();
	}

	public boolean hasFile(IPath path) {
		try {
			ensureEntryCache();
			return entries.containsKey(fileKey(path));
		} catch (Exception e) {
			GettingStartedActivator.log(e);
		}
		return false;
	}

	public boolean hasFolder(IPath path) {
		try {
			ensureEntryCache();
			return entries.containsKey(folderKey(path));
		} catch (Exception e) {
			GettingStartedActivator.log(e);
		}
		return false;
	}
	
	private String folderKey(IPath _path) {
		String path = fileKey(_path);
		if (!path.endsWith("/")) {
			//ZipEntries for dirs always end with a "/"
			path = path+"/";
		}
		return path;
	}

	private String fileKey(IPath path) {
		path = path.makeRelative();
		return path.toString();
	}

	@Override
	public <T> T each(Processor<T> processor) throws Exception {
		T result = null;
		ZipFile zip = new ZipFile(zipDownload.getFile());
		try {
			Enumeration<? extends ZipEntry> iter = zip.entries();
			while (iter.hasMoreElements() && result==null) {
				ZipEntry el = iter.nextElement();
				Path zipPath = new Path(el.getName());
				if (root.isPrefixOf(zipPath)) {
					String key = zipPath.removeFirstSegments(root.segmentCount()).toString();
					if ("".equals(key)) {
						//path maches exactly, this means we hit the root of the
						// code set. Do not store it because the root of a codeset 
						// is not actually an element of the codeset! 
					} else {
						CodeSetEntry cse = csEntry(zip, el);
						result = processor.doit(cse);
						if (result!=null) {
							//Bail out early when result found
							return result;
						}
					}
				}
			}
			return result;
		} finally {
			try {
				zip.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Create a CodeSetEntry that wraps a ZipEntry
	 */
	private CodeSetEntry csEntry(final ZipFile zip, final ZipEntry e) {
		IPath zipPath = new Path(e.getName()); //path relative to zip file
		Assert.isTrue(root.isPrefixOf(zipPath));
		final IPath csPath = zipPath.removeFirstSegments(root.segmentCount());
		return new CodeSetEntry() {
			@Override
			public IPath getPath() {
				return csPath;
			}
			
			@Override
			public String toString() {
				return getPath()+" in "+zipDownload;
			}

			@Override
			public boolean isDirectory() {
				return e.isDirectory();
			}

			@Override
			public InputStream getData() throws IOException {
				return zip.getInputStream(e);
			}
		};
	}
	
}