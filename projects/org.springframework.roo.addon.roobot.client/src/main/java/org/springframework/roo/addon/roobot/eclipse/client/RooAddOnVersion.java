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
package org.springframework.roo.addon.roobot.eclipse.client;

import org.springframework.roo.addon.roobot.client.model.Bundle;
import org.springframework.roo.addon.roobot.client.model.BundleVersion;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;


public class RooAddOnVersion extends PluginVersion {

	private final BundleVersion bundleVersion;
	private final Bundle bundle;

	public RooAddOnVersion(Bundle bundle, BundleVersion bundleVersion) {
		this.bundle = bundle;
		this.bundleVersion = bundleVersion;
	}
	
	public BundleVersion getBundleVersion() {
		return bundleVersion;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
	
}
