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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
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
	protected ApplicationOperationEventHandler eventHandler;
	protected ApplicationOperationEventFactory eventFactory;

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName) {
		this(opName, model, appName, new StartingOperationHandler(model));
	}

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			ApplicationOperationEventHandler eventHandler) {
		super(opName, model);
		this.eventHandler = eventHandler;
		this.eventFactory = new ApplicationOperationEventFactory(model);
		this.appName = appName;
		setSchedulingRule(new StartApplicationSchedulingRule(model.getRunTarget(), appName));
	}

	protected CloudDashElement getDashElement() {
		return model.getElement(appName);
	}

	protected CloudAppInstances getCachedApplicationInstances() {
		return model.getAppCache().getAppInstances(appName);
	}

	public void addOperationEventHandler(ApplicationOperationEventHandler eventHandler) {
		if (eventHandler != null) {
			this.eventHandler = eventHandler;
		}
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
}
