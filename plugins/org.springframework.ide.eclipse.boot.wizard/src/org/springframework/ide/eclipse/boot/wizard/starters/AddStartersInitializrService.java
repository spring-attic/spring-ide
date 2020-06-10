/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Option;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrlBuilders;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

/**
 *
 * Wrapper around services that require connection to initializr. Injected into add starters wizard model and
 * allows for mocking in tests
 *
 */
public class AddStartersInitializrService {

	protected final URLConnectionFactory urlConnectionFactory;
	protected final InitializrUrlBuilders urlBuilders = new InitializrUrlBuilders();

	public AddStartersInitializrService(URLConnectionFactory urlConnectionFactory) {
		this.urlConnectionFactory = urlConnectionFactory;
	}

	public InitializrService getService(Supplier<String> url) {
		return InitializrService.create(urlConnectionFactory, url);
	}

	public InitializrProjectDownloader getProjectDownloader(String url) {
		return new InitializrProjectDownloader(urlConnectionFactory, url, urlBuilders);
	}

	public Option[] getSupportedBootReleaseVersions(String url) throws Exception {
		Option[] options = getServiceSpec(new URL(url)).getSingleSelectOptions("bootVersion");
		List<Option> releasesOnly = new ArrayList<>();
		int count = 0;
		if (options != null) {
			for (Option option : options) {
				// PT 172040311 - Do not include snapshot versions
				if (!option.getId().contains("SNAPSHOT")) {
					releasesOnly.add(option);
					count++;
				}
			}
		}
		return releasesOnly.toArray(new Option[count]);
	}

	public void checkBasicConnection(URL url) throws Exception {
		InitializrServiceSpec.checkBasicConnection(urlConnectionFactory, url);
	}

	private InitializrServiceSpec getServiceSpec(URL url) throws Exception {
		return InitializrServiceSpec.parseFrom(urlConnectionFactory, url);
	}

}
