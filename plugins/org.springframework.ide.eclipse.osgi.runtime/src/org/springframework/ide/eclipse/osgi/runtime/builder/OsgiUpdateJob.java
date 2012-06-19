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
package org.springframework.ide.eclipse.osgi.runtime.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.osgi.runtime.OsgiPlugin;
import org.springframework.ide.eclipse.osgi.runtime.OsgiUiImages;
import org.springframework.ide.eclipse.osgi.runtime.builder.OsgiBundleUpdateBuilder.Command;


/**
 * Eclipse {@link Job} implementation that is scheduled to do an update or
 * refresh of the given bundle identified by its symbolic name.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0
 */
class OsgiUpdateJob extends Job {

	private static final String YES_COMMAND = "y";

	private static final String DISCONNECT_COMMAND = "disconnect";

	public static final Object MODEL_CONTENT_FAMILY = new Object();

	private String symbolicName;

	private Command command;

	/**
	 * Private Constructor.
	 * @param symbolicName the bundle identifier to update
	 * @param command the command to execute; either refresh or update
	 * @see #schedule(String, Command)
	 */
	private OsgiUpdateJob(String symbolicName, Command command) {
		super("Refreshing bundle with symbolic name '" + symbolicName + "'");
		this.symbolicName = symbolicName;
		this.command = command;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {

		// First make sure that only one Job at a time is scheduled that is
		// responsible for updating the same bundle
		synchronized (getClass()) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			Job[] buildJobs = Job.getJobManager().find(MODEL_CONTENT_FAMILY);
			for (int i = 0; i < buildJobs.length; i++) {
				Job curr = buildJobs[i];
				if (curr != this && curr instanceof OsgiUpdateJob) {
					OsgiUpdateJob job = (OsgiUpdateJob) curr;
					if (job.isCoveredBy(this)) {
						curr.cancel();
					}
				}
			}
		}

		// Get the port from the preferences store
		int port = getPort();

		monitor.beginTask("Connecting to OSGi runtime running at port " + port,
				1);

		// Open socket to console and emit command
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			Socket socket = new Socket(InetAddress.getLocalHost(), port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			emitCommnad(out, command.toString().toLowerCase() + " " + symbolicName);
			emitCommnad(out, DISCONNECT_COMMAND);
			emitCommnad(out, YES_COMMAND);
		}
		catch (Exception e) {
			// ignore all exceptions here
		}
		finally {
			if (out != null) {
				out.close();
			}
			if (in != null)
				try {
					{
						in.close();
					}

				}
				catch (IOException e) {
				}
		}
		return Status.OK_STATUS;
	}
	
	private void emitCommnad(PrintWriter writer, String command) throws Exception {
		writer.println(command);
		writer.flush();
		Thread.sleep(500);
	}

	/**
	 * Returns the port of the console defined in the preferences
	 * @return port
	 */
	private int getPort() {
		IScopeContext context = new InstanceScope();
		IEclipsePreferences preferences = context.getNode(OsgiPlugin.PLUGIN_ID);
		return Integer.valueOf(preferences.get(OsgiPlugin.PORT_PREFERENCE_KEY,
				OsgiPlugin.DEFAULT_PORT));
	}

	public boolean isCoveredBy(OsgiUpdateJob other) {
		return other.symbolicName.equals(symbolicName)
				&& other.command.equals(command);
	}

	public boolean belongsTo(Object family) {
		return MODEL_CONTENT_FAMILY == family;
	}

	/**
	 * Schedules a update or refresh job for the given bundle
	 * @param symbolicName the bundle identifier to update
	 * @param command the command to execute; either refresh or update
	 */
	public static void schedule(String symbolicName, Command command) {
		OsgiUpdateJob job = new OsgiUpdateJob(symbolicName, command);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setPriority(BUILD);
		job.setProperty(IProgressConstants.ICON_PROPERTY,
				OsgiUiImages.DESC_OBJS_OSGI);
		job.schedule();
	}

}
