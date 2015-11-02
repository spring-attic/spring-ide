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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationOperationEventFactory {

	protected final CloudFoundryBootDashModel model;

	public ApplicationOperationEventFactory(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	/**
	 * Update the runstate and app instances of the application in the model. If
	 * no runstate is specified, the existing runstate of the element will be
	 * retained. Cases that would require this are when updating the model
	 * during a long deployment process, where the model should receive an
	 * updated app instances but retain the "STARTING" state of the element.
	 *
	 * @param appInstances
	 * @param element
	 * @param runState
	 *            state to set for the element. If null, the existing state will
	 *            be retained.
	 * @return
	 *
	 */
	public ApplicationOperationEvent getUpdateRunStateEvent(CloudAppInstances appInstances, CloudDashElement element,
			RunState runState) {
		// If no runstate specified retain the existing runstate in the element
		if (runState == null) {
			runState = element != null ? element.getRunState() : null;
		}
		return new UpdateRunStateEvent(appInstances, model, runState);
	}

	public ApplicationOperationEvent getUpdateRunStateEvent(CloudDashElement element, RunState runState) {
		// Dont fire anything if the element is not specified
		if (element != null) {
			// If no runstate specified retain the existing runstate in the
			// element

			if (runState == null) {
				runState = element.getRunState();
			}
			return new UpdateElementRunStateEvent(element, model, runState);
		}
		return null;

	}

	class UpdateRunStateEvent implements ApplicationOperationEvent {

		protected final CloudAppInstances appInstances;

		protected final CloudFoundryBootDashModel model;

		protected final RunState runState;

		UpdateRunStateEvent(CloudAppInstances appInstances, CloudFoundryBootDashModel model, RunState runState) {
			this.appInstances = appInstances;
			this.model = model;
			this.runState = runState;
		}

		@Override
		public void fire() {
			model.updateApplication(appInstances, runState);
		}

		@Override
		public CloudAppInstances getAppInstances() {
			return this.appInstances;
		}

	}

	class UpdateElementRunStateEvent implements ApplicationOperationEvent {

		protected final CloudDashElement element;

		protected final CloudFoundryBootDashModel model;

		protected final RunState runState;

		UpdateElementRunStateEvent(CloudDashElement element, CloudFoundryBootDashModel model, RunState runState) {
			this.element = element;
			this.model = model;
			this.runState = runState;
		}

		@Override
		public void fire() {
			model.updateElementRunState(element, runState);
		}

		@Override
		public CloudAppInstances getAppInstances() {
			return element != null ? model.getAppCache().getAppInstances(element.getName()) : null;
		}

	}
}
