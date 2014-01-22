/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.html5.editor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.html5.IdeHtml5Plugin;
import org.springsource.ide.eclipse.commons.core.templates.TemplateProcessor;

/**
 * Makes a copy of some content from one directory to another. The target
 * directory is wiped before copying, but only one copy is made per STS session.
 * Subsequent copies requested will reuse the first copy (until STS is
 * restarted).
 * 
 * @author Kris De Volder
 */
public class ResourceCopier {

	private static ResourceCopier instance = null;

	/**
	 * Cache of processed content. This cache only retains content per Eclipse
	 * session. So restarting STS should begin with a clear cache.
	 */
	private final Map<String, File> copied = new HashMap<String, File>();

	private final File workdir;

	private ResourceCopier() {
		workdir = new File(IdeHtml5Plugin.getDefault().getStateLocation().toFile(), "resources");
		FileUtils.deleteQuietly(workdir);
	}

	public static synchronized ResourceCopier getInstance() {
		if (instance == null) {
			instance = new ResourceCopier();
		}
		return instance;
	}

	private static File urlToFile(URL fileURL) {
		try {
			// proper way to conver url to file:
			return new File(fileURL.toURI());
		}
		catch (URISyntaxException e) {
			// Deal with broken file urls (may contain spaces in unescaped form,
			// thanks Eclipse FileLocator!).
			// We will assume that if some chars are not escaped none of them
			// are escaped.
			return new File(fileURL.getFile());
		}
	}

	/**
	 * Copy directory contents from some source directory to some working
	 * directory
	 */
	public static File getCopy(String relativeResourceUrlString, IProgressMonitor mon) throws IOException {
		URL platformUrl = FileLocator.toFileURL(new URL("platform:/plugin/" + relativeResourceUrlString));
		return getInstance()._getCopy(urlToFile(platformUrl), mon);
	}

	/**
	 * Copy directory contents from some source directory to some working
	 * directory
	 */
	public static File getCopy(File from, IProgressMonitor mon) throws IOException {
		return getInstance()._getCopy(from, mon);
	}

	private File _getCopy(File from, IProgressMonitor mon) throws IOException {
		String key = from.getCanonicalPath();
		File cached = copied.get(key);
		if (cached != null && cached.exists()) {
			return cached;
		}
		File to = new File(workdir, generateFileName());
		if (!to.mkdirs()) {
			throw new IOException("Couldn't create dir " + to);
		}
		mon.beginTask("Instantiating Content", 3);
		try {
			Map<String, String> replacementContext = new HashMap<String, String>();
			TemplateProcessor processor = new TemplateProcessor(replacementContext);
			processor.process(from, to);
			copied.put(key, to);
			return to;
		}
		finally {
			mon.done();
		}
	}

	long count = System.currentTimeMillis();

	private String generateFileName() {
		return "" + (count++);
	}

}
