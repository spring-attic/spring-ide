/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Helper class that can be attached to a LiveExpression to track its value
 * and, whenever the value is changed, call 'dispose' on the previous value.
 * <p>
 * It also ensures that 'dispose' is called on the final value if the LiveExp
 * itself is disposed.
 * <p>
 * Note: The LiveExp framework's design as grown over time unfortunately doesn't
 * currently provide a strong guarantee that *all* values will be seen by a listener
 * So, some care should be taken with the helper class. It should *only* really be
 * applied to a type of LiveExp that calls its value listeners synchronously (i.e.
 * listener is called immediately when the value changed.
 * <p>
 * Even for synchronous listener calls some strange effects may occur if the synchronous
 * value change propagations contain cycles.
 * <p>
 * A proper solution to this problem would likely entail a big change / rethinking of
 * how livexps work and dispatch events.
 * <p>
 * In the mean time, this class can be used safely to track the value of a simple
 * LiveVariable and dispose its old value whenever a new value is assigned.
 *
 * @author Kris De Volder
 */
public class OldValueDisposer {

	private Disposable lastObservedValue = null;

	public OldValueDisposer(LiveExpression<? extends Disposable> target) {
		target.addListener((e, v) -> gotValue(v));
		target.onDispose((e) -> gotValue(null));
	}

	private synchronized void gotValue(Disposable v) {
		Disposable oldValue = lastObservedValue;
		lastObservedValue = v;
		//Take care with spurious change events! Ideally these shouldn't happen, but livexp isn't perfectly avoiding them!
		if (oldValue!=v) {
			disposeValue(oldValue);
		}
	}

	private void disposeValue(Disposable value) {
		if (value!=null) {
			value.dispose();
		}
	}

}
