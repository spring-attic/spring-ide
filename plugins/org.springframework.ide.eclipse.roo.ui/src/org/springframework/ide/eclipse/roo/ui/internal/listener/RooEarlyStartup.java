/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.listener;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

/**
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @since 2.5.1
 */
public class RooEarlyStartup implements IStartup {

	static private RooProjectImportingListener listener;
	
	public void earlyStartup() {
		registerListener();
	}

	public static void registerListener() {
		if (listener == null) {
			listener = new RooProjectImportingListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		}
	}
	
	public static void unregisterListener() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}

}
