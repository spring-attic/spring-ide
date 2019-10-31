/*******************************************************************************
 * Copyright (c) 2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.liveprocess;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor.Server;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicSubMenuSupplier;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import reactor.core.publisher.Flux;

public class LiveDataConnectionManagementActions extends AbstractDisposable implements DynamicSubMenuSupplier {

	private static final IAction DUMMY_ACTION = new Action("No matching processes") {
		{
			setEnabled(false);
		}
	};
	private final Params params;
	private LiveProcessCommandsExecutor liveProcessCmds;
	private final LiveExpression<Boolean> isEnabled;

	@Override
	public boolean isVisible() {
		if (liveProcessCmds!=null) {
			Set<BootDashElement> selection = params.getSelection().getValue();
			return selection.isEmpty() || selection.stream().anyMatch(x -> x instanceof BootProjectDashElement);
		}
		return false;
	}

	class ExecuteCommandAction extends AbstractBootDashElementsAction {
		private String projectName;
		private String label;
		private Server server;
		private CommandInfo commandInfo;

		public ExecuteCommandAction(Server server, CommandInfo commandInfo) {
			super(params);
			this.server = server;
			this.commandInfo = commandInfo;
			String command = commandInfo.command;
			int lastSlash = command.lastIndexOf("/");
			String humanReadableCommand = command.substring(lastSlash+1);
			humanReadableCommand = humanReadableCommand.substring(0, 1).toUpperCase() + humanReadableCommand.substring(1);
			this.projectName = commandInfo.info.get("projectName");
			label = humanReadableCommand + " "+commandInfo.info.get("label");
			this.setText(label);
		}

		public String getProjectName() {
			return projectName;
		}

		@Override
		public String toString() {
			return "ExecuteCommandAction [projectName=" + projectName +", label="+label+"]";
		}

		@Override
		public void run() {
			try {
				server.executeCommand(commandInfo).block(Duration.ofSeconds(2));
			} catch (Exception e) {
				Log.log(e);
			}
		}
	}

	public LiveDataConnectionManagementActions(Params params) {
		this.params = params;
		this.liveProcessCmds = params.getLiveProcessCmds();
		ObservableSet<BootDashElement> selection = params.getSelection().getElements();
		this.isEnabled = addDisposableChild(new LiveExpression<Boolean>(false) {

			ElementStateListener elementStateListener = (BootDashElement e) -> {
				refresh();
			};

			{
				dependsOn(selection);
				params.getModel().addElementStateListener(elementStateListener);
			}

			@Override
			protected Boolean compute() {
				ImmutableSet<BootDashElement> els = selection.getValues();
				if (els.isEmpty()) {
					return true;
				} else {
					for (BootDashElement bde : els) {
						if (bde instanceof BootProjectDashElement) {
							RunState s = bde.getRunState();
							if (s == RunState.RUNNING || s ==RunState.DEBUGGING) {
								return true;
							}
						}
					}
					return false;
				}
			}
		});
	}

	@Override
	public List<IAction> get() {
		Set<BootDashElement> bdes = params.getSelection().getValue();
		Predicate<ExecuteCommandAction> filter;
		if (bdes.isEmpty()) {
			filter = x -> true;
		} else {
			Set<String> projectNames = new HashSet<>(bdes.size());
			for (BootDashElement bde : bdes) {
				IProject project = bde.getProject();
				if (project!=null) {
					projectNames.add(project.getName());
				}
			}
			filter = action -> projectNames.contains(action.getProjectName());
		}
		try {
			List<LiveProcessCommandsExecutor.Server> servers = liveProcessCmds.getLanguageServers();
			return Flux.fromIterable(servers)
			.flatMap((Server server) ->
				server.listCommands()
				.map(cmdInfo -> new ExecuteCommandAction(server, cmdInfo))
			)
			.filter(filter)
			.cast(IAction.class)
			.collect(Collectors.toList())
			.map(actions -> {
				if (actions.isEmpty()) {
					return ImmutableList.of(DUMMY_ACTION);
				}
				return actions;
			})
			.block();
		} catch (Exception e) {
			Log.log(e);
		}
		return ImmutableList.of(DUMMY_ACTION);
	}

	@Override
	public LiveExpression<Boolean> isEnabled() {
		return isEnabled;
	}

}
