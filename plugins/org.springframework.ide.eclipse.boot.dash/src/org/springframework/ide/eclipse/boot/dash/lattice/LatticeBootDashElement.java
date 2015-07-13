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

import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.STARTING;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPResponse;
import io.pivotal.receptor.support.Route;

public class LatticeBootDashElement extends WrappingBootDashElement<String> {

	private String processGuid;
	private RunTarget target;

	private LiveVariable<DesiredLRPResponse> desiredLrp = new LiveVariable<DesiredLRPResponse>();
	private LiveSet<ActualLRPResponse> actualLrps = new LiveSet<ActualLRPResponse>();

	private LiveExpression<Integer> runningInstances = new LiveExpression<Integer>(0) {
		{
			dependsOn(actualLrps);
		}
		protected Integer compute() {
			int count = 0;
			for (ActualLRPResponse alrp : actualLrps.getValues()) {
				if (getRunState(alrp)==RunState.RUNNING) {
					count++;
				}
			}
			return count;
		}
	};

	private LiveExpression<Integer> desiredInstances = new LiveExpression<Integer>(0) {
		{
			dependsOn(desiredLrp);
		}
		protected Integer compute() {
			DesiredLRPResponse lrp = desiredLrp.getValue();
			if (lrp!=null) {
				return lrp.getInstances();
			}
			return 0;
		}
	};

	private LiveExpression<RunState> runState = new LiveExpression<RunState>(INACTIVE) {
		{
			dependsOn(desiredInstances);
			dependsOn(actualLrps);
		}
		protected RunState compute() {
			RunState stateSummary = getDesiredInstances()>0?RunState.STARTING:RunState.INACTIVE;
			for (ActualLRPResponse alrp : actualLrps.getValues()) {
				stateSummary = stateSummary.merge(getRunState(alrp));
			}
			return stateSummary;
		}
	};

	private PropertyStoreApi persistentProperties;

	public LatticeBootDashElement(LatticeBootDashModel parent, RunTarget target, String processGuid, IPropertyStore store) {
		super(parent, processGuid);
		this.processGuid = processGuid;
		this.target = target;
		registerLiveExpListener();
		this.persistentProperties = PropertyStoreFactory.createApi(store);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerLiveExpListener() {
		ValueListener modelChangeNotfiier = new ValueListener() {
			public void gotValue(LiveExpression exp, Object value) {
				getParent().notifyElementChanged(LatticeBootDashElement.this);
			}
		};
		runState.addListener(modelChangeNotfiier);
		runningInstances.addListener(modelChangeNotfiier);
		desiredInstances.addListener(modelChangeNotfiier);
	}

	private static RunState getRunState(ActualLRPResponse alrp) {
		String state = alrp.getState();
		// lattice has these 'process states':
		// "UNCLAIMED", "CLAIMED", "RUNNING" or "CRASHED"
		if ("UNCLAIMED".equals(state)) {
			return INACTIVE;
		} else if ("CLAIMED".equals(state)) {
			return STARTING;
		} else if ("RUNNING".equals(state)) {
			return RUNNING;
		} else if ("CRASHED".equals(state)) {
			return INACTIVE;
		} else {
			throw new IllegalArgumentException("Unexpected LRP state: "+state);
		}
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
		return runState.getValue();
	}

	@Override
	public RunTarget getTarget() {
		return target;
	}

	@Override
	public int getLivePort() {
		DesiredLRPResponse lrp = desiredLrp.getValue();
		if (lrp!=null) {
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
		}
//		System.out.println("<<<< routes");
		return -1;
	}

	@Override
	public String getLiveHost() {
		DesiredLRPResponse lrp = desiredLrp.getValue();
		if (lrp!=null) {
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
		return processGuid;
	}

	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	public void setDesiredLrp(DesiredLRPResponse lrp) {
		this.desiredLrp.setValue(lrp);
	}

	@Override
	public int getActualInstances() {
		return runningInstances.getValue();
	}

	@Override
	public int getDesiredInstances() {
		return desiredInstances.getValue();
	}

	public void setActualLrps(Collection<ActualLRPResponse> alrps) {
		actualLrps.replaceAll(alrps);
	}
}
