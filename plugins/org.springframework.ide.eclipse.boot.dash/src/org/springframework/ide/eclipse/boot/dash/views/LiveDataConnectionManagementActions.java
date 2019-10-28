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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicSubMenuSupplier;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("restriction")
public class LiveDataConnectionManagementActions implements DynamicSubMenuSupplier {

	private static final String CMD_LIST_PROCESSES = "sts/livedata/listProcesses";
	private static final IAction DUMMY_ACTION = new Action("No matching processes") {
		{
			setEnabled(false);
		}
	};
	private Params params;

	@Override
	public boolean isVisible() {
		return params.getSelection().getSingle() instanceof BootProjectDashElement;
	}

	class ExecuteCommandAction extends AbstractBootDashElementsAction {
		private String command;
		private String projectName;
		private String label;
		private LanguageServer server;
		private Map<String, String> commandInfo;

		@SuppressWarnings("unchecked")
		public ExecuteCommandAction(LanguageServer server, Object _commandInfo) {
			super(params);
			this.server = server;
			commandInfo = (Map<String, String>) _commandInfo;
			this.command = commandInfo.get("action");
			int lastSlash = command.lastIndexOf("/");
			String humanReadableCommand = command.substring(lastSlash+1);
			humanReadableCommand = humanReadableCommand.substring(0, 1).toUpperCase() + humanReadableCommand.substring(1);
			this.projectName = commandInfo.get("projectName");
			label = humanReadableCommand + " "+commandInfo.get("label");
			this.setText(label);
		}

		public String getProjectName() {
			return projectName;
		}

		@Override
		public String toString() {
			return "ExecuteCommandAction [command=" + command + ", projectName=" + projectName +", label="+label+"]";
		}

		@Override
		public void run() {
			try {
				server.getWorkspaceService().executeCommand(new ExecuteCommandParams(command, ImmutableList.of(commandInfo))).get(2, TimeUnit.SECONDS);
			} catch (Exception e) {
				Log.log(e);
			}
		}
	}

	public LiveDataConnectionManagementActions(Params params) {
		this.params = params;
	}

	@Override
	public List<IAction> get() {
		BootDashElement bde = params.getSelection().getSingle();
		try {
			if (bde!=null) {
				IProject project = bde.getProject();
				if (project!=null) {
					String projectName = project.getName();
					List<LanguageServer> servers = LanguageServiceAccessor.getActiveLanguageServers(cap -> {
						ExecuteCommandOptions commandCap = cap.getExecuteCommandProvider();
						if (commandCap!=null) {
							List<String> supportedCommands = commandCap.getCommands();
							return supportedCommands!=null && supportedCommands.contains(CMD_LIST_PROCESSES);
						}
						return false;
					});
					ExecuteCommandParams params = new ExecuteCommandParams(CMD_LIST_PROCESSES, Collections.emptyList());
					return Flux.fromIterable(servers)
					.flatMap((LanguageServer server) ->
						Mono.fromFuture(server.getWorkspaceService().executeCommand(params))
						.flatMapMany(commandList -> Flux.fromIterable((List<?>)commandList))
						.map(cmd -> new ExecuteCommandAction(server, cmd))
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
		return ImmutableList.of();
	}

}
