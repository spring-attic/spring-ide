/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public interface InitializrService {

	public static final InitializrService DEFAULT = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			try {
				return new SpringBootStarters(bootVersion,
						StsProperties.getInstance().url("spring.initializr.json.url"),
						WizardPlugin.getUrlConnectionFactory()
				);
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return null;
		}
	};

	SpringBootStarters getStarters(String bootVersion);

}
