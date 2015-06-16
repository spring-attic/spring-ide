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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class BootDashActions {

	/////  context info //////////////
	private BootDashModel model;
	private MultiSelection<BootDashElement> selection;
	private UserInteractions ui;

	///// actions ///////////////////

	private RunStateAction[] runStateActions;
	private AbstractBootDashAction openConsoleAction;
	private OpenLaunchConfigAction openConfigAction;

	public BootDashActions(BootDashModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		this.model = model;
		this.selection = selection;
		this.ui = ui;

		makeActions();
	}

	protected void makeActions() {
//		refreshAction = new Action() {
//			public void run() {
//				model.refresh();
//				tv.refresh();
//			}
//		};
//		refreshAction.setText("Refresh");
//		refreshAction.setToolTipText("Manually trigger a view refresh");
//		refreshAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/refresh.gif"));

		RunStateAction restartAction = new RunOrDebugStateAction(model, selection, ui, RunState.RUNNING);
		restartAction.setText("(Re)start");
		restartAction.setToolTipText("Start or restart the process associated with the selected elements");
		restartAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/restart.gif"));
		restartAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/restart_disabled.gif"));

		RunStateAction rebugAction = new RunOrDebugStateAction(model, selection, ui, RunState.DEBUGGING);
		rebugAction.setText("(Re)debug");
		rebugAction.setToolTipText("Start or restart the process associated with the selected elements in debug mode");
		rebugAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/rebug.png"));
		rebugAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/rebug_disabled.png"));

		RunStateAction stopAction = new RunStateAction(model, selection, ui, RunState.INACTIVE) {
			@Override
			protected boolean currentStateAcceptable(RunState s) {
				return s==RunState.DEBUGGING || s==RunState.RUNNING;
			}
			@Override
			protected Job createJob() {
				final Collection<BootDashElement> selecteds = selection.getValue();
				if (!selecteds.isEmpty()) {
					return new Job("Stopping "+selecteds.size()+" Boot Dash Elements") {
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("Stopping "+selecteds.size()+" Elements", selecteds.size());
							try {
								for (BootDashElement el : selecteds) {
									monitor.subTask("Stopping: "+el.getName());
									try {
										el.stopAsync();
									} catch (Exception e) {
										return BootActivator.createErrorStatus(e);
									}
									monitor.worked(1);
								}
								return Status.OK_STATUS;
							} finally {
								monitor.done();
							}
						}
					};
				}
				return null;
			}
		};
		stopAction.setText("Stop");
		stopAction.setToolTipText("Stop the process(es) associated with the selected elements");
		stopAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/stop.gif"));
		stopAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/stop_disabled.gif"));

		runStateActions = new RunStateAction[] {
			restartAction, rebugAction, stopAction
		};

		openConfigAction = new OpenLaunchConfigAction(selection, ui);
		openConsoleAction = new OpenConsoleAction(selection, ui);
	}

	static class RunOrDebugStateAction extends RunStateAction {

		public RunOrDebugStateAction(
				BootDashModel model,
				MultiSelection<BootDashElement> selection,
				UserInteractions ui,
				RunState goalState) {
			super(model, selection, ui, goalState);
			Assert.isLegal(goalState==RunState.RUNNING || goalState==RunState.DEBUGGING);
		}

		@Override
		protected Job createJob() {
			final Collection<BootDashElement> selecteds = getSelectedElements();
			if (!selecteds.isEmpty()) {
				return new Job("Restarting "+selecteds.size()+" Dash Elements") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Restart Boot Dash Elements", selecteds.size());
						try {
							for (BootDashElement el : selecteds) {
								monitor.subTask("Restarting: "+el.getName());
								try {
									el.restart(goalState, ui);
								} catch (Exception e) {
									return BootActivator.createErrorStatus(e);
								}
								monitor.worked(1);
							}
							return Status.OK_STATUS;
						} finally {
							monitor.done();
						}
					}

				};
			}
			return null;
		}
	}

	public RunStateAction[] getRunStateActions() {
		return runStateActions;
	}

	public AbstractBootDashAction getOpenConsoleAction() {
		return openConsoleAction;
	}

	public OpenLaunchConfigAction getOpenConfigAction() {
		return openConfigAction;
	}

	public void dispose() {
		if (runStateActions!=null) {
			for (RunStateAction a : runStateActions) {
				a.dispose();
			}
			runStateActions = null;
		}
		if (openConsoleAction!=null) {
			openConsoleAction.dispose();
		}
		if (openConfigAction!=null) {
			openConfigAction.dispose();
		}
	}

}
