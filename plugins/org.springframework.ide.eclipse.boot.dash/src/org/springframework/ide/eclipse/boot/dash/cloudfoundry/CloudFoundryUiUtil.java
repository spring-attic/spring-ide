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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.ui.ICoreRunnable;

public class CloudFoundryUiUtil {

	public static void runForked(final ICoreRunnable coreRunner, IRunnableContext context) throws CoreException {
		try {
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						coreRunner.run(SubMonitor.convert(monitor));
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			context.run(true, false, runner);
		} catch (InvocationTargetException e) {
			IStatus status;
			if (e.getCause() instanceof CoreException) {
				status = ((CoreException) e.getCause()).getStatus();
			} else {
				status = BootDashActivator.createErrorStatus(e, "Failed to connect to Cloud Foundry target: ");
			}

			throw new CoreException(status);

		} catch (InterruptedException e) {
			BootDashActivator.log(e);
		}
	}

	public static OrgsAndSpaces getCloudSpaces(final CloudFoundryTargetProperties targetProperties,
			IRunnableContext context) throws CoreException {

		try {
			final OrgsAndSpaces[] spaces = new OrgsAndSpaces[1];
			ICoreRunnable coreRunner = new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {

					try {

						List<CloudSpace> actualSpaces = new CloudFoundryClient(
								new CloudCredentials(targetProperties.getUserName(), targetProperties.getPassword()),
								new URL(targetProperties.getUrl()), targetProperties.isSelfsigned()).getSpaces();
						if (actualSpaces != null && !actualSpaces.isEmpty()) {
							spaces[0] = new OrgsAndSpaces(actualSpaces);
						}
					} catch (MalformedURLException e) {
						throw new CoreException(BootDashActivator.createErrorStatus(e));

					} catch (Throwable e) {
						throw new CoreException(BootDashActivator.createErrorStatus(e,
								"Cloud Foundry client error: " + e.getMessage()));
					}
				}
			};
			runForked(coreRunner, context);

			return spaces[0];
		} catch (OperationCanceledException e) {
			// Ignore if cancelled
		}
		return null;

	}
}
