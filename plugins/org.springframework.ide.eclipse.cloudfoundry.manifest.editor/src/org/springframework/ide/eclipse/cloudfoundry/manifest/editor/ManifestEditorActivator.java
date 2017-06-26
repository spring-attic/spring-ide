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
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.inject.Provider;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class ManifestEditorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.cloudfoundry.manifest.editor"; //$NON-NLS-1$

	// The shared instance
	private static ManifestEditorActivator plugin;

	/**
	 * The constructor
	 */
	public ManifestEditorActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ManifestEditorActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		getDefault().getLog().log(ExceptionUtil.status(e));
	}
	
	
	/*
	 * "Framework" to contribute value hints into manifest editor
	 */
	
	private Provider<Collection<YValueHint>> buildpackProvider;

	public void setBuildpackProvider(Provider<Collection<YValueHint>> buildpackProvider) {
		this.buildpackProvider = buildpackProvider;
	}
	
	public Provider<Collection<YValueHint>> getBuildpackProvider() {
		return this.buildpackProvider;
	}
	
	public void setCfTargetLoginOptions(Map<String, Object> cfTargetLoginOptions) {
		try {
			Bundle lsBundle = Platform.getBundle("org.springframework.tooling.cloudfoundry.manifest.ls");
			if (lsBundle != null && lsBundle.getState() != Bundle.INSTALLED) {
				Class<?> lsClass = lsBundle.loadClass("org.springframework.tooling.cloudfoundry.manifest.ls.CloudFoundryManifestLanguageServer");
				Method lsMethod = lsClass.getMethod("setCfTargetLoginOptions", Object.class);
				lsMethod.invoke(null, cfTargetLoginOptions);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isLanguageServerEnabled() {
		Bundle lsBundle = Platform.getBundle("org.springframework.tooling.cloudfoundry.manifest.ls");
		return lsBundle != null && lsBundle.getState() != Bundle.INSTALLED;
	}
}
