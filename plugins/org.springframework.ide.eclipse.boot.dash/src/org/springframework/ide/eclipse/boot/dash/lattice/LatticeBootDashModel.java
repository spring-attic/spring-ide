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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPResponse;
import io.pivotal.receptor.events.ActualLRPChangedEvent;
import io.pivotal.receptor.events.ActualLRPCreatedEvent;
import io.pivotal.receptor.events.ActualLRPRemovedEvent;
import io.pivotal.receptor.events.DesiredLRPChangedEvent;
import io.pivotal.receptor.events.DesiredLRPCreatedEvent;
import io.pivotal.receptor.events.DesiredLRPRemovedEvent;
import io.pivotal.receptor.events.EventListener;
import io.pivotal.receptor.events.ReceptorEvent;

public class LatticeBootDashModel extends BootDashModel {

	private Map<String, LatticeBootDashElement> elementByProcessGuid = new TreeMap<String, LatticeBootDashElement>();
	private LiveSet<BootDashElement> elements;

	private LatticeRunTarget ltcTarget;
	private ReceptorClient receptor;
	private BootDashModelContext context;

	private EventListener<ReceptorEvent<?>> receptorListener = new EventListener<ReceptorEvent<?>>() {
		public void onEvent(ReceptorEvent<?> _event) {
			System.out.println(">>> ReceptorEvent: "+_event);
			if (_event instanceof DesiredLRPChangedEvent) {
				DesiredLRPChangedEvent event = (DesiredLRPChangedEvent) _event;
				DesiredLRPResponse newLrp = event.getDesiredLRPAfter();
				LatticeBootDashElement el = ensureElement(newLrp.getProcessGuid());
				el.setDesiredLrp(newLrp);
			} else if (_event instanceof DesiredLRPCreatedEvent) {
				DesiredLRPCreatedEvent event = (DesiredLRPCreatedEvent) _event;
				DesiredLRPResponse newLrp = event.getData().get("desired_lrp");
				LatticeBootDashElement el = ensureElement(newLrp.getProcessGuid());
				el.setDesiredLrp(newLrp);
			} else if (_event instanceof DesiredLRPRemovedEvent) {
				DesiredLRPRemovedEvent event = (DesiredLRPRemovedEvent) _event;
				DesiredLRPResponse removedLrp = event.getData().get("desired_lrp");
				removeElement(removedLrp.getProcessGuid());
			} else if (_event instanceof ActualLRPRemovedEvent) {
				ActualLRPRemovedEvent event = (ActualLRPRemovedEvent) _event;
				ActualLRPResponse removedLrp = event.getData().get("actual_lrp");
				removeActualLrp(removedLrp);
			} else if (_event instanceof ActualLRPCreatedEvent) {
				ActualLRPCreatedEvent event = (ActualLRPCreatedEvent) _event;
				ActualLRPResponse newLrp = event.getData().get("actual_lrp");
				updateActualLrp(newLrp);
			} else if (_event instanceof ActualLRPChangedEvent) {
				ActualLRPChangedEvent event = (ActualLRPChangedEvent) _event;
				ActualLRPResponse newLrp = event.getActualLRPAfter();
				updateActualLrp(newLrp);
			}
		}

		private void updateActualLrp(ActualLRPResponse newLrp) {
			String processGuid = newLrp.getProcessGuid();
			LatticeBootDashElement el = ensureElement(processGuid);
			el.putActualLrp(newLrp);
		}

		private void removeActualLrp(ActualLRPResponse removedLrp) {
			String processGuid = removedLrp.getProcessGuid();
			LatticeBootDashElement el = getElementFor(processGuid);
			if (el!=null) {
				el.removeActualLrp(removedLrp);
			}
		}



		/* Event sequence when downscaling app from 2 instances to 0 instances:

		  Event [id=0, type=desired_lrp_changed,
		      data={
		          desired_lrp_before=DesiredLRPCreateRequest [
		              processGuid=boot-app,
		              domain=lattice,
		              rootfs=docker:///kdvolder/gs-spring-boot-docker#latest,
		              instances=2,
		              stack=lucid64,
		              ports=[8080],
		              routes={cf-router=[Lio.pivotal.receptor.support.Route;@b06494e},
		              env=[io.pivotal.receptor.support.EnvironmentVariable@5c1a65e, io.pivotal.receptor.support.EnvironmentVariable@1fe223e],
		              cpuWeight=100, diskMb=0, memoryMb=128, privileged=true, noMonitor=false, logGuid=boot-app, metricsGuid=boot-app,
		              logSource=APP, setup={download=io.pivotal.receptor.actions.DownloadAction@44fcac65},
		              action={run=RunAction [path=java, args=[-Djava.security.egd=file:/dev/./urandom, -jar, /app.jar],
		              dir=/, resourceLimits={}, env=null, logSource=null]}, monitor={run=RunAction
		              [path=/tmp/healthcheck, args=[-timeout, 1s, -port, 8080], dir=null, resourceLimits={}, env=null, logSource=HEALTH]}, startTimeout=0],
		          desired_lrp_after=DesiredLRPCreateRequest [
		              processGuid=boot-app, domain=lattice,
		              rootfs=docker:///kdvolder/gs-spring-boot-docker#latest,
		              instances=0,
		              stack=lucid64,
		              ports=[8080],
		              routes={cf-router=[Lio.pivotal.receptor.support.Route;@7d09d18d},
		              env=[io.pivotal.receptor.support.EnvironmentVariable@75f26b24, io.pivotal.receptor.support.EnvironmentVariable@5ed4876], cpuWeight=100, diskMb=0, memoryMb=128, privileged=true, noMonitor=false, logGuid=boot-app, metricsGuid=boot-app, logSource=APP, setup={download=io.pivotal.receptor.actions.DownloadAction@24d3532a}, action={run=RunAction [path=java, args=[-Djava.security.egd=file:/dev/./urandom, -jar, /app.jar], dir=/, resourceLimits={}, env=null, logSource=null]}, monitor={run=RunAction [path=/tmp/healthcheck, args=[-timeout, 1s, -port, 8080], dir=null, resourceLimits={}, env=null, logSource=HEALTH]}, startTimeout=0]}]
			ReceptorEvent: Event [id=0, type=actual_lrp_removed, data={actual_lrp=ActualLRPResponse{processGuid='boot-app', instanceGuid='87993c69-7889-44fc-7bee-9977aed048e7', cellId='cell-01', domain='lattice', index=1, address='192.168.11.11', ports=[Port{containerPort=8080, hostPort=61004}], state='RUNNING', since=1436210776218424794}}]
			ReceptorEvent: Event [id=0, type=actual_lrp_removed, data={actual_lrp=ActualLRPResponse{processGuid='boot-app', instanceGuid='9652a0e0-0a1b-40a1-510f-53d567525692', cellId='cell-01', domain='lattice', index=0, address='192.168.11.11', ports=[Port{containerPort=8080, hostPort=61002}], state='RUNNING', since=1436206917650819822}}]

		 */

	};

	public LatticeBootDashModel(LatticeRunTarget target, BootDashModelContext context) {
		super(target);
		this.ltcTarget = target;
		this.context = context;
	}

	@Override
	public synchronized LiveSet<BootDashElement> getElements() {
		if (elements==null) {
			elements = new LiveSet<BootDashElement>();
			refresh();
			getReceptor().subscribeToEvents(receptorListener);
		}
		return elements;
	}

	private synchronized ReceptorClient getReceptor() {
		if (receptor==null) {
			receptor = new ReceptorClient(ltcTarget.getReceptorHost());
		}
		return receptor;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void refresh() {
		refreshDesiredLRPs();
		refreshActualLRPs();
	}

	private void refreshActualLRPs() {
		System.out.println(">>> actual lrps");
		List<ActualLRPResponse> lrps = getReceptor().getActualLRPs();
		for (ActualLRPResponse lrp : lrps) {
			actualLrpCreated(lrp);
			System.out.println(lrp);
		}
	}

	private void actualLrpCreated(ActualLRPResponse lrp) {
		String guid = lrp.getProcessGuid();
		LatticeBootDashElement element = this.elementByProcessGuid.get(guid);
		element.putActualLrp(lrp);
	}

	public void refreshDesiredLRPs() {
		List<DesiredLRPResponse> lrps = getReceptor().getDesiredLRPs();
		for (DesiredLRPResponse lrp : lrps) {
			String id = lrp.getProcessGuid();
			LatticeBootDashElement el = ensureElement(id);
			el.setDesiredLrp(lrp);
			elementByProcessGuid.put(id, el);
		}
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

	private void removeElement(String processGuid) {
		LatticeBootDashElement el;
		synchronized (this) {
			el = elementByProcessGuid.remove(processGuid);
		}
		if (el!=null) {
			elements.remove(el);
		}
	}

	private synchronized LatticeBootDashElement getElementFor(String processGuid) {
		return elementByProcessGuid.get(processGuid);
	}

}
