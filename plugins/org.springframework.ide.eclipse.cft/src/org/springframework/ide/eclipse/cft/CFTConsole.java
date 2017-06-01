/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft;

import org.eclipse.cft.server.core.internal.CloudFoundryPlugin;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;

public class CFTConsole {

	private final String prefix;
	
	private static CFTConsole instance;

	public static final String MESSAGE_PREFIX = "[Spring IDE CFT]";

	public CFTConsole() {
		this(MESSAGE_PREFIX);
	}

	public static CFTConsole getDefault() {
		if (instance == null) {
			instance = new CFTConsole();
		}
		return instance;
	}

	public CFTConsole(String prefix) {
		this.prefix = prefix;
	}

	public void printToConsole(IModule module, CloudFoundryServer server, String message) {
		printToConsole(module, server, message, false);
	}

	public void printErrorToConsole(IModule module, CloudFoundryServer server, String message) {
		printToConsole(module, server, message, true);
	}

	public void printToConsole(IModule module, CloudFoundryServer server, String message, boolean error) {
		if (server != null) {
			CloudFoundryApplicationModule appModule = server.getExistingCloudModule(module);

			if (appModule != null) {
				message = prefix + " - " + message + '\n'; 
				CloudFoundryPlugin.getCallback().printToConsole(server, appModule, message, false, error);
			}
		}
	}
	
	public void printToConsole(String appName, CloudFoundryServer server, String message) {
		if (server != null) {
			try {
				CloudFoundryApplicationModule appModule = server.getExistingCloudModule(appName);

				if (appModule != null) {
					message = prefix + " - " + message + '\n'; 
					CloudFoundryPlugin.getCallback().printToConsole(server, appModule, message, false, false);
				}
			} catch (CoreException e) {
				Log.log(e);
			}
		}
	}
}
