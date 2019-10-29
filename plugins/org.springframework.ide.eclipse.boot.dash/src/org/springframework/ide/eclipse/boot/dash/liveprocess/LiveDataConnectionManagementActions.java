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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor.Server;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicSubMenuSupplier;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;

public class LiveDataConnectionManagementActions implements DynamicSubMenuSupplier {

	private static final IAction DUMMY_ACTION = new Action("No matching processes") {
		{
			setEnabled(false);
		}
	};
	private final Params params;
	private LiveProcessCommandsExecutor liveProcessCmds;

	@Override
	public boolean isVisible() {
		return liveProcessCmds!=null && params.getSelection().getSingle() instanceof BootProjectDashElement;
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
	}

	@Override
	public List<IAction> get() {
		BootDashElement bde = params.getSelection().getSingle();
		try {
			if (bde!=null) {
				IProject project = bde.getProject();
				if (project!=null) {
					String projectName = project.getName();
					List<LiveProcessCommandsExecutor.Server> servers = liveProcessCmds.getLanguageServers();
					return Flux.fromIterable(servers)
					.flatMap((Server server) ->
						server.listCommands()
						.map(cmdInfo -> new ExecuteCommandAction(server, cmdInfo))
					)
					.filter(action -> projectName.equals(action.getProjectName()))
					.cast(IAction.class)
					.collect(Collectors.toList())
					.map(actions -> {
						if (actions.isEmpty()) {
							return ImmutableList.of(DUMMY_ACTION);
						}
						return actions;
					})
					.block();
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return ImmutableList.of(DUMMY_ACTION);
	}

}
