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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Comparator;

import org.eclipse.core.runtime.ListenerList;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

public abstract class AbstractBootDashModel extends AbstractDisposable implements BootDashModel {

	private BootDashViewModel parent;
	private RunTarget target;

	private LiveVariable<RefreshState> refreshState;

	public AbstractBootDashModel(RunTarget target, BootDashViewModel parent) {
		super();
		this.target = target;
		this.parent = parent;
		this.refreshState = new LiveVariable<RefreshState>(RefreshState.READY, this);
	}

	public RunTarget getRunTarget() {
		return this.target;
	}

	ListenerList elementStateListeners = new ListenerList();

	public void notifyElementChanged(BootDashElement element) {
		for (Object l : elementStateListeners.getListeners()) {
			((BootDashModel.ElementStateListener) l).stateChanged(element);
		}
	}

	ListenerList modelStateListeners = new ListenerList();

	protected final void notifyModelStateChanged() {
		for (Object l : modelStateListeners.getListeners()) {
			((BootDashModel.ModelStateListener) l).stateChanged(this);
		}
	}

	@Override
	public Comparator<BootDashElement> getElementComparator() {
		return null;
	}

	abstract public ObservableSet<BootDashElement> getElements();

	abstract public BootDashModelConsoleManager getElementConsoleManager();

	/**
	 * Trigger manual model refresh.
	 */
	@Override
	abstract public void refresh(UserInteractions ui);

	/**
	 * Returns the state of the model
	 * @return
	 */
	@Override
	public RefreshState getRefreshState() {
		return refreshState.getValue();
	}

	public final void setRefreshState(RefreshState newState) {
		refreshState.setValue(newState);
		notifyModelStateChanged();
	}

	public void addElementStateListener(BootDashModel.ElementStateListener l) {
		elementStateListeners.add(l);
	}

	public void removeElementStateListener(BootDashModel.ElementStateListener l) {
		elementStateListeners.remove(l);
	}

	public void addModelStateListener(BootDashModel.ModelStateListener l) {
		modelStateListeners.add(l);
	}

	public void removeModelStateListener(BootDashModel.ModelStateListener l) {
		modelStateListeners.remove(l);
	}

	public BootDashViewModel getViewModel() {
		return parent;
	}

}