/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 ***********************************************************************************/
package org.springframework.ide.eclipse.boot.launch.devtools;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubContributionManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.ui.BootUIImages;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class DevtoolsClientConsolePageParticipant implements IConsolePageParticipant {

	private class TerminateClientAction extends Action {
		@Override
		public void run() {
			try {
				IProcess process = console.getProcess();
				if (process!=null && process.canTerminate() && isDevtoolsClient(process)) {
					process.terminate();
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}
	}

	private ProcessConsole console;
	private ProcessTracker processTracker;
	private TerminateClientAction terminateAction;

	public void activated() {
		// ignore
	}

	public void deactivated() {
		// ignore
	}

	public void dispose() {
		if (processTracker!=null) {
			processTracker.dispose();
			processTracker = null;
		}
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public void init(IPageBookViewPage page, IConsole console) {
		this.console = (ProcessConsole)console;

		//TODO: This code works assuming that our IConsolePageParticipant is called after the
		//  ProcessConsolePageParticipant (which creates the action we are replacing
		//When testing this that was always the case... but it may not be guaranteed.

		if (isInteresting(this.console)) {
			terminateAction = new TerminateClientAction();
			try {
				terminateAction.setImageDescriptor(BootUIImages.descriptor("icons/stop.gif"));
				terminateAction.setDisabledImageDescriptor(BootUIImages.descriptor("icons/stop_disabled.gif"));
			} catch (Exception e) {
				BootActivator.log(e);
			}
			IToolBarManager toolbar = page.getSite().getActionBars().getToolBarManager();
			IContributionItem replace = findReplacementItem(toolbar);
			if (replace!=null) {
		 		toolbar.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminateAction);
		 		toolbar.remove(replace);
			}
			boolean enabled = getConsoleProcess().canTerminate();
			terminateAction.setEnabled(enabled);
			if (enabled) {
				this.processTracker = new ProcessTracker(new ProcessListenerAdapter() {
					@Override
					public void processTerminated(ProcessTracker tracker, IProcess terminated) {
						if (getConsoleProcess().equals(terminated)) {
							terminateAction.setEnabled(false);
							//after process is terminated... it can't come back to life so... can stop listening now.
							tracker.dispose();
						}
					}
				});
			}
		}
	}

	private IProcess getConsoleProcess() {
		if (console!=null) {
			return console.getProcess();
		}
		return null;
	}

	private boolean isInteresting(ProcessConsole console) {
		IProcess process = console.getProcess();
		return isDevtoolsClient(process);
	}

	private IContributionItem findReplacementItem(IToolBarManager toolbar) {
		SubContributionManager contributions = (SubContributionManager) toolbar;
		for (IContributionItem item : contributions.getItems()) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) item;
				IAction replaceAction = actionItem.getAction();
				if (replaceAction.getClass().getName().equals("org.eclipse.debug.internal.ui.views.console.ConsoleTerminateAction")) {
					return item;
				}
			}
		}
		return null;
	}

	private static boolean isDevtoolsClient(IProcess process) {
		try {
			if (process!=null) {
				ILaunch launch = process.getLaunch();
				if (launch!=null) {
					ILaunchConfiguration conf = launch.getLaunchConfiguration();
					if (conf!=null) {
						return BootDevtoolsClientLaunchConfigurationDelegate.TYPE_ID.equals(conf.getType().getIdentifier());
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return false;
	}


}

