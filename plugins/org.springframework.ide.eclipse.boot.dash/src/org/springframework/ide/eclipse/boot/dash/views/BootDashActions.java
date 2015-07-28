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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;

public class BootDashActions {

	private final static String PROPERTIES_VIEW_ID = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

	///// context info //////////////
	private BootDashViewModel model;
	private MultiSelection<BootDashElement> selection;
	private UserInteractions ui;
	private BootDashModel sectionModel;

	///// actions ///////////////////

	private RunStateAction[] runStateActions;
	private AbstractBootDashAction openConsoleAction;
	private OpenLaunchConfigAction openConfigAction;
	private OpenInBrowserAction openBrowserAction;
	private AddRunTargetAction[] addTargetActions;
	private RefreshRunTargetAction refreshAction;
	private RemoveRunTargetAction removeTargetAction;
	private DeleteApplicationsAction deleteApplicationsAction;
	private UpdatePasswordAction updatePasswordAction;
	private ShowViewAction showPropertiesViewAction;
	private ToggleFiltersAction toggleFiltersAction;

	public BootDashActions(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		this.model = model;
		this.selection = selection;
		this.ui = ui;

		makeActions();
	}

	public BootDashActions(BootDashViewModel model, BootDashModel sectionModel,
			MultiSelection<BootDashElement> selection, UserInteractions ui) {
		this.model = model;
		this.selection = selection;
		this.ui = ui;
		this.sectionModel = sectionModel;
		makeActions();
	}

	protected void makeActions() {

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
				return s == RunState.DEBUGGING || s == RunState.RUNNING;
			}

			@Override
			protected Job createJob() {
				final Collection<BootDashElement> selecteds = selection.getValue();
				if (!selecteds.isEmpty()) {
					return new Job("Stopping " + selecteds.size() + " Boot Dash Elements") {
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("Stopping " + selecteds.size() + " Elements", selecteds.size());
							try {
								for (BootDashElement el : selecteds) {
									monitor.subTask("Stopping: " + el.getName());
									try {
										el.stopAsync(ui);
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

		runStateActions = new RunStateAction[] { restartAction, rebugAction, stopAction };

		openConfigAction = new OpenLaunchConfigAction(selection, ui);
		openConsoleAction = new OpenConsoleAction(selection, ui);
		openBrowserAction = new OpenInBrowserAction(model, selection, ui);
		addTargetActions = createAddTargetActions();

		if (sectionModel != null) {
			refreshAction = new RefreshRunTargetAction(sectionModel, selection, ui);
			if (sectionModel.getRunTarget().canRemove()) {
				removeTargetAction = new RemoveRunTargetAction(sectionModel.getRunTarget(), model, selection, ui);
			}
			deleteApplicationsAction = new DeleteApplicationsAction( sectionModel, selection, ui);

			if (sectionModel.getRunTarget() instanceof RunTargetWithProperties) {
				RunTargetWithProperties runTargetWP = (RunTargetWithProperties) sectionModel.getRunTarget();
				if (runTargetWP.requiresCredentials()) {
					updatePasswordAction = new UpdatePasswordAction(runTargetWP, model, sectionModel, selection, ui);
				}
			}
		}

		showPropertiesViewAction = new ShowViewAction(PROPERTIES_VIEW_ID);
		toggleFiltersAction = new ToggleFiltersAction(model.getToggleFilters(), selection, ui);
	}

	private AddRunTargetAction[] createAddTargetActions() {
		Set<RunTargetType> targetTypes = model.getRunTargetTypes();
		ArrayList<AddRunTargetAction> actions = new ArrayList<AddRunTargetAction>();
		for (RunTargetType tt : targetTypes) {
			if (tt.canInstantiate()) {
				actions.add(new AddRunTargetAction(tt, model.getRunTargets(), selection, ui));
			}
		}
		return actions.toArray(new AddRunTargetAction[actions.size()]);
	}

	static class RunOrDebugStateAction extends RunStateAction {

		public RunOrDebugStateAction(BootDashViewModel model, MultiSelection<BootDashElement> selection,
				UserInteractions ui, RunState goalState) {
			super(model, selection, ui, goalState);
			Assert.isLegal(goalState == RunState.RUNNING || goalState == RunState.DEBUGGING);
		}

		@Override
		protected Job createJob() {
			final Collection<BootDashElement> selecteds = getSelectedElements();
			if (!selecteds.isEmpty()) {
				return new Job("Restarting " + selecteds.size() + " Dash Elements") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Restart Boot Dash Elements", selecteds.size());
						try {
							for (BootDashElement el : selecteds) {
								monitor.subTask("Restarting: " + el.getName());
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

	public AbstractBootDashAction getOpenBrowserAction() {
		return openBrowserAction;
	}

	public AbstractBootDashAction getOpenConsoleAction() {
		return openConsoleAction;
	}

	public OpenLaunchConfigAction getOpenConfigAction() {
		return openConfigAction;
	}

	public AddRunTargetAction[] getAddRunTargetActions() {
		return addTargetActions;
	}

	public IAction getRemoveRunTargetAction() {
		return removeTargetAction;
	}

	/**
	 *
	 * @return May be null as it may not be supported on all models.
	 */
	public IAction getRefreshRunTargetAction() {
		return refreshAction;
	}

	/**
	 * @return May be null as it may not be supported on all models.
	 */
	public IAction getDeleteApplicationsAction() {
		return deleteApplicationsAction;
	}

	/**
	 *
	 * @return May be null as it may not be supported on all models.
	 */
	public IAction getUpdatePasswordAction() {
		return updatePasswordAction;
	}

	/**
	 *
	 * @return show properties view action instance
	 */
	public IAction getShowPropertiesViewAction() {
		return showPropertiesViewAction;
	}

	public void dispose() {
		if (runStateActions != null) {
			for (RunStateAction a : runStateActions) {
				a.dispose();
			}
			runStateActions = null;
		}
		if (openConsoleAction != null) {
			openConsoleAction.dispose();
		}
		if (openConfigAction != null) {
			openConfigAction.dispose();
		}
		if (openBrowserAction != null) {
			openBrowserAction.dispose();
		}
		if (addTargetActions != null) {
			for (AddRunTargetAction a : addTargetActions) {
				a.dispose();
			}
			addTargetActions = null;
		}
		if (toggleFiltersAction != null) {
			toggleFiltersAction.dispose();
			toggleFiltersAction = null;
		}
	}

	public IAction getToggleFiltersAction() {
		return toggleFiltersAction;
	}

}
