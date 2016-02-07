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
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.OnDispose;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

public abstract class AbstractDisposable implements Disposable, OnDispose {

	private ListenerList disposeListeners = new ListenerList();

	@Override
	public void onDispose(DisposeListener l) {
		this.disposeListeners.add(l);
	}

	@Override
	public void dispose() {
		if (disposeListeners!=null) {
			for (Object _l : disposeListeners.getListeners()) {
				DisposeListener l = (DisposeListener) _l;
				l.disposed(this);
			}
		}
	}

	/**
	 * Convenience method to declare that a given {@link Disposable} is an 'owned' child of
	 * this element and should also be disposed when this element itself is disposed.
	 */
	public <C extends Disposable> C addDisposableChild(final C child) {
		onDispose(new DisposeListener() {
			public void disposed(Disposable disposed) {
				child.dispose();
			}
		});
		return child;
	}
}
