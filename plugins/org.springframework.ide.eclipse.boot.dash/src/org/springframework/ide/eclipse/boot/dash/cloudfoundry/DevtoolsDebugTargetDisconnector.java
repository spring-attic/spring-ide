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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import static org.springframework.ide.eclipse.boot.dash.model.RunState.*;

/**
 * Listens for statechanges on Cloudfoundry model so that when elements are stopped, connected
 * devtools debug connections are also terminated.
 *
 * @author Kris De Volder
 */
public class DevtoolsDebugTargetDisconnector implements Disposable {

	/**
	 * States in which devtools client shouldn't be connected.
	 */
	private static final EnumSet<RunState> NON_CONNECTABLE = EnumSet.of(STARTING, INACTIVE, CRASHED, FLAPPING);

	private ElementStateListener listener;
	private Map<BootDashElement, RunState> lastKnownState = new HashMap<BootDashElement, RunState>();
	private CloudFoundryBootDashModel model;

	public DevtoolsDebugTargetDisconnector(CloudFoundryBootDashModel model) {
		this.model = model;
		model.addElementStateListener(listener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				handleStateChange(e);
			}
		});
	}

	private void handleStateChange(BootDashElement e) {
		RunState newState = e.getRunState();
		RunState oldState = lastKnownState.get(e);
		//Careful 'oldState' may be null (meaning we have not yet seen a 'previous' event for that element)
		if (NON_CONNECTABLE.contains(newState) && newState!=oldState) {
			//Cast should be safe because we are connected to CloudFoundryBootDashModel so all elements
			// should be CloudDashElement
			DevtoolsUtil.disconnectDevtoolsClientsFor((CloudAppDashElement) e);
		}
	}

	@Override
	public void dispose() {
		if (listener!=null) {
			this.model.removeElementStateListener(listener);
			listener = null;
		}
		lastKnownState = null;
	}


}
