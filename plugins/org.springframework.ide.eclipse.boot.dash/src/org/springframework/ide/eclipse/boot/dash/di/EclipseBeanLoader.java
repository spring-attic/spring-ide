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
package org.springframework.ide.eclipse.boot.dash.di;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class EclipseBeanLoader {

	public interface Contribution {
		void applyBeanDefinitions(SimpleDIContext context) throws Exception;
	}

	final private SimpleDIContext context;

	public EclipseBeanLoader(SimpleDIContext context) {
		this.context = context;
	}

	public void loadFromExtensionPoint(String extensionPointId) {
		try {
			for (IConfigurationElement ce : Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId)) {
				Contribution contribution = (Contribution) ce.createExecutableExtension("class");
				contribution.applyBeanDefinitions(context);
			}
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

}
