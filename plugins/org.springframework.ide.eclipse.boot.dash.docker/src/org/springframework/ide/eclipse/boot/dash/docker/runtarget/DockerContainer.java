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

import java.util.Set;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.JmxConnectable;
import org.springframework.ide.eclipse.boot.dash.api.PortConnectable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.messages.Container;

public class DockerContainer implements App, RunStateProvider, JmxConnectable, Styleable, PortConnectable {

	private final Container container;
	private final DockerRunTarget target;

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
}
