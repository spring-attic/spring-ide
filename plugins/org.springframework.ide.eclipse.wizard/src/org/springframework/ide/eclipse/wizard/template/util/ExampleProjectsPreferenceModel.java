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
import org.springframework.ide.eclipse.wizard.WizardPlugin;

public class ExampleProjectsPreferenceModel extends AbstractNameUrlPreferenceModel {

	private static ExampleProjectsPreferenceModel instance;

	public static ExampleProjectsPreferenceModel getInstance() {
		if (instance == null) {
			instance = new ExampleProjectsPreferenceModel();
		}
		return instance;
	}

	public final static String DEFAULT_FILENAME = "/defaultExampleUrls.properties";

	public ExampleProjectsPreferenceModel() {
		super();
	}

	@Override
	protected String getStoreKey() {
		return "examples.name.url.key";
	}

	@Override
	protected String getDefaultFilename() {
		return DEFAULT_FILENAME;
	}

	@Override
	protected IEclipsePreferences getStore() {
		return InstanceScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID);
	}

	@Override
	protected IEclipsePreferences getDefaultStore() {
		return DefaultScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID);
	}

	@Override
	protected void setOptionalFlagValue(boolean flagValue) {
		// not applicable
	}

	@Override
	protected boolean getOptionalFlagValue() {
		return false;
	}

	@Override
	protected String getStoreOptionalFlagKey() {
		return "example.notused.key";
	}

}
