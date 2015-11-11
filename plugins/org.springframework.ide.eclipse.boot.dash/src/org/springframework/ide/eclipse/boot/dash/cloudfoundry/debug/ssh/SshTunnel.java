/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.ide.eclipse.boot.dash.util.LogSink;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.ssh.SshHost;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * This class is responsible for creating an ssh tunnel to a remote port. This class implements
 * Closeable, its close method must be called to close the tunnel and avoid resource leak.
 */
public class SshTunnel implements Closeable {

	private Session session;
	private int localPort;

	public SshTunnel(SshHost sshHost, String user, String oneTimeCode, int remotePort, LogSink log) throws JSchException {
		JSch jsch = new JSch();
		session = jsch.getSession(user, sshHost.getHost(), sshHost.getPort());
		log.log("Ssh session created");

		session.setPassword(oneTimeCode);
		session.setUserInfo(getUserInfo(oneTimeCode));
		session.setServerAliveInterval(15_000); //15 seconds
		session.connect();
		log.log("Ssh client connected");

		localPort = session.setPortForwardingL(0, "localhost", remotePort); //$NON-NLS-1$
		log.log("Ssh tunnel created: localPort = "+localPort);
	}

	private UserInfo getUserInfo(final String accessToken) {
		return new UserInfo() {

			@Override
			public void showMessage(String arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean promptYesNo(String arg0) {
				return true;
			}

			@Override
			public boolean promptPassword(String arg0) {
				return true;
			}

			@Override
			public boolean promptPassphrase(String arg0) {
				return false;
			}

			@Override
			public String getPassword() {
				return accessToken;
			}

			@Override
			public String getPassphrase() {
				return null;
			}
		};
	}

	@Override
	synchronized public void close() throws IOException {
		if (session!=null) {
			session.disconnect();
			session = null;
		}
	}

	public int getLocalPort() {
		return localPort;
	}


}
