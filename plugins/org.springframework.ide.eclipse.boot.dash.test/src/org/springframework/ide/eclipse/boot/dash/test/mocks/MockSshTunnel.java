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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnel;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;

public class MockSshTunnel extends AbstractDisposable implements SshTunnel {

	boolean disposed = false;
	private SshHost sshHost;
	private String user;
	private String oneTimeCode;
	private int remotePort;
	private LogSink log;
	private int localPort;

	public MockSshTunnel(
			SshHost sshHost,
			String user,
			String oneTimeCode,
			int remotePort,
			LogSink log,
			int localPort
	) {
		this.sshHost = sshHost;
		this.user = user;
		this.oneTimeCode = oneTimeCode;
		this.remotePort = remotePort;
		this.log = log;
		this.localPort = localPort;
	}

	public boolean isDisposed() {
		return disposed;
	}

	public SshHost getSshHost() {
		return sshHost;
	}

	public String getUser() {
		return user;
	}

	public String getOneTimeCode() {
		return oneTimeCode;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public LogSink getLog() {
		return log;
	}

	public int getLocalPort() {
		return localPort;
	}

}