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
package org.springframework.ide.eclipse.boot.dash.lattice;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

import io.pivotal.receptor.commands.DesiredLRPResponse;
import io.pivotal.receptor.support.Route;

public class LatticeBootDashElement extends WrappingBootDashElement<String> {

	private DesiredLRPResponse lrp;
	private RunTarget target;

	public LatticeBootDashElement(RunTarget target, DesiredLRPResponse lrp) {
		super(lrp.getProcessGuid());
		this.target = target;
		this.lrp = lrp;
	}

	@Override
	public IJavaProject getJavaProject() {
		return null;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		return RunState.INACTIVE;
	}

	@Override
	public RunTarget getTarget() {
		return target;
	}

	@Override
	public int getLivePort() {
		Map<String, Route[]> routes = lrp.getRoutes();
//		System.out.println(">>>> routes");
		for (Entry<String, Route[]> e : routes.entrySet()) {
//			System.out.println(e.getKey()+":");
			for (Route r : e.getValue()) {
				int port = r.getPort();
				if (port>0) {
					//The router actually forwards default http port 80 to
					//  some port on running app. The 'port' here is the port
					//  we are forwarding to. But port 80 is the actual port
					//  on the client-side.
					return 80;
				}
			}
		}
//		System.out.println("<<<< routes");
		return -1;
	}

	@Override
	public String getLiveHost() {
		Map<String, Route[]> routes = lrp.getRoutes();
//		System.out.println(">>>> routes");
		for (Entry<String, Route[]> e : routes.entrySet()) {
//			System.out.println(e.getKey()+":");
			for (Route r : e.getValue()) {
				for (String host : r.getHostnames()) {
					return host;
				}
			}
		}
//		System.out.println("<<<< routes");
		return null;
	}

	private String toString(Route r) {
		StringBuilder buf = new StringBuilder();
		buf.append(r.getPort()+": ");
		boolean first = true;
		for (String host : r.getHostnames()) {
			if (!first) {
				buf.append(", ");
			}
			buf.append(host);
			first = false;
		}
		return buf.toString();
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultRequestMappingPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultRequestMapingPath(String defaultPath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void openConfig(UserInteractions ui) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return lrp.getProcessGuid();
	}

	@Override
	public LinkedHashSet<String> getTags() {
		return new LinkedHashSet<String>();
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {
		// TODO Auto-generated method stub

	}

}
