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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * This class is responsible for creating an ssh tunnel to a remote port. This class implements
 * Closeable, its close method must be called to close the tunnel and avoid resource leak.
 */
public class SshTunnel implements Closeable {

	private int localPort;
	private SSHClient ssh;
	private LocalPortForwarder portForwarder;

	public SshTunnel(SshHost sshHost, String user, String oneTimeCode, int remotePort, LogSink log) throws Exception {
		DefaultConfig config = new DefaultConfig();
		ssh = new SSHClient(config);
		ssh.addHostKeyVerifier(new PromiscuousVerifier()); //TODO: use fingerprint verifier
		log.log("Ssh client created");

		ssh.connect(sshHost.getHost(), sshHost.getPort());
		ssh.authPassword(user, oneTimeCode);
		ssh.getConnection().getKeepAlive().setKeepAliveInterval(15);
		log.log("Ssh client connected");

		ServerSocket ss = new ServerSocket(0);
		ss.setSoTimeout(5_000);
		localPort = PortFinder.findFreePort();
		localPort = ss.getLocalPort();
		Job job = new Job("SshTunnel port forwarding") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				final LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters("0.0.0.0", localPort, "localhost", remotePort);
				try {
					portForwarder = ssh.newLocalPortForwarder(params, ss);
					boolean timeout;
					do {
						timeout = false;
						try {
							portForwarder.listen();
						} catch (SocketTimeoutException e) {
							timeout = true;
						}
					} while (timeout);
				} catch (IOException e) {
					Log.log(e);
				} finally {
					try {
						ss.close();
					} catch (IOException e) {
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		log.log("Ssh tunnel created: localPort = "+localPort);
	}

	@Override
	synchronized public void close() throws IOException {
		if (portForwarder!=null) {
			try {
				portForwarder.close();
			} catch (Exception e) {
			}
			portForwarder = null;
		}
		if (ssh!=null) {
			try {
				ssh.disconnect();
			} catch (Exception e) {
			}
			ssh = null;
		}
	}

	public int getLocalPort() {
		return localPort;
	}


}
