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

import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.SimpleUriBuilder;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public interface InitializrService {

	public static final InitializrService DEFAULT = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			try {
				URL initializrUrl = StsProperties.getInstance().url("spring.initializr.json.url");
				URL dependencyUrl = dependencyUrl(bootVersion, initializrUrl);
				return new SpringBootStarters(
						initializrUrl, dependencyUrl,
						BootWizardActivator.getUrlConnectionFactory()
				);
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return null;
		}

		private URL dependencyUrl(String bootVersion, URL initializerUrl) throws MalformedURLException {
			SimpleUriBuilder builder = new SimpleUriBuilder(initializerUrl.toString()+"/dependencies");
			builder.addParameter("bootVersion", bootVersion);
			return new URL(builder.toString());
		}
	};

	SpringBootStarters getStarters(String bootVersion);

}
