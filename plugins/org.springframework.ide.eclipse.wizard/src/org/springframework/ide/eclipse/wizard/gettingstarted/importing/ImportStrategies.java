/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;

public class ImportStrategies {

	/**
	 * This method is provided to more easily wrap up the old import strategies, defined before
	 * we provided an extension point. It is deprecated because stuff calling this method
	 * should really be converted to use the extension point instead to contribute an ImportStrategy.
	 */
	@Deprecated
	public static ImportStrategyFactory forClass(final String className) {
		return new ImportStrategyFactory() {
			@Override
			public ImportStrategy create(BuildType buildType, String notInstalledMessage, String name) throws Exception {
				@SuppressWarnings("unchecked")
				Class<? extends ImportStrategy> klass =  (Class<? extends ImportStrategy>) Class.forName(className);
				return klass.newInstance();
			}
		};
	}

}
