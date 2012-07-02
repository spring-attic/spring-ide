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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CommandMarker;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginService;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;


/**
 * Commands for this add-on.
 * 
 * @author Steffen Pingel
 * @since 1.1
 */
@Component
@Service
public class AddOnEclipseCommands extends PluginService implements
		CommandMarker {

	@Reference
	private AddOnRooBotEclipseOperations operations;

	public List<Plugin> search(String searchTerms, boolean refresh,
			boolean trustedOnly, boolean compatibleOnly) {
		return operations.searchAddOns(searchTerms, refresh, trustedOnly,
				compatibleOnly, null);
	}

	@Override
	public InstallOrUpgradeStatus install(PluginVersion plugin) {
		return operations.installOrUpgradeAddOn(plugin, true);
	}

	@Override
	public InstallOrUpgradeStatus remove(PluginVersion plugin) {
		return operations.removeAddOn(plugin);
	}

	@Override
	public InstallOrUpgradeStatus upgrade(PluginVersion plugin) {
		return operations.installOrUpgradeAddOn(plugin, false);
	}

}