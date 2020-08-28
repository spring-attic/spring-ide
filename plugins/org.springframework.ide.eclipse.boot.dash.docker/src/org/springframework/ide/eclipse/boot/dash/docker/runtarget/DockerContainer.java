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

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;
import static org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType.PLUGIN_ID;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;

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
import org.springframework.ide.eclipse.boot.dash.api.LogSource;
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
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class DockerContainer implements App, RunStateProvider, JmxConnectable, Styleable, PortConnectable, 
	Deletable, ActualInstanceCount, DebuggableApp, ProjectRelatable, DevtoolsConnectable, LogSource, RunStateIconProvider
{

	public static final Duration WAIT_BEFORE_KILLING = Duration.ofSeconds(10);
	private static final boolean DEBUG = true;
	private final Container container;
	private final DockerRunTarget target;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();
	
	private static Map<RunState, ImageDescriptor> RUNSTATE_ICONS = null;
	private DockerApp app;

	public DockerContainer(DockerRunTarget target, DockerApp app, Container container) {
		this.target = target;
		this.app = app;
		this.container = container;
	}

	@Override
	public String getName() {
		return container.getId();
	}

	@Override
	public RunState fetchRunState() {
		String state = container.getState();
		if ("running".equals(state)) {
			return (container.getLabels().get(DockerApp.DEBUG_PORT)!=null) 
					? RunState.DEBUGGING
					: RunState.RUNNING;
		} else if ("exited".equals(state)) {
			return RunState.INACTIVE;
		} else if ("paused".equals(state)) {
			return RunState.PAUSED;
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
			String port = container.getLabels().get(DockerApp.JMX_PORT);
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
		return "DockerContainer("+container.getId()+")";
	}


	@Override
	public StyledString getStyledName(Stylers stylers) {
		StyledString styledString = new StyledString();
		if (container.getNames() != null && container.getNames().length>0) {
			styledString = styledString.append(StringUtil.removePrefix(container.getNames()[0], "/"));
		}
		styledString = styledString.append(" (" +getShortHash()+")", StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	private String getShortHash() {
		String id = container.getId();
		if (id.length() > 12) {
			id = id.substring(0, 12);
		}		
		return id;
	}

	@Override
	public Set<Integer> getPorts() {
		ImmutableSet.Builder<Integer> livePorts = ImmutableSet.builder();
		String portVal = container.getLabels().get(DockerApp.APP_LOCAL_PORT);
		if (portVal != null) {
			livePorts.add(Integer.parseInt(portVal));
		}
		return livePorts.build();
	}
	
	@Override
	public EnumSet<RunState> supportedGoalStates() {
		if (container.getLabels().get(DockerApp.DEBUG_PORT)!=null) {
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
							client.startContainerCmd(container.getId()).exec();
							RetryUtil.until(100, 1000, runstate -> runstate.equals(RunState.RUNNING), this::fetchRunState);
						});
					} else if (goal == RunState.INACTIVE) {
						rt.run("Stopping " + getShortHash(), () -> {
							debug("Stopping  ");
							client.stopContainerCmd(container.getId()).withTimeout((int) WAIT_BEFORE_KILLING.getSeconds()).exec();
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
					client.restartContainerCmd(container.getId()).exec();					
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
				client.removeContainerCmd(container.getId()).withForce(true).exec();
				debug("Waiting for Deleting");

				RetryUtil.until(100, WAIT_BEFORE_KILLING.toMillis(),
						exception -> exception instanceof NotFoundException, () -> {
							try {
								client.inspectContainerCmd(container.getId()).exec();
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
				String portStr = container.getLabels().get(DockerApp.DEBUG_PORT);
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
			String projectName = container.getLabels().get(DockerApp.APP_NAME);
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
			String sysprops = c.getLabels().get(DockerApp.SYSTEM_PROPS);
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
			return connectLog(client, container.getId(), logConsole, includeHistory);
		}
		return null;
	}
	
	private static class LogHandler implements ResultCallback<Frame>{

		private AtomicBoolean isClosed = new AtomicBoolean();

		private OutputStream consoleOut;
		private OutputStream consoleErr;
		
		LogConnection connection = new LogConnection() {
			
			@Override
			public void dispose() {
				LogHandler.this.close();
			}
			
			@Override
			public boolean isClosed() {
				return isClosed.get();
			}
		};

		private final CompletableFuture<Closeable> closeable = new CompletableFuture<Closeable>();

		public LogHandler(AppConsole console) {
			consoleOut = console.getOutputStream(LogType.APP_OUT);
			consoleErr = console.getOutputStream(LogType.APP_OUT);
		}

		@Override
		public void close() {
			if (isClosed.compareAndSet(false, true)) {
				closeable.thenAccept(c -> {
					try {
						c.close();
					} catch (IOException e) {
					}
				});
				try {
					consoleOut.close();
				} catch (IOException e) {
				}
				try {
					consoleErr.close();
				} catch (IOException e) {
				}
			}
		}

		@Override
		public void onStart(Closeable closeable) {
			this.closeable.complete(closeable);
		}

		@Override
		public void onNext(Frame logMsg) {
			try {
				StreamType tp = logMsg.getStreamType();
				if (tp==StreamType.STDERR) {
					consoleErr.write(logMsg.getPayload());
				} else if (tp==StreamType.STDOUT) {
					consoleOut.write(logMsg.getPayload());
				} else {
					Log.warn("Unknown docker log frame type dropped: "+tp);
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}

		@Override
		public void onError(Throwable e) {
			try {
				consoleErr.write(ExceptionUtil.getMessage(e).getBytes(Charset.forName("UTF8")+"\n"));
			} catch (Exception e1) {
				Log.log(e1);
			}
			Log.log(e);
			this.close();
		}

		@Override
		public void onComplete() {
			this.close();
		}
	}
	
	public static LogConnection connectLog(DockerClient client, String containerId, AppConsole console, boolean includeHistory) {
		LogContainerCmd cmd = client.logContainerCmd(containerId)
				.withStdOut(true).withStdErr(true).withFollowStream(true);
		
		if (!includeHistory) {
			cmd = cmd.withSince((int)Instant.now().getEpochSecond());
		}
		
		LogHandler logHandler = cmd.exec(new LogHandler(console));
		return logHandler.connection;
	}
//			
//			LogStream appOutput = client.logs(containerId, logParams.toArray(new LogsParam[logParams.size()]));
//			return new LogConnection() {
//
//				private boolean isClosed = false;
//				private OutputStream consoleOut = console.getOutputStream(LogType.APP_OUT);
//				private OutputStream consoleErr = console.getOutputStream(LogType.APP_OUT);
//				
//				{
//					JobUtil.runQuietlyInJob("Tracking output for docker container "+containerId, mon -> {
//						try {
//							appOutput.attach(consoleOut, consoleErr);
//						} finally {
//							isClosed = true;
//						}
//					});
//
//				}
//				
//				@Override
//				public void dispose() {
//					try {
//						appOutput.close(); 
//							//Warning... appOutput.close seems to have no effect. This seems like the right way to disconnect from
//							// docker log stream... but it doesn't work.
//							//So we also close the consoleOut as a 'backup plan'. When later on more messages are streamed then the
//							// closed console output stream will throw an IOExcption. Since this is called as a 'callback' from
//							// appOutput.attach. that interrupts the job running appOutput.attach(consoleOut, consoleErr); and allows
//							// it to terminate as it should, when connection is closed.
//						consoleOut.close();
//						consoleErr.close();
//					} catch (IOException e) {
//						Log.log(e);
//					}
//				}
//				
//				@Override
//				public boolean isClosed() {
//					return isClosed;
//				}
//			};
//		} catch (Exception e) {
//			Log.log(e);
//		}			
//		return null;
//	}

	@Override
	public ImageDescriptor getRunStateIcon(RunState runState) {
		try {
			if (RUNSTATE_ICONS==null) {
				RUNSTATE_ICONS = ImmutableMap.of(
						RunState.RUNNING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_started.png"),
						RunState.INACTIVE, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_stopped.png"),
						RunState.DEBUGGING, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_debugging.png"),
						RunState.PAUSED, imageDescriptorFromPlugin(PLUGIN_ID, "/icons/container_paused.png")
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
	
	@Override
	public String getConsoleDisplayName() {
		return app.getName() + " - in container "+getStyledName(null).getString()+" @ "+getTarget().getName();
	}
}
