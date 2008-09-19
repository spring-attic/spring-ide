/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class ProjectContributionEventListenerFactory {

	public static final String LISTENERS_EXTENSION_POINT = SpringCore.PLUGIN_ID
			+ ".listeners";

	public static List<IProjectContributionEventListener> getProjectContributionEventListeners() {
		List<IProjectContributionEventListener> listenerDefinitions =
				new ArrayList<IProjectContributionEventListener>();
		for (IExtension extension : Platform.getExtensionRegistry()
				.getExtensionPoint(LISTENERS_EXTENSION_POINT).getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				try {
					Object listener = element.createExecutableExtension("class");
					if (listener instanceof IProjectContributionEventListener) {
						listenerDefinitions.add((IProjectContributionEventListener) listener);
					}
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		
		return listenerDefinitions;
	}
}
