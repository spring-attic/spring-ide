/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.content;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;

/**
 * A CodeSet represents a bunch of content that can somehow be imported into
 * the workspace to create a project.
 * <p>
 * A CodeSet instance is more like a handle to content than actual content.
 * I.e. it contains enough information to fetch the content but doesn't guarantee that the
 * content actually exists.
 * <p>
 * This means it is possible to create CodeSet instances without having to pay the
 * cost of downloading the Zip file upfront. Some operations on CodeSets however
 * will force download to be attempted.
 * 
 * @author Kris De Volder
 */
public abstract class CodeSet {

	/**
	 * Represents an Entry in a CodeSet. API similar to ZipEntry but paths may be remapped
	 * as they are relative to the root of the code set not the Zip (or other source of data)
	 */
	public abstract class CodeSetEntry {
		/**
		 * Return relative path of entry within the code set.
		 */
		public abstract IPath getPath();
	}

	public static abstract class Processor<T> {
		/**
		 * Do some work an a ZipEntry in a CodeSet. THe method may return true 
		 * or throw an exception to avoid processing additonal elements in a loop.
		 */
		public abstract T doit(CodeSetEntry e) throws Exception;
	}

	protected String name;

	public CodeSet(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public static CodeSet fromZip(String name, DownloadableItem zip, IPath root) {
		return new ZipFileCodeSet(name, zip, root);
	}
	
	/**
	 * A CodeSet stored in a Downloadable Zip File. The interesting data in the
	 * code set may be somewhere inside the zip file and is pointed to by
	 * a path relative to the zipfile root.
	 */
	public static class ZipFileCodeSet extends CodeSet {

		private DownloadableItem zipDownload;
		private IPath root;
		
		private Map<String, ZipEntry> entries = null;
		
		/**
		 * Ensures that zip file is downloaded and entries are parsed
		 * into a map. Only the first time this method is called do
		 * we do any actual work. The zip entries are cached
		 * after that.
		 */
		private void readEntries() throws ZipException, IOException {
			if (entries==null) {
				entries = new HashMap<String, ZipEntry>(1024);
				ZipFile zip = new ZipFile(zipDownload.getFile());
				try {
					Enumeration<? extends ZipEntry> iter = zip.entries();
					while (iter.hasMoreElements()) {
						ZipEntry el = iter.nextElement();
						Path zipPath = new Path(el.getName());
						if (root.isPrefixOf(zipPath)) {
							String key = zipPath.removeFirstSegments(root.segmentCount()).toString();
							if ("".equals(key)) {
								//path maches exactly, this means we hit the root of the
								// code set. Do not store it because the root of a codeset 
								// is not actually an element of the codeset! 
							} else {
								entries.put(key, el);
							}
						}
					}
				} finally {
					try {
						zip.close();
					} catch (IOException e) {
					}
				}
			}
		}

		private ZipFileCodeSet(String name, DownloadableItem zip, IPath root) {
			super(name);
			this.zipDownload = zip;
			this.root = root.makeRelative();
		}
		
		@Override
		public String toString() {
			return "ZipCodeSet("+name+", "+zipDownload.getURL()+"@"+root+")";
		}

		@Override
		public boolean exists() throws IOException {
			readEntries();
			return !entries.isEmpty();
		}

		public boolean hasFile(IPath path) {
			try {
				readEntries();
				return entries.containsKey(fileKey(path));
			} catch (Exception e) {
				GettingStartedActivator.log(e);
			}
			return false;
		}

		public boolean hasFolder(IPath path) {
			try {
				readEntries();
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
			//TODO: if we are going to use this to access actual data
			// in the zip, not just directory entries then we will need to
			// make sure the ZipFile is open during processing.
			readEntries();
			T result = null;
			for (ZipEntry e : entries.values()) {
				result = processor.doit(csEntry(e));
				if (result!=null) {
					//Bail out early when result found
					return result;
				}
			}
			return result;
		}

		/**
		 * Create a CodeSetEntry that wraps a ZipEntry
		 */
		private CodeSetEntry csEntry(ZipEntry e) {
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
					return getPath()+" in "+ZipFileCodeSet.this;
				}
			};
		}
		
	}

	/**
	 * Returns true if content for this codeset can be downloaded and
	 * contains some data.
     * 
     * If the CodeSet was not previously downloaded this will force a
     * download.
	 */
	public abstract boolean exists() throws IOException;

	/**
	 * Returns true if this codeset has a file with a given path.
	 * 
     * If the CodeSet was not previously downloaded this will force a
     * download.
	 */
	public abstract boolean hasFile(IPath path);
	
	/**
	 * Returns true if this codeset has a folder with a given path.
	 * 
     * If the CodeSet was not previously downloaded this will force a
     * download.
	 */
	public abstract boolean hasFolder(IPath path);

	/**
	 * Convenience method that allows passing paths as Strings instead of IPath instances
	 */
	public final boolean hasFile(String path) {
		return hasFile(new Path(path));
	}

	/**
	 * Perform some work on all content element in this codeSet.
	 * The processor may return a non-null value or throw an Exception to
	 * stop processing in the middle of the iteration.
	 */
	public abstract <T> T each(Processor<T> processor) throws Exception;

}
