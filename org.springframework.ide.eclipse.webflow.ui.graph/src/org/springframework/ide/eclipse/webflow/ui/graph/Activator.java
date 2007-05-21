/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.webflow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EndState;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.ViewState;
import org.springframework.ide.eclipse.webflow.ui.graph.preferences.WebflowGraphPreferences;

/**
 * The Activator of the Web Flow Graph OSGi bundle
 * @author Christian Dupuis
 * @since 2.0
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.webflow.ui.graph";

	private static Activator plugin;

	public Activator() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(
				createErrorStatus("Internal Error", exception));
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(WebflowGraphPreferences.getPreferenceName(ViewState.class.getName()),
				WebflowGraphPreferences.getDefaultColorStringForModelElementClass(ViewState.class));
		store.setDefault(WebflowGraphPreferences.getPreferenceName(ActionState.class.getName()),
				WebflowGraphPreferences.getDefaultColorStringForModelElementClass(ActionState.class));
		store.setDefault(WebflowGraphPreferences.getPreferenceName(EndState.class.getName()),
				WebflowGraphPreferences.getDefaultColorStringForModelElementClass(EndState.class));
		store.setDefault(WebflowGraphPreferences.getPreferenceName(SubflowState.class.getName()),
				WebflowGraphPreferences.getDefaultColorStringForModelElementClass(SubflowState.class));
		store.setDefault(WebflowGraphPreferences.getPreferenceName(DecisionState.class.getName()),
				WebflowGraphPreferences.getDefaultColorStringForModelElementClass(DecisionState.class));
	}
}
