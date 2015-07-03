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
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.DesiredLRPResponse;

public class LatticeBootDashModel extends BootDashModel {

	private LiveSet<BootDashElement> elements;

	private LatticeRunTarget ltcTarget;
	private ReceptorClient receptor;
	private BootDashModelContext context;

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
		List<DesiredLRPResponse> lrps = getReceptor().getDesiredLRPs();
		List<BootDashElement> newElements = new ArrayList<BootDashElement>(lrps.size());
		for (DesiredLRPResponse lrp : lrps) {
			newElements.add(new LatticeBootDashElement(ltcTarget, lrp));
		}
		elements.replaceAll(newElements);
	}

}
