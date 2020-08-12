/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.LogsParam;
import org.mandas.docker.client.DockerClient.RemoveContainerParam;
import org.mandas.docker.client.LogStream;
import org.mandas.docker.client.exceptions.ContainerNotFoundException;
import org.mandas.docker.client.messages.Container;
import org.springframework.ide.eclipse.boot.dash.api.ActualInstanceCount;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.DebuggableApp;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.DevtoolsConnectable;
import org.springframework.ide.eclipse.boot.dash.api.JmxConnectable;
import org.springframework.ide.eclipse.boot.dash.api.LogConnection;
import org.springframework.ide.eclipse.boot.dash.api.LogProducer;
import org.springframework.ide.eclipse.boot.dash.api.PortConnectable;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateIconProvider;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;
import static org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType.PLUGIN_ID;

public class DockerContainer implements App, RunStateProvider, JmxConnectable, Styleable, PortConnectable, 
	Deletable, ActualInstanceCount, DebuggableApp, ProjectRelatable, DevtoolsConnectable, LogProducer, RunStateIconProvider
{

	private static final Duration WAIT_BEFORE_KILLING = Duration.ofSeconds(10);
	private static final boolean DEBUG = true;
	private final Container container;
	private final DockerRunTarget target;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();
	
	private static Map<RunState, ImageDescriptor> RUNSTATE_ICONS = null;

	public DockerContainer(DockerRunTarget target, Container container) {
		this.target = target;
		this.container = container;
	}

	@Override
	public String getName() {
		return container.id();
	}

	@Override
	public RunState fetchRunState() {
		String state = container.state();
		if ("running".equals(state)) {
			return (container.labels().get(DockerApp.DEBUG_PORT)!=null) 
					? RunState.DEBUGGING
					: RunState.RUNNING;
		} else if ("exited".equals(state)) {
			return RunState.INACTIVE;
		}
		return RunState.UNKNOWN;
	}

	@Override
	public DockerRunTarget getTarget() {
		return this.target;
	}

	@Override
	public String getJmxUrl() {
		try {
			String port = container.labels().get(DockerApp.JMX_PORT);
			if (port!=null) {
				return new JmxSupport(Integer.valueOf(port)).getJmxUrl();
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "DockerContainer("+container.id()+")";
	}

	@Override
	public StyledString getStyledName(Stylers stylers) {
		StyledString styledString = new StyledString();
		if (container.names() != null && !container.names().isEmpty()) {
			styledString = styledString.append(StringUtil.removePrefix(container.names().get(0), "/")).append(" ");
		}
		styledString = styledString.append(getShortHash(), stylers==null?null:stylers.italicColoured(SWT.COLOR_DARK_GRAY));
		return styledString;
	}

	private String getShortHash() {
		String id = container.id();
		if (id.length() > 12) {
			id = id.substring(0, 12);
		}		
		return id;
	}

	@Override
	public Set<Integer> getPorts() {
		ImmutableSet.Builder<Integer> livePorts = ImmutableSet.builder();
		String portVal = container.labels().get(DockerApp.APP_LOCAL_PORT);
		if (portVal != null) {
			livePorts.add(Integer.parseInt(portVal));
		}
		return livePorts.build();
	}
	
	@Override
	public EnumSet<RunState> supportedGoalStates() {
		if (container.labels().get(DockerApp.DEBUG_PORT)!=null) {
			return EnumSet.of(RunState.INACTIVE, RunState.DEBUGGING);
		} else {
			return EnumSet.of(RunState.INACTIVE, RunState.RUNNING);
		}
	}
	
	@Override
	public void setGoalState(RunState goal) {
		RunState runState = fetchRunState();
		if (runState != goal) {

			DockerRunTarget dockerTarget = getTarget();
			DockerClient client = dockerTarget.getClient();
			if (client != null) {
				try {
					RefreshStateTracker rt = this.refreshTracker.get();
					
					if (goal == RunState.RUNNING) {
						rt.run("Starting " + getShortHash(), () -> {
							client.startContainer(container.id());
							RetryUtil.until(100, 1000, runstate -> runstate.equals(RunState.RUNNING), this::fetchRunState);
						});
					} else if (goal == RunState.INACTIVE) {
						rt.run("Stopping " + getShortHash(), () -> {
							debug("Stopping  ");
							client.stopContainer(container.id(), (int) WAIT_BEFORE_KILLING.getSeconds());
							debug("Waiting for stopped state...");
							RetryUtil.until(100, WAIT_BEFORE_KILLING.toMillis(),
									runstate -> runstate.equals(RunState.INACTIVE), this::fetchRunState);
							debug("Stopped  ");
						});
					}
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}
	}
	
	private void debug(String message) {
		if (DEBUG) {
			System.out.println("DockerContainer " + getShortHash() + ": " + message);
		}
	}

	@Override
	public void restart(RunState runingOrDebugging) {
		DockerRunTarget dockerTarget = getTarget();
		DockerClient client = dockerTarget.getClient();
		if (client != null) {
			try {
				AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
				console.show();
				
				RefreshStateTracker rt = this.refreshTracker.get();
				rt.run("Starting " + getShortHash(), () -> {
					client.restartContainer(container.id());					
					RetryUtil.until(100, 1000, runstate -> runstate.equals(RunState.RUNNING), this::fetchRunState);
				});
			} catch (Exception e) {
				Log.log(e);
			}
		}
		
	}
	
	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
	}

	@Override
	public void delete() throws Exception {
		DockerClient client = getTarget().getClient();
		if (client != null) {
			RefreshStateTracker rt = this.refreshTracker.get();
			rt.run("Deleting " + getShortHash(), () -> {
				debug("Deleting");
				client.removeContainer(container.id(), RemoveContainerParam.forceKill());
				debug("Waiting for Deleting");

				RetryUtil.until(100, WAIT_BEFORE_KILLING.toMillis(),
						exception -> exception instanceof ContainerNotFoundException, () -> {
							try {
								client.stats(container.id());
							} catch (Exception e) {
								return e;
							}
							return null;
						});
				debug("Deleted");
			});
		}
	}

	@Override
	public int getActualInstances() {
		return fetchRunState().isActive() ? 1 : 0;
	}

	@Override
	public int getDebugPort() {
		try {
			if (fetchRunState().isActive()) {
				String portStr = container.labels().get(DockerApp.DEBUG_PORT);
				if (portStr!=null) {
					int port = Integer.valueOf(portStr);
					if (port>0) {
						return port;
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return -1;
	}

	@Override
	public IProject getProject() {
		try {
			String projectName = container.labels().get(DockerApp.APP_NAME);
			if (projectName!=null) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	@Override
	public String getDevtoolsSecret() {
		Map<String, String> sysprops = getSystemProps(container);
		return sysprops.getOrDefault(DevtoolsUtil.REMOTE_SECRET_PROP, null);
	}

	public Map<String,String> getSystemProps() {
		return container!=null ? getSystemProps(container) : null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String,String> getSystemProps(Container c) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String sysprops = c.labels().get(DockerApp.SYSTEM_PROPS);
			if (StringUtils.hasText(sysprops)) {
				return mapper.readValue(sysprops, Map.class);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return ImmutableMap.of();
	}

	@Override
	public LogConnection connectLog(AppConsole logConsole, boolean includeHistory) {
		DockerClient client = target.getClient();
		if (client != null) {
			AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
			try {
				List<LogsParam> logParams = new ArrayList<>();
				logParams.addAll(Arrays.asList(LogsParam.stdout(), LogsParam.follow()));
				if (!includeHistory) {
					logParams.add(LogsParam.since((int)Instant.now().getEpochSecond()));
				}
				LogStream appOutput = target.getClient().logs(container.id(), logParams.toArray(new LogsParam[logParams.size()]));
				return new LogConnection() {
					
					private boolean isClosed = false;
					
					{
						JobUtil.runQuietlyInJob("Tracking output for docker container "+container.id(), mon -> {
							try {
								appOutput.attach(console.getOutputStream(LogType.APP_OUT), console.getOutputStream(LogType.APP_OUT));
							} finally {
								isClosed = true;
							}
						});

					}
					
					@Override
					public void dispose() {
						try {
							appOutput.close();
						} catch (IOException e) {
							Log.log(e);
						}
					}
					
					@Override
					public boolean isClosed() {
						return isClosed;
					}
				};
			} catch (Exception e) {
				Log.log(e);
			}			
		}
		return null;
	}

	@Override
	public ImageDescriptor getRunStateIcon(RunState runState) {
		try {
			if (RUNSTATE_ICONS==null) {
				RUNSTATE_ICONS = ImmutableMap.of(
						RunState.RUNNING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_started.png"),
						RunState.INACTIVE, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_stopped.png"),
						runState.DEBUGGING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_debugging.png")
				);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		if (RUNSTATE_ICONS!=null) {
			return RUNSTATE_ICONS.get(runState);
		}
		return null;
	}
}
