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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPResponse;
import io.pivotal.receptor.support.Route;

public class LatticeBootDashElement extends WrappingBootDashElement<String> {

	private String processGuid;
	private DesiredLRPResponse desiredLrp;
	private Map<Integer, ActualLRPResponse> actualLrps = new TreeMap<Integer, ActualLRPResponse>();

	private RunTarget target;
	private BootDashModel parent;

	private LiveExpression<RunState> runState = new LiveExpression<RunState>(INACTIVE) {
		protected RunState compute() {
			RunState stateSummary = getDesiredInstances()>0?RunState.STARTING:RunState.INACTIVE;
			for (ActualLRPResponse alrp : getActualLrps()) {
				stateSummary = stateSummary.merge(getRunState(alrp));
			}
			return stateSummary;
		}
	};

	private LiveExpression<Integer> runningInstances = new LiveExpression<Integer>(0) {
		protected Integer compute() {
			int count = 0;
			for (ActualLRPResponse alrp : getActualLrps()) {
				if (getRunState(alrp)==RunState.RUNNING) {
					count++;
				}
			}
			return count;
		}
	};

	private LiveExpression<Integer> desiredInstances = new LiveExpression<Integer>(0) {
		protected Integer compute() {
			DesiredLRPResponse lrp = desiredLrp;
			if (lrp!=null) {
				return lrp.getInstances();
			}
			return 0;
		}
	};

	public LatticeBootDashElement(final BootDashModel parent, RunTarget target, String processGuid) {
		super(processGuid);
		this.processGuid = processGuid;
		this.parent = parent;
		this.target = target;
		registerLiveExpListener(parent);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerLiveExpListener(final BootDashModel parent) {
		ValueListener modelChangeNotfiier = new ValueListener() {
			public void gotValue(LiveExpression exp, Object value) {
				parent.notifyElementChanged(LatticeBootDashElement.this);
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

	public void putActualLrp(ActualLRPResponse alrp) {
		synchronized (this) {
			int id = alrp.getIndex();
			actualLrps.put(id, alrp);
		}
		refreshLivexps();
	}

	private void refreshLivexps() {
		runState.refresh();
		runningInstances.refresh();
		desiredInstances.refresh();
	}

	public synchronized ActualLRPResponse[] getActualLrps() {
		Collection<ActualLRPResponse> collection = actualLrps.values();
		return collection.toArray(new ActualLRPResponse[collection.size()]);
	}

	public synchronized void removeActualLrp(ActualLRPResponse removedLrp) {
		actualLrps.remove(removedLrp.getIndex());
		refreshLivexps();
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
		DesiredLRPResponse lrp = desiredLrp;
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
		DesiredLRPResponse lrp = desiredLrp;
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

	@Override
	public LinkedHashSet<String> getTags() {
		return new LinkedHashSet<String>();
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {
		// TODO Auto-generated method stub

	}

	public void setDesiredLrp(DesiredLRPResponse lrp) {
		this.desiredLrp = lrp;
		refreshLivexps();
	}

	@Override
	public int getActualInstances() {
		return runningInstances.getValue();
	}

	@Override
	public int getDesiredInstances() {
		return desiredInstances.getValue();
	}



}
