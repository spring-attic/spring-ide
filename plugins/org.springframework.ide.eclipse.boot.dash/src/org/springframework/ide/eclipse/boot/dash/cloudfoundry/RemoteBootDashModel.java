/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

@SuppressWarnings("rawtypes")
public abstract class RemoteBootDashModel extends AbstractBootDashModel {

	final protected ValueListener NOTIFY_MODEL_STATE_CHANGE = new ValueListener() {
		@Override
		public void gotValue(LiveExpression exp, Object value) {
			RemoteBootDashModel.this.notifyModelStateChanged();
		}
	};


	public RemoteBootDashModel(RunTarget target, BootDashViewModel parent) {
		super(target, parent);
		BootDashModelContext context = parent.getContext();
		getRunTarget().getClientExp().addListener(NOTIFY_MODEL_STATE_CHANGE);
	}

	public abstract void performDeployment(Set<IProject> of, UserInteractions ui, RunState runOrDebug) throws Exception;

	public final IPropertyStore getPropertyStore() {
		return getRunTarget().getPropertyStore();
	}

	@Override
	public RemoteRunTarget getRunTarget() {
		return (RemoteRunTarget) super.getRunTarget();
	}

	public abstract void connect() throws Exception;

	public abstract void disconnect();

	@Override
	public void dispose() {
		getRunTarget().getClientExp().removeListener(NOTIFY_MODEL_STATE_CHANGE);
		super.dispose();
	}
}
