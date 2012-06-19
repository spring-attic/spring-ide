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

import java.util.List;

import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginService.InstallOrUpgradeStatus;


/**
 * Interface for operations offered by this add-on.
 * 
 * @author Steffen Pingel
 * @since 1.0
 */
public interface AddOnRooBotEclipseOperations {

	public InstallOrUpgradeStatus installOrUpgradeAddOn(
			PluginVersion pluginVersion, boolean install);

	public InstallOrUpgradeStatus removeAddOn(PluginVersion pluginVersion);

	List<Plugin> searchAddOns(String searchTerms, boolean refresh,
			boolean trustedOnly, boolean compatibleOnly, String requiresCommand);

}