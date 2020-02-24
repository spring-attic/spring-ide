/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.cli.install.DownloadableZipItem;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Downloads projects from Initializr
 *
 */
public class InitializrProjectDownloader implements Disposable {

	/**
	 * There should only be one instance to avoid possibly memory leaks when using
	 * the downloadManager
	 */
	private DownloadManager downloadManager;

	private final String initializrUrl;
	private final URLConnectionFactory urlConnectionFactory;
	private final InitializrUrlBuilders urlBuilders;

	public InitializrProjectDownloader(URLConnectionFactory urlConnectionFactory,
			String initializrUrl,
			InitializrUrlBuilders urlBuilders) {
		this.initializrUrl = initializrUrl;
		this.urlConnectionFactory = urlConnectionFactory;
		this.urlBuilders = urlBuilders;
	}

	/**
	 *
	 * @param dependencies
	 * @param bootProject
	 * @return generated project from initializr, using project information from the
	 *         given boot project and list of dependencies
	 * @throws Exception
	 */
	public File getProject(List<Dependency> dependencies, ISpringBootProject bootProject) throws Exception {
		if (downloadManager == null) {
			downloadManager = new DownloadManager(urlConnectionFactory);
		}

		InitializrUrlBuilder builder = urlBuilders.getBuilder(bootProject, initializrUrl);
		String url = builder
				.dependencies(dependencies)
				.build();

		DownloadableItem item = new DownloadableZipItem(new URL(url), downloadManager);

		File file = item.getFile();

		return file;
	}

	@Override
	public void dispose() {
		if (downloadManager != null) {
			downloadManager.dispose();
		}
	}

}
