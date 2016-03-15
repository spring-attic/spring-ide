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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;

/**
 * A cloud operation that is performed on a Cloud application (for example,
 * creating, starting, or stopping an application)
 *
 */
public abstract class CloudApplicationOperation extends CloudOperation {

	protected String appName;
	private ISchedulingRule schedulingRule;

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName) {
		super(opName, model);
		this.appName = appName;
		setSchedulingRule(new StartApplicationSchedulingRule(model.getRunTarget(), appName));
	}

	protected CloudAppDashElement getDashElement() {
		return model.getApplication(appName);
	}

	protected CloudAppInstances getCachedApplicationInstances() {
		return model.getAppCache().getAppInstances(appName);
	}

	public ISchedulingRule getSchedulingRule() {
		return this.schedulingRule;
	}

	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}

	protected void resetAndShowConsole() {
		try {
			model.getElementConsoleManager().resetConsole(appName);
			model.getElementConsoleManager().showConsole(appName);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	protected void log(String message) {
		try {
			model.getElementConsoleManager().writeToConsole(appName, message, LogType.LOCALSTDOUT);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	protected void logAndUpdateMonitor(String message, IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.setTaskName(message);
		}
		try {
			model.getElementConsoleManager().writeToConsole(appName, message, LogType.LOCALSTDOUT);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	String getOpErrorPrefix() {
		return "Error: " + appName + " in '" + model.getRunTarget().getName() + "'";
	}

	public void checkTerminationRequested() throws OperationCanceledException {
		//TODO: Does nothing for now. But we need some mechanics to allow requesting an operation stops whatever it
		// is doing.
	}

}
