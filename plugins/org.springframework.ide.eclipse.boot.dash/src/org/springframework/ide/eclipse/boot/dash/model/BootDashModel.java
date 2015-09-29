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

import org.eclipse.core.runtime.ListenerList;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public abstract class BootDashModel {

	private RunTarget target;

	private LiveVariable<RefreshState> state = new LiveVariable<RefreshState>(RefreshState.READY, this);

	public BootDashModel(RunTarget target) {
		super();
		this.target = target;
	}

	public RunTarget getRunTarget() {
		return this.target;
	}

	ListenerList elementStateListeners = new ListenerList();

	public void notifyElementChanged(BootDashElement element) {
		for (Object l : elementStateListeners.getListeners()) {
			((ElementStateListener) l).stateChanged(element);
		}
	}

	abstract public LiveSet<BootDashElement> getElements();

	abstract public BootDashModelConsoleManager getElementConsoleManager();

	/**
	 * When no longer needed the model should be disposed, otherwise it will
	 * continue listening for changes to the workspace in order to keep itself
	 * in synch.
	 */
	abstract public void dispose();

	/**
	 * Trigger manual model refresh.
	 */
	abstract public void refresh();

	/**
	 * Returns the state of the model
	 * @return
	 */
	public RefreshState getState() {
		return state.getValue();
	}

	public final void setState(RefreshState newState) {
		state.setValue(newState);
	}

	public void addElementStateListener(ElementStateListener l) {
		elementStateListeners.add(l);
	}

	public void removeElementStateListener(ElementStateListener l) {
		elementStateListeners.remove(l);
	}

	public interface ElementStateListener {
		/**
		 * Called when something about the element has changed.
		 * <p>
		 * Note this doesn't get called when elements are added / removed etc.
		 * Only when some property of the element itself has changed.
		 */
		void stateChanged(BootDashElement e);
	}

	public void addModelStateListener(ValueListener<RefreshState> l) {
		state.addListener(l);
	}

	public void removeModelStateListener(ValueListener<RefreshState> l) {
		state.removeListener(l);
	}

}