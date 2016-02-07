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
package org.springframework.ide.eclipse.boot.launch.process;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifeCycleClientManager;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifecycleClient;
import org.springframework.ide.eclipse.boot.util.RetryUtil;

public class BootProcessFactory implements IProcessFactory {

	private static class DumpOutput implements IStreamListener {

		private final String label;
		private boolean first = true;

		public DumpOutput(String streamName) {
			this.label = streamName;
		}

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			//TODO: this might look messy on windows
			if (first) {
				System.out.print("\n"+label);
				first = false;
			}
			System.out.print(text.replaceAll("\n", "\n"+label));
		}

	}

	/**
	 * This flag enables dumping the process output onto System.out. This meant to help in
	 * diagnosing problems during CI build test execution.
	 */
	public static boolean ENABLE_OUTPUT_DUMPING = false;

	private static final boolean DEBUG = false;

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	@Override
	public IProcess newProcess(ILaunch launch, final Process process, final String label, Map<String, String> attributes) {

		final int jmxPort = getJMXPort(launch);
		final long timeout = getNiceTerminationTimeout(launch);
		RuntimeProcess rtProcess = new RuntimeProcess(launch, process, label, attributes) {

			SpringApplicationLifeCycleClientManager clientMgr = new SpringApplicationLifeCycleClientManager(jmxPort);

			@Override
			public void terminate() throws DebugException {
				if (!terminateNicely()) {
					//Let eclipse try it more aggressively.
					try {
						super.terminate();
					} catch (DebugException e) {
						//Try even harder to destroy the process
						if (!destroyForcibly()) {
							throw e;
						}
					}
				}
			}

			@Override
			protected IStreamsProxy createStreamsProxy() {
				IStreamsProxy streams = super.createStreamsProxy();
				if (ENABLE_OUTPUT_DUMPING) {
					streams.getOutputStreamMonitor().addListener(new DumpOutput("%out: "));
					streams.getErrorStreamMonitor().addListener(new DumpOutput("%err: "));
				}
				return streams;
			}

			private boolean destroyForcibly() {
				try {
					Method m = Process.class.getDeclaredMethod("destroyForcibly");
					m.invoke(process);
					return true;
				} catch (Exception e) {
					BootActivator.log(e);
					//ignore... probably means we are not running on Java 8 VM and so we can't use 'destroyForcibly'.
				}
				return false;
			}

			private boolean terminateNicely() {
				if (isTerminated()) {
					return true;
				}
				if (jmxPort>0) {
					try {
						debug("Trying to terminate nicely: "+label);
						SpringApplicationLifecycleClient client = clientMgr.getLifeCycleClient();
						if (client!=null) {
							debug("Asking JMX client to 'stop'");
							client.stop();
							debug("Asking JMX client to 'stop' -> SUCCESS");
						} else {
							//This case happens if process is already being terminated in another way.
							// This happens for debug processes where debug target kills vm before
							// asking process to terminate.
							debug("PROBLEM? Couldn't get JMX client");
							throw new IOException("Couldn't get JMX client.");
						}
						//Wait for a bit, but not forever until process dies.
						RetryUtil.retry(100, timeout, new Callable<Void>() {
							public Void call() throws Exception {
								debug("process exited?");
								process.exitValue(); //throws if process not ready
								debug("process exited? -> YES");
								return null;
							}
						});
						debug("SUCCESS terminate nicely: "+label);
						return true;
					} catch (Exception e) {
						//ignore... nice termination failed.
						//BootActivator.log(e);
					} finally {
						clientMgr.disposeClient();
					}
				}
				return false;
			}

		};

		return rtProcess;
	}

	private long getNiceTerminationTimeout(ILaunch launch) {
		return BootLaunchConfigurationDelegate.getTerminationTimeoutAsLong(launch);
	}

	private int getJMXPort(ILaunch launch) {
		ILaunchConfiguration conf = launch.getLaunchConfiguration();
		if (conf!=null && BootLaunchConfigurationDelegate.canUseLifeCycle(conf)) {
			return BootLaunchConfigurationDelegate.getJMXPortAsInt(conf);
		}
		return -1;
	}

}
