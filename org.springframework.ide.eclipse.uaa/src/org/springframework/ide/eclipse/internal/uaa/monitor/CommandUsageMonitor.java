/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/ 
package org.springframework.ide.eclipse.internal.uaa.monitor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.springframework.ide.eclipse.internal.uaa.UaaPlugin;

/**
 * Helper class that captures executions of Eclipse commands.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class CommandUsageMonitor {

	private static final String COMMANDS_EXTENSION_POINT = "org.eclipse.ui.commands"; //$NON-NLS-1$

	private ExtensionIdToBundleMapper commandToBundleIdMapper;

	private IExecutionListener executionListener;

	public void startMonitoring() {
		commandToBundleIdMapper = new ExtensionIdToBundleMapper(COMMANDS_EXTENSION_POINT);
		executionListener = new IExecutionListener() {
			public void notHandled(String commandId, NotHandledException exception) {
			}

			public void postExecuteFailure(String commandId, ExecutionException exception) {
			}

			public void postExecuteSuccess(String commandId, Object returnValue) {
				recordEvent(commandId);
			}

			public void preExecute(String commandId, ExecutionEvent event) {

			}
		};
		getCommandService().addExecutionListener(executionListener);
	}

	public void stopMonitoring() {
		ICommandService commandService = getCommandService();
		if (commandService != null) {
			commandService.removeExecutionListener(executionListener);
		}
		if (commandToBundleIdMapper != null) {
			commandToBundleIdMapper.dispose();
		}
	}

	private ICommandService getCommandService() {
		return (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
	}

	private void recordEvent(String commandId) {
		UaaPlugin.getDefault().registerFeatureUse(commandToBundleIdMapper.getBundleId(commandId));
	}

}
