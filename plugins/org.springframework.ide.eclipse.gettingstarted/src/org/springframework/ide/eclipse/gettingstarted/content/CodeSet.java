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
package org.springframework.ide.eclipse.gettingstarted.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet.Processor;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;
import org.springframework.ide.eclipse.gettingstarted.util.IOUtil;
import org.springframework.ide.eclipse.gettingstarted.util.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

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

		public abstract IPath getPath();

		public abstract boolean isDirectory();

		public abstract InputStream getData() throws IOException;
		
	}

	public static abstract class Processor<T> {
		
		/**
		 * Do some work an a ZipEntry in a CodeSet.
		 */
		public abstract T doit(CodeSetEntry e) throws Exception;
	}

	protected String name;
	private List<BuildType> buildTypes;

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
	 * Fetch list of build types that are supported by the CodeSet. Note that
	 * determining this information requires inspecting some of the directory 
	 * entries in the CodeSet and therefore it requires downloading the 
	 * content / zip / repo the CodeSet is contained in (unless it is
	 * already cached locally).
	 */
	public List<BuildType> getBuildTypes() throws UIThreadDownloadDisallowed {
		if (buildTypes==null) {
			buildTypes = new ArrayList<BuildType>();
			for (BuildType type : BuildType.values()) {
				if (hasFile(type.getBuildScript())) {
					buildTypes.add(type);
				}
			}
		}
		return buildTypes;
	}
	
	/**
	 * Returns true if content for this codeset can be downloaded and
	 * contains some data.
     * 
     * If the CodeSet was not previously downloaded this will force a
     * download.
	 */
	public abstract boolean exists() throws Exception;

	/**
	 * Returns true if this codeset has a file with a given path.
	 * 
     * If the CodeSet was not previously downloaded this will force a
     * download.
	 * @throws UIThreadDownloadDisallowed 
	 */
	public abstract boolean hasFile(IPath path) throws UIThreadDownloadDisallowed;
	
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
	public final boolean hasFile(String path) throws UIThreadDownloadDisallowed {
		return hasFile(new Path(path));
	}

	/**
	 * Perform some work on all content element in this codeSet.
	 * The processor may return a non-null value or throw an Exception to
	 * stop processing in the middle of the iteration.
	 */
	public abstract <T> T each(Processor<T> processor) throws Exception;

	/**
	 * Copies the contents of codeset to a given filesystem location
	 */
	public void createAt(final File location) throws Exception {
		if (location.exists()) {
			if (!FileUtils.deleteQuietly(location)) {
				throw new IOException("Data already exists at location and it could not be deleted: "+location);
			}
		}
		each(new CodeSet.Processor<Void>() {
			public Void doit(CodeSetEntry e) throws Exception {
				IPath path = e.getPath();
				File target = new File(location, path.toString());
				if (e.isDirectory()) {
					target.mkdirs();
				} else {
					IOUtil.pipe(e.getData(), target);
				}
				return null;
			}
		});
	}

	public ValidationResult validateBuildType(BuildType buildType) throws UIThreadDownloadDisallowed {
		String bs = buildType.getBuildScript().toString();
		if (!hasFile(bs)) {
			return ValidationResult.error(buildType.displayName()+" is not supported: there is no '"+bs+"'");
		}
		return ValidationResult.OK;
	}

	/**
	 * Read a single entry from a codeset. Note that this operation might be expensive because
	 * it might open and close a zipfile for a ZipFileCodeSet).
	 */
	public abstract <T> T readFileEntry(String path, Processor<T> processor) throws Exception;
	
}
