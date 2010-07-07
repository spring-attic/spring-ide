/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;


/**
 * Helper class that captures executions of Eclipse commands.
 * @author Christian Dupuis
 * @since 2.3.3
 */
public class ExtensionIdToBundleMapper {

	private final String extensionPointId;

	private IRegistryChangeListener listener = new IRegistryChangeListener() {
		public void registryChanged(IRegistryChangeEvent event) {
			if (extensionsAdded(event)) {
				clearCache();
			}
		}

		private boolean extensionsAdded(IRegistryChangeEvent event) {
			for (IExtensionDelta delta : event.getExtensionDeltas()) {
				if (delta.getExtensionPoint().getUniqueIdentifier().equals(extensionPointId))
					return true;
			}
			return false;
		}
	};

	private Map<String, String> map;

	public ExtensionIdToBundleMapper(String extensionPointId) {
		this.extensionPointId = extensionPointId;
		hookListeners();
	}

	public void dispose() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(listener);
		clearCache();
	}

	private synchronized void clearCache() {
		map = null;
	}

	/**
	 * This method walks through the commands registered via the extension registry and creates the {@link #map}.
	 */
	private synchronized void updateCommandToBundleMappings() {
		if (map != null)
			return;
		map = new HashMap<String, String>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionPointId);
		for (IConfigurationElement element : elements) {
			map.put(element.getAttribute("id"), element.getContributor().getName()); //$NON-NLS-1$
		}
	}

	protected synchronized String getBundleId(String extensionId) {
		updateCommandToBundleMappings();
		return map.get(extensionId);
	}

	void hookListeners() {
		Platform.getExtensionRegistry().addRegistryChangeListener(listener);
	}
}
