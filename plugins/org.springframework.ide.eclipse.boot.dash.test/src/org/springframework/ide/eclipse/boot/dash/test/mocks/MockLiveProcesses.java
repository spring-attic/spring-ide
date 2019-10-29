/*******************************************************************************
 * Copyright (c) 2015-2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.junit.Assert;
import org.springframework.ide.eclipse.boot.dash.liveprocess.CommandInfo;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor.Server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MockLiveProcesses {

	public static final String COMMAND_CONNECT = "sts/livedata/connect";
	public static final String COMMAND_REFRESH = "sts/livedata/refresh";
	public static final String COMMAND_DISCONNECT = "sts/livedata/disconnect";

	private final Map<String, MockProcess> processes = new LinkedHashMap<>();

	public class MockProcess {
		final private String processKey;
		private String projectName;
		private int refreshCount = 0;
		private boolean connected = false;

		private MockProcess(String processKey, String projectName) {
			this.processKey = processKey;
			this.projectName = projectName;
			Assert.assertFalse("duplicate process key", processes.containsKey(processKey));
			processes.put(processKey, this);
		}

		public int getRefreshCount() {
			return refreshCount;
		}

		public boolean isConnected() {
			return connected;
		}

		public void connect() {
			connected = true;
			refreshCount = 0;
		}

		public void refresh() {
			refreshCount++;
		}

		public void disconnect() {
			connected = false;
		}

		public void assertRefreshed(int expectedCount) {
			assertEquals(expectedCount, refreshCount);
		}

		public String getProcessKey() {
			return processKey;
		}

		public String getProjectName() {
			return projectName;
		}
	}


	private final Server server = new Server() {

		@Override
		public Flux<CommandInfo> listCommands() {
			return Flux.fromIterable(processes.values())
			.flatMapIterable(this::createCommands);
		}

		private List<CommandInfo> createCommands(MockProcess process) {
			if (process.isConnected()) {
				return ImmutableList.of(
						command(COMMAND_DISCONNECT, process),
						command(COMMAND_REFRESH, process)
				);
			} else {
				return ImmutableList.of(command(COMMAND_CONNECT, process));
			}
		}

		private CommandInfo command(String cmdId, MockProcess process) {
			ImmutableMap.Builder<String, String> info = ImmutableMap.builder();
			info.put("processKey", process.getProcessKey());
			info.put("action", cmdId);
			info.put("label", process.getProcessKey()+" lbl");
			if (process.getProjectName()!=null) {
				info.put("projectName", process.getProjectName());
			}
			return new CommandInfo(cmdId, info.build());
		}

		@Override
		public Mono<Void> executeCommand(CommandInfo cmd) {
			return Mono.fromRunnable(() -> {
				MockProcess process = processes.get(cmd.info.get("processKey"));
				switch (cmd.command) {
				case COMMAND_DISCONNECT:
					process.disconnect();
					break;
				case COMMAND_CONNECT:
					process.connect();
					break;
				case COMMAND_REFRESH:
					process.refresh();
					break;
				default:
					break;
				}
			});
		}
	};

	public LiveProcessCommandsExecutor commandExecutor = new LiveProcessCommandsExecutor() {
		@Override
		public List<Server> getLanguageServers() {
			return ImmutableList.of(server);
		}
	};

	public MockProcess newProcess(String processKey, IProject project) {
		return new MockProcess(processKey, project==null?null:project.getName());
	}

}
