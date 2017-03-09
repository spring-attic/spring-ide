/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.livexp.ui;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * A {@link LiveExpression} which is defined as mirroring the
 * contents of a delegate.
 * <p>
 * This delegate is initially null and can be set repeatedly.
 * <p>
 * When delegate is null the valud of the {@link DelegatingLiveExp} will also be null.
 * When delegate points to non-null {@link LiveExpression} then its value will be
 * equal to the value of the delegate.
 *
 * @author Kris De Volder
 */
public class DelegatingLiveExp<T> extends LiveExpression<T> {

	public DelegatingLiveExp() {}

	private LiveExpression<T> delegate = null;
	private ValueListener<T> delegateListener = (exp, value) -> refresh();

	@Override
	protected T compute() {
		if (delegate==null) {
			return null;
		} else {
			return delegate.getValue();
		}
	}

	public synchronized void setDelegate(LiveExpression<T> newDelegate) {
		LiveExpression<T> oldDelegate = this.delegate;
		this.delegate = newDelegate;
		if (oldDelegate==newDelegate) {
			return;
		} else {
			if (oldDelegate!=null) {
				oldDelegate.removeListener(delegateListener);
			}
			if (newDelegate==null) {
				//trigger a refresh because the delegate changed and the newDelegate won't trigger one
				refresh();
			} else {
				newDelegate.addListener(delegateListener);
			}
		}
	}

}
