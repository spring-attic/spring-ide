/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import javax.management.remote.JMXConnector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * An instance of this class starts checking a spring application's lifecyle using
 * a JMX bean protocol. Checks are performed repeatedly with a short delay between
 * polls. This continues until either the  SpringApplicationReadyStateMonitor is disposed,
 * or the application enters the 'ready' state.
 * <p>
 * When the application reaches ready state then its 'ready' LiveExp will change value from
 * false to true. Clients who which to respond to this 'event' can attach a listener to
 * the livexp.
 *
 * @author Kris De Volder
 */
public class SpringApplicationReadyStateMonitor implements ReadyStateMonitor {

	//////////////////////////////////////////////////////////////////////////
	// public API

	public static final long POLLING_INTERVAL = 500/*ms*/;

	public SpringApplicationReadyStateMonitor(int jmxPort) {
		this.jmxPort = jmxPort;
		this.job = new Job("Ready state poller") {
			protected IStatus run(IProgressMonitor monitor) {
				LiveVariable<Boolean> r = ready;
				if (r!=null) { //null means disposed. Job may be lagging behind
					r.setValue(checkReady());
					if (!r.getValue()) {
						this.schedule(POLLING_INTERVAL);
					} else {
						// don't reschedule
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	public LiveVariable<Boolean> getReady() {
		return ready;
	}

	public void dispose() {
		disposeClient();
		if (job!=null) {
			job.cancel();
			job = null;
		}
		ready = null;
	}

	/////////////////////////////////////////////////////////////////////////
	// implementation

	private int jmxPort;
	private JMXConnector connector;
	private SpringApplicationLifecycleClient client;

	private Job job;
	private LiveVariable<Boolean> ready = new LiveVariable<Boolean>(false);


	private boolean checkReady() {
		try {
			SpringApplicationLifecycleClient client = getLifeCycleClient();
			if (client!=null) {
				return client.isReady();
			}
		} catch (Exception e) {
			//Something went wrong asking clinet for ready state.
			// most likely process died.
			disposeClient();
		}
		return false;
	}

	private synchronized void disposeClient() {
		try {
			if (connector!=null) {
				connector.close();
			}
		} catch (Exception e) {
			//ignore
		}
		client = null;
		connector = null;
	}

	private synchronized SpringApplicationLifecycleClient getLifeCycleClient() {
		try {
			if (client==null) {
				connector = SpringApplicationLifecycleClient.createLocalJmxConnector(jmxPort);
				client = new SpringApplicationLifecycleClient(
						connector.getMBeanServerConnection(),
						SpringApplicationLifecycleClient.DEFAULT_OBJECT_NAME
				);
			}
			return client;
		} catch (Exception e) {
			//Someting went wrong creating client (most likely process we are trying to connect
			// doesn't exist yet or has been terminated.
			disposeClient();
		}
		return null;
	}
}
