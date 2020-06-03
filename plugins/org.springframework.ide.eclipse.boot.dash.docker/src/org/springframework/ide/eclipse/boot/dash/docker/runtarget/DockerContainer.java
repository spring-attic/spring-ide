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

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.JmxConnectable;
import org.springframework.ide.eclipse.boot.dash.api.PortConnectable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.exceptions.ContainerNotFoundException;
import org.mandas.docker.client.messages.Container;

public class DockerContainer implements App, RunStateProvider, JmxConnectable, Styleable, PortConnectable, Deletable {

	private static final Duration WAIT_BEFORE_KILLING = Duration.ofSeconds(10);
	private static final boolean DEBUG = true;
	private final Container container;
	private final DockerRunTarget target;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();


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
			return RunState.RUNNING;
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
		styledString = styledString.append(getShortHash(), stylers.italicColoured(SWT.COLOR_DARK_GRAY));
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
		return EnumSet.of(RunState.RUNNING, RunState.INACTIVE);
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
				client.removeContainer(container.id());
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
}
