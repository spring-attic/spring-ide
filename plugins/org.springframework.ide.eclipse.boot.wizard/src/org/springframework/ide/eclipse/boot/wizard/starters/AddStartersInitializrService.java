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
import java.util.function.Supplier;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrlBuilders;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

public class AddStartersInitializrService {

	private final URLConnectionFactory urlConnectionFactory;

	public AddStartersInitializrService(URLConnectionFactory urlConnectionFactory) {
		this.urlConnectionFactory = urlConnectionFactory;
	}
	public InitializrService getService(Supplier<String> url) {
		return InitializrService.create(urlConnectionFactory, url);
	}

	public InitializrProjectDownloader getProjectDownloader(String url, InitializrUrlBuilders urlBuilders) {
		return new InitializrProjectDownloader(urlConnectionFactory, url, urlBuilders);
	}

	public InitializrServiceSpec getServiceSpec(URL url) throws Exception {
		return InitializrServiceSpec.parseFrom(urlConnectionFactory, url);
	}

	public void checkBasicConnection(URL url) throws Exception {
		InitializrServiceSpec.checkBasicConnection(urlConnectionFactory, url);
	}

}
