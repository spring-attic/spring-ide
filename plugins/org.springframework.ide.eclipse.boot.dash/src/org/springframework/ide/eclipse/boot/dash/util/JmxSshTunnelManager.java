/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.JmxSupport;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * An instance of this class keeps track of open ssh tunnels for JMX connections to
 * remote hosts. It is capable of producing a (Live)Set of JMX urls for all the
 * open tunnels.
 */
public class JmxSshTunnelManager {

	private Map<SshTunnel, CloudAppDashElement> tunnels = new HashMap<>();

	private ObservableSet<List<String>> jmxUrls = ObservableSet.<List<String>>builder()
			.refresh(AsyncMode.ASYNC)
			.compute(this::collectUrls)
			.build();

	private synchronized ImmutableSet<List<String>> collectUrls() {
		Builder<List<String>> builder = ImmutableSet.builder();
		for (Entry<SshTunnel, CloudAppDashElement> entry : tunnels.entrySet()) {
			SshTunnel tunnel = entry.getKey();
			CloudAppDashElement app = entry.getValue();
			int port = tunnel.getLocalPort();
			if (port>0) {
				builder.add(ImmutableList.of(JmxSupport.getJmxUrl(port), app.getLiveHost()));
			}
		}
		return builder.build();
	}

	public void add(SshTunnel sshTunnel, CloudAppDashElement app) {
		sshTunnel.onDispose(this::handleTunnelClosed);
		tunnels.put(sshTunnel, app);
		jmxUrls.refresh();
		app.getJmxSshTunnelStatus().refresh();
	}

	private void handleTunnelClosed(Disposable disposed) {
		CloudAppDashElement owner = tunnels.remove(disposed);
		owner.getJmxSshTunnelStatus().refresh();
		jmxUrls.refresh();
	}

	/**
	 * LiveSet of pairs containing a url (left value) + corresponding cf app's host name (rigt value) in each pair.
	 */
	public ObservableSet<List<String>> getUrls() {
		return jmxUrls;
	}

}
