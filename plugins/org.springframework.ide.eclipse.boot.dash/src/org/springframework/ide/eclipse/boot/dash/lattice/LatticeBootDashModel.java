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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPResponse;

public class LatticeBootDashModel extends BootDashModel {

	protected static final long REFRESH_INTERVAL = 500;
	private Map<String, LatticeBootDashElement> elementByProcessGuid = new TreeMap<String, LatticeBootDashElement>();
	private LiveSet<BootDashElement> elements;

	private LatticeRunTarget ltcTarget;
	private ReceptorClient receptor;
	private BootDashModelContext context;
	private Job refreshJob;

	public LatticeBootDashModel(LatticeRunTarget target, BootDashModelContext context) {
		super(target);
		this.ltcTarget = target;
		this.context = context;
	}

	@Override
	public synchronized LiveSet<BootDashElement> getElements() {
		if (elements==null) {
			elements = new LiveSet<BootDashElement>();
			startRefreshJob();
		}
		return elements;
	}

	private void startRefreshJob() {
		this.refreshJob = new Job("Refresh Lattice State") {
			protected IStatus run(IProgressMonitor monitor) {
				if (!isDisposed()) {
					try {
						refresh();
					} catch (Throwable e) {
						BootDashActivator.log(e);
					}
					this.schedule(REFRESH_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		refreshJob.setSystem(true);
		refreshJob.schedule();
	}

	private boolean isDisposed() {
		return ltcTarget==null;
	}

	private synchronized ReceptorClient getReceptor() {
		if (receptor==null) {
			receptor = new ReceptorClient(ltcTarget.getReceptorHost());
		}
		return receptor;
	}

	@Override
	public void dispose() {
		if (this.refreshJob!=null) {
			this.refreshJob.cancel();
			this.refreshJob = null;
		}
		this.ltcTarget = null;
		this.context = null;
		this.elements = null;
		this.receptor = null;
	}

	@Override
	public void refresh() {
		List<DesiredLRPResponse> dlrps = refreshDesiredLRPs();
		refreshActualLRPs(dlrps);
	}

	private void refreshActualLRPs(List<DesiredLRPResponse> dlrps) {
		List<ActualLRPResponse> lrps = getReceptor().getActualLRPs();
		Multimap<String, ActualLRPResponse> byProcessGuid = ArrayListMultimap.create();
		for (ActualLRPResponse lrp : lrps) {
			byProcessGuid.put(lrp.getProcessGuid(), lrp);
		}
		for (DesiredLRPResponse dlrp : dlrps) {
			String processGuid = dlrp.getProcessGuid();
			Collection<ActualLRPResponse> alrps = byProcessGuid.get(processGuid);
			LatticeBootDashElement el = getElementFor(processGuid);
			if (el!=null) {
				el.setActualLrps(alrps);
			}
		}
	}

	public List<DesiredLRPResponse> refreshDesiredLRPs() {
		List<DesiredLRPResponse> lrps = getReceptor().getDesiredLRPs();
		Set<String> activeProcessGuids = new HashSet<String>(lrps.size());
		for (DesiredLRPResponse dlrp : lrps) {
			activeProcessGuids.add(dlrp.getProcessGuid());
		}
		ArrayList<BootDashElement> newElements = new ArrayList<BootDashElement>();
		for (DesiredLRPResponse lrp : lrps) {
			String id = lrp.getProcessGuid();
			LatticeBootDashElement el = ensureElement(id);
			newElements.add(el);
			el.setDesiredLrp(lrp);
			elementByProcessGuid.put(id, el);
		}
		elements.replaceAll(newElements);
		return lrps;
	}

	private synchronized LatticeBootDashElement ensureElement(String processGuid) {
		LatticeBootDashElement existing;
		LatticeBootDashElement created = null;
		synchronized (this) {
			existing = elementByProcessGuid.get(processGuid);
			if (existing==null) {
				created = existing = new LatticeBootDashElement(this, ltcTarget, processGuid);
				elementByProcessGuid.put(processGuid, created);
			}
		}
		if (created!=null) {
			elements.add(created);
		}
		return existing;
	}

	private synchronized LatticeBootDashElement getElementFor(String processGuid) {
		return elementByProcessGuid.get(processGuid);
	}

}
