/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.monitor;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.json.simple.JSONObject;
import org.springframework.ide.eclipse.internal.uaa.IUaa;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.UaaManager;

/**
 * {@link IUsageMonitor} that tracks usages of WTP server instances.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class ServerUsageMonitor implements IUsageMonitor {

	private static final String SERVERS_EXTENSION_POINT = "org.eclipse.wst.server.core.serverTypes"; //$NON-NLS-1$

	private IServerListener serverListener;

	private IServerLifecycleListener serverLifecycleListener;

	private ExtensionIdToBundleMapper serverToBundleIdMapper;

	private IUaa manager;

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;

		serverToBundleIdMapper = new ExtensionIdToBundleMapper(SERVERS_EXTENSION_POINT);

		serverLifecycleListener = new ServerLifecycleMonitor();
		ServerCore.addServerLifecycleListener(serverLifecycleListener);

		// Register listener with already existing servers
		serverListener = new ServerMonitor();
		for (IServer server : ServerCore.getServers()) {
			server.addServerListener(serverListener);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void stopMonitoring() {
		ServerCore.removeServerLifecycleListener(serverLifecycleListener);

		for (IServer server : ServerCore.getServers()) {
			server.removeServerListener(serverListener);
		}
	}

	/**
	 * {@link IServerListener} that dispatches {@link ServerEvent}s to the {@link UaaManager}.
	 * @since 2.5.2
	 */
	private class ServerMonitor implements IServerListener {

		@SuppressWarnings("unchecked")
		public void serverChanged(ServerEvent event) {
			IServer server = event.getServer();
			JSONObject json = new JSONObject();
			json.put("runtime", server.getRuntime().getId());
			manager.registerFeatureUse(serverToBundleIdMapper.getBundleId(event.getServer().getServerType().getId()),
					json.toJSONString());
		}
	}

	/**
	 * {@link IServerLifecycleListener} to install and uninstall {@link IServerListener} on added and removed servers.
	 * @since 2.5.2
	 */
	private class ServerLifecycleMonitor implements IServerLifecycleListener {

		public void serverAdded(IServer server) {
			server.addServerListener(serverListener);
		}

		public void serverChanged(IServer server) {
		}

		public void serverRemoved(IServer server) {
			server.removeServerListener(serverListener);
		}
	}

}
