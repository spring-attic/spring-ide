/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

/**
 * Base class for handling save and restore of simple boolean preferences.
 * Depending if a {@link IProject} is passed in {@link #isEnabled(IProject)} and
 * {@link #setEnabled(boolean,IProject)} the preference is stored in the
 * project preferences or under global workspace scope.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class PersistablePreferenceObjectSupport {

	private boolean isEnabledByDefault = true;

	/**
	 * Returns the preference id that should be used for storing the value
	 * @return the preference key
	 */
	protected abstract String getPreferenceId();

	protected boolean hasProjectSpecificOptions(IProject project) {
		return SpringCorePreferences.getProjectPreferences(project).getBoolean(
				SpringCore.PROJECT_PROPERTY_ID, false);
	}

	/**
	 * Checks if the current preference identified by a call to
	 * {@link #getPreferenceId()} is enabled in the scope of the given project.
	 * <p>
	 * If the project is <code>null</code> the check is executed against the
	 * global workspace settings.
	 * @return true if preference is enabled for the given project
	 */
	public boolean isEnabled(IProject project) {
		if (project != null && hasProjectSpecificOptions(project)) {
			return SpringCorePreferences.getProjectPreferences(project)
					.getBoolean(getPreferenceId(), this.isEnabledByDefault);
		}
		else {
			return SpringCore.getDefault().getPluginPreferences().getBoolean(
					getPreferenceIdForPluginPreference());
		}
	}

	/**
	 * Sets this preferences to the value of <code>isEnabled</code>.
	 * <p>
	 * If the project is <code>null</code> the preference will be stored in
	 * the global workspace settings.
	 * @param isEnabled
	 * @param project
	 */
	public void setEnabled(boolean isEnabled, IProject project) {
		if (project != null) {
			SpringCorePreferences.getProjectPreferences(project).putBoolean(
					getPreferenceId(), isEnabled);
		}
		else {
			SpringCore.getDefault().getPluginPreferences().setValue(
					getPreferenceIdForPluginPreference(), isEnabled);
		}
		onEnablementChanged(isEnabled, project);
	}

	private String getPreferenceIdForPluginPreference() {
		return SpringCore.PLUGIN_ID + "." + getPreferenceId();
	}

	/**
	 * Set the default value if no value has been stored
	 * @param isEnabledByDefault
	 */
	protected void setEnabledByDefault(boolean isEnabledByDefault) {
		this.isEnabledByDefault = isEnabledByDefault;
		SpringCore.getDefault().getPluginPreferences().setDefault(
				getPreferenceIdForPluginPreference(), this.isEnabledByDefault);
	}

	/**
	 * Called after the value has been changed by a call to
	 * {@link #setEnabled(boolean, IProject)}.
	 * <p>
	 * This default implementation is empty but can be overridden by subclasses
	 * to executed some kind of lifecycle management (e.g. cleanup of
	 * {@link IMarker}).
	 * @param isEnabled
	 * @param project
	 */
	protected void onEnablementChanged(boolean isEnabled, IProject project) {
	}
}