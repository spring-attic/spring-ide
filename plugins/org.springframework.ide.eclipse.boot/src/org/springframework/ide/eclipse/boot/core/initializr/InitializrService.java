/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public interface InitializrService {

	public static final InitializrService DEFAULT = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			try {
				URL initializrUrl = new URL(BootPreferences.getInitializrUrl());
				URL dependencyUrl = dependencyUrl(bootVersion, initializrUrl);
				return SpringBootStarters.load(
						initializrUrl, dependencyUrl,
						BootActivator.getUrlConnectionFactory()
				);
			} catch (Exception e) {
				Log.log(e);
			}
			return null;
		}

		private URL dependencyUrl(String bootVersion, URL initializerUrl) throws MalformedURLException {
			SimpleUriBuilder builder = new SimpleUriBuilder(initializerUrl.toString()+"/dependencies");
			builder.addParameter("bootVersion", bootVersion);
			return new URL(builder.toString());
		}
	};

	SpringBootStarters getStarters(String bootVersion) throws Exception;

	static InitializrService create(URLConnectionFactory urlConnectionFactory, String url) throws MalformedURLException {
		URL initializrUrl = new URL(url);
		return new InitializrService() {
			@Override
			public SpringBootStarters getStarters(String bootVersion) throws Exception {
				URL dependencyUrl = dependencyUrl(bootVersion, initializrUrl);
				return SpringBootStarters.load(
						initializrUrl, dependencyUrl,
						BootActivator.getUrlConnectionFactory()
				);
			}

			private URL dependencyUrl(String bootVersion, URL initializerUrl) throws MalformedURLException {
				SimpleUriBuilder builder = new SimpleUriBuilder(initializerUrl.toString()+"/dependencies");
				builder.addParameter("bootVersion", bootVersion);
				return new URL(builder.toString());
			}
		};
	}

}
