/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.JmxSupport;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * An instance of this class keeps track of open ssh tunnels for JMX connections to
 * remote hosts. It is capable of producing a (Live)Set of JMX urls for all the
 * open tunnels.
 */
public class JmxSshTunnelManager {

	private Set<SshTunnel> tunnels = new HashSet<>();

	private ObservableSet<String> jmxUrls = ObservableSet.<String>builder()
			.refresh(AsyncMode.ASYNC)
			.compute(this::collectUrls)
			.build();

	private synchronized ImmutableSet<String> collectUrls() {
		Builder<String> builder = ImmutableSet.builder();
		for (SshTunnel tunnel : tunnels) {
			int port = tunnel.getLocalPort();
			if (port>0) {
				builder.add(JmxSupport.getJmxUrl(port));
			}
		}
		return builder.build();
	}

	public void add(SshTunnel sshTunnel) {
		sshTunnel.onDispose(this::handleTunnelClosed);
		tunnels.add(sshTunnel);
		jmxUrls.refresh();
	}

	private void handleTunnelClosed(Disposable disposed) {
		tunnels.remove(disposed);
		jmxUrls.refresh();
	}

	public ObservableSet<String> getUrls() {
		return jmxUrls;
	}

}
