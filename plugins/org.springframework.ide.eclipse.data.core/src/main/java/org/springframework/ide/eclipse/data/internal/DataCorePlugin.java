/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Central access point for the Spring Data Support plug-in (id
 * <code>"org.springframework.ide.eclipse.data.core"</code>).
 * 
 * @author Tomasz Zarna
 */
public class DataCorePlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Data Support (value <code>org.springframework.ide.eclipse.data.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.data.core";

	/** The shared instance */
	private static DataCorePlugin plugin;

	/**
	 * Creates the Spring Data Support plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public DataCorePlugin() {
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DataCorePlugin getDefault() {
		return plugin;
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus("Internal Error", exception));
	}

	/**
	 * Returns a new {@link IStatus} with status "ERROR" for this plug-in.
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		DataCoreImages.initializeImageRegistry(registry);
	}
}
