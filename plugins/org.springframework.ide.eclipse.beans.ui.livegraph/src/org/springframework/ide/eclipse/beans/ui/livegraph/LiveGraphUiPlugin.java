/*******************************************************************************
 *  Copyright (c) 2012, 2016 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Leo Dos Santos
 */
public class LiveGraphUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.ui.livegraph"; //$NON-NLS-1$

	// The shared instance
	private static LiveGraphUiPlugin plugin;

	/**
	 * The constructor
	 */
	public LiveGraphUiPlugin() {
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(LiveBeansGraphView.PREF_DISPLAY_MODE, LiveBeansGraphView.DISPLAY_MODE_GRAPH);
		store.setDefault(LiveBeansGraphView.PREF_GROUP_MODE, LiveBeansGraphView.GROUP_BY_RESOURCE);
		store.setDefault(LiveBeansGraphView.PREF_FILTER_INNER_BEANS, true);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		LiveGraphUIImages.initializeImageRegistry(registry);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static LiveGraphUiPlugin getDefault() {
		return plugin;
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(exception));
	}

	public static IStatus createErrorStatus(Throwable exception) {
		String message = exception.getMessage();
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

}
