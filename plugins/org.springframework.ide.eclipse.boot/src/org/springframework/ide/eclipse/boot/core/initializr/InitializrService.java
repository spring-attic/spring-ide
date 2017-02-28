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

public interface InitializrService {

	public static final InitializrService DEFAULT = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			try {
				URL initializrUrl = new URL(BootPreferences.getInitializrUrl());
				URL dependencyUrl = dependencyUrl(bootVersion, initializrUrl);
				return new SpringBootStarters(
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
	
	public static final InitializrService CACHING = new InitializrService() {
		
		private SpringBootStarters cached = null;
		
		{
			BootActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (BootPreferences.PREF_INITIALIZR_URL.equals(event.getProperty())) {
						cached = null;
					}
				}
			});
		}
		
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			if (cached == null) {
				cached = DEFAULT.getStarters(bootVersion);
			}
			return cached;
		}
	};

	SpringBootStarters getStarters(String bootVersion);

}
