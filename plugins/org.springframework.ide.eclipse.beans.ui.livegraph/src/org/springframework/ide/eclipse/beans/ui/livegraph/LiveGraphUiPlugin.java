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
package org.springframework.ide.eclipse.beans.ui.livegraph;

import org.eclipse.jface.preference.IPreferenceStore;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static LiveGraphUiPlugin getDefault() {
		return plugin;
	}

}
