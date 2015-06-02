/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import static org.eclipse.debug.core.DebugEvent.CREATE;
import static org.eclipse.debug.core.DebugEvent.TERMINATE;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * @author Kris De Volder
 */
public class ProcessTracker {

	public interface ProcessListener {
		void processTerminated(IProcess process);
		void processCreated(IProcess process);
	}

	private IDebugEventSetListener debugListener;
	private ProcessListener listener;
	private String stacktrace;

	protected ProcessTracker(ProcessListener listener) {
		this.stacktrace = ExceptionUtil.stacktrace();
		this.listener = listener;
		//Pick up any processes already running
		DebugPlugin.getDefault().addDebugEventListener(debugListener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				if (events!=null) {
					for (DebugEvent debugEvent : events) {
						handleDebugEvent(debugEvent);
					}
				}
			};
		});

		//What if processes got started before we attached the listener?
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (launches!=null) {
			for (ILaunch launch : launches) {
				for (IProcess process : launch.getProcesses()) {
					processCreated(process);
				}
			}
		}
	}

	protected final void handleDebugEvent(DebugEvent debugEvent) {
		int kind = debugEvent.getKind();
		switch (kind) {
		case CREATE:
			if (debugEvent.getSource() instanceof IProcess) {
				//We can only track one process. Ignore additional events.
				processCreated((IProcess)debugEvent.getSource());
			}
			break;
		case TERMINATE:
			if (debugEvent.getSource() instanceof IProcess) {
				processTerminated((IProcess)debugEvent.getSource());
			}
			break;
		default:
			break;
		}
	}

	private void processCreated(IProcess process) {
		listener.processCreated(process);
	}

	private void processTerminated(IProcess process) {
		listener.processTerminated(process);
	}

	/**
	 * Call to free up or deregister stuff if we don't need it any more. (e.g disconnect debug event listeners)
	 */
	public void dispose() {
		if (debugListener!=null) {
			DebugPlugin.getDefault().removeDebugEventListener(debugListener);
			debugListener = null;
			listener = null;
		}
	}

}
