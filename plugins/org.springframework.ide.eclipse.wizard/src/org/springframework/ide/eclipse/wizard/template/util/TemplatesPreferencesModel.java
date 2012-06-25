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

/*
 * @author Kaitlin Duck Sherwood
 */
package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

public class TemplatesPreferencesModel extends AbstractNameUrlPreferenceModel {

	public final static String DEFAULT_FILENAME = "/defaultTemplateUrls.properties";

	private static TemplatesPreferencesModel instance;

	public static TemplatesPreferencesModel getInstance() {
		if (instance == null) {
			instance = new TemplatesPreferencesModel();
		}
		return instance;
	}

	public TemplatesPreferencesModel() {
		super();
	}

	@Override
	protected String getStoreKey() {
		return ContentManager.RESOURCE_CONTENT_DESCRIPTORS;
	}

	protected String getStoreSelfHostingKey() {
		return "templates.selfhosting.key";
	}

	@Override
	protected String getDefaultFilename() {
		return DEFAULT_FILENAME;
	}

	@Override
	protected void setDefaults() {
		super.setDefaults();
		// We don't really need to store the default value in defaultStore since
		// we can get the default value from the second argument in the
		// store.get()
		getDefaultStore().putBoolean(getStoreSelfHostingKey(), optionalFlagDefault());
	}

	@Override
	public void revert() {
		super.revert();
		optionalFlagValue = getStore().getBoolean(getStoreSelfHostingKey(), optionalFlagDefault());
	}

	@Override
	protected IEclipsePreferences getStore() {
		return InstanceScope.INSTANCE.getNode(CorePlugin.PLUGIN_ID);
	}

	@Override
	protected IEclipsePreferences getDefaultStore() {
		return DefaultScope.INSTANCE.getNode(CorePlugin.PLUGIN_ID);
	}

	@Override
	protected String getStoreOptionalFlagKey() {
		return getStoreSelfHostingKey();
	}

	public boolean shouldShowSelfHostedProjects() {
		return getOptionalFlagValue();
	}

}
