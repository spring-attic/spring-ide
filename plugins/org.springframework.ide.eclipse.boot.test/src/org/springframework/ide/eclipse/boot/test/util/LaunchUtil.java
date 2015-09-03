/*******************************************************************************
 * Copyright (c) 2012, 2014, 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * Utility class providing methods to help launching a process from a ILauncConfiguration and waiting for it to
 * terminate while capturing the output for testing purposes.
 *
 * @author Kris De Volder
 */
public class LaunchUtil {

	public static class LaunchResult {

		public final int terminationCode;
		public final String out;
		public final String err;

		private LaunchResult(int terminationCode, String out, String err) {
			super();
			this.terminationCode = terminationCode;
			this.out = out;
			this.err = err;
		}
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("---- Sys.out ---\n");
			buf.append(out);
			buf.append("---- Sys.err ---\n");
			buf.append(err);
			return buf.toString();
		}
	}

	public static LaunchResult synchLaunch(ILaunchConfiguration launchConf) throws CoreException {
		ILaunch l = launchConf.launch("run", new NullProgressMonitor(), false, true);
		IProcess process = findProcess(l);
		IStreamsProxy streams = process.getStreamsProxy();

		StringBuilder out = capture(streams.getOutputStreamMonitor());
		StringBuilder err = capture(streams.getErrorStreamMonitor());
		IProcess p = synchLaunch(l);
		return new LaunchResult(p.getExitValue(), out.toString(), err.toString());
	}

	private static StringBuilder capture(IStreamMonitor stream) {
		final StringBuilder out = new StringBuilder();
		synchronized (stream) {
			out.append(stream.getContents());
			stream.addListener(new IStreamListener() {
				public void streamAppended(String text, IStreamMonitor monitor) {
					out.append(text);
				}
			});
		}
		return out;
	}

	private static IProcess synchLaunch(ILaunch launch) {
		DebugPlugin mgr = DebugPlugin.getDefault();
		LaunchTerminationListener listener = null;
		try {
			//DebugUITools.launch(launchConf, "run");
			listener = new LaunchTerminationListener(launch);
			mgr.getLaunchManager().addLaunchListener(listener);
			return listener.waitForProcess();
		} finally {
			if (listener!=null) {
				mgr.getLaunchManager().removeLaunchListener(listener);
			}
		}
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static IProcess findProcess(ILaunch launch) {
		IProcess[] processes = launch.getProcesses();
		if (processes!=null && processes.length>0) {
			Assert.isTrue(processes.length==1);
			return processes[0];
		}
		return null;
	}

}
