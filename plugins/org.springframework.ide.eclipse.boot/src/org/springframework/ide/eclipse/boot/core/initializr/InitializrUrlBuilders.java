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

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Factory for creating an Initializr URL builder
 *
 */
public class InitializrUrlBuilders {

	public InitializrUrlBuilder getBuilder(ISpringBootProject bootProject, String initializrUrl) {
		return new InitializrUrlBuilder(initializrUrl) {
			@Override
			protected String resolveBaseUrl(String initializrUrl) {
				String url = super.resolveBaseUrl(initializrUrl);
				try {
					URI base = new URI(url);
					URI resolved = base.resolve("/starter.zip");
					url = resolved.toString();
				} catch (URISyntaxException e) {
					Log.log(e);
				}

				return url;
			}

		}
		.project(bootProject);
	}

}
