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
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget.ConnectMode;
import org.springframework.ide.eclipse.boot.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

@SuppressWarnings("rawtypes")
public abstract class RemoteBootDashModel extends AbstractBootDashModel {

	protected static final String AUTO_CONNECT_PROP = GenericRemoteBootDashModel.class.getSimpleName()+".auto-connect";

	public final RefreshStateTracker refreshTracker = new RefreshStateTracker(this);

	public RemoteBootDashModel(RunTarget target, BootDashViewModel parent) {
		super(target, parent);
		BootDashModelContext context = parent.getContext();
		getRunTarget().getClientExp().onChange(this, (e, v) -> {
			RemoteBootDashModel.this.notifyModelStateChanged();
			RemoteBootDashModel.this.getViewModel().updateTargetPropertiesInStore();
		});
		refreshTracker.refreshState.onChange(this, (e, v) -> {
			RemoteBootDashModel.this.notifyModelStateChanged();
		});
		addDisposableChild(refreshTracker.refreshState);
		if (getRunTarget().getPersistentProperties().get(AUTO_CONNECT_PROP, true)) {
			if (!getRunTarget().isConnected()) {
				Log.async(connect(ConnectMode.AUTOMATIC));
			}
		}
	}

	public final IPropertyStore getPropertyStore() {
		return getRunTarget().getPropertyStore();
	}

	@Override
	public RemoteRunTarget getRunTarget() {
		return (RemoteRunTarget) super.getRunTarget();
	}

	final public CompletableFuture<Void> connect(ConnectMode mode) {
		return refreshTracker.callAsync("Connecting...", () -> {
			try {
				getRunTarget().connect(mode);
			} catch (Exception e) {
				if (mode==ConnectMode.INTERACTIVE) {
					ui().errorPopup("Failed to connect to " + getDisplayName() + ". ", ExceptionUtil.getMessage(e));
				}
				throw e;
			}
			getRunTarget().getPersistentProperties().put(AUTO_CONNECT_PROP, true);
			return null;
		});
	}

	final public void disconnect() {
		try {
			getRunTarget().getPersistentProperties().put(AUTO_CONNECT_PROP, false);
		} catch (Exception e) {
			Log.log(e);
		}
		getRunTarget().disconnect();
	}


}
