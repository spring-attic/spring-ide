/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

/**
 * Manages a set of CancelationTokens.
 *
 * @author Kris De Volder
 */
public class CancelationTokens {

	//Note: we don't actually have to keep a set of tokens explicitly.
	// The tokens use a 'id' which is incremented on each new token.
	//So it is easy to cancel all existing tokens based on a their
	//if simply by remembering the 'watermark id' where the cancelation
	//occurred.

	private final Object SYNC = CancelationTokens.this;

	private int canceledAllBefore = 0;
	private int nextId = 0;

	public interface CancelationToken {
		boolean isCanceled();
	}

	public synchronized CancelationToken create() {
		CancelationToken token = new ManagedToken();
		return token;
	}

	private class ManagedToken implements CancelationToken {
		private int id;

		private ManagedToken() {
			synchronized (SYNC) {
				this.id = ++nextId;
			}
		}

		public boolean isCanceled() {
			synchronized (SYNC) {
				return id < canceledAllBefore;
			}
		}

	}

	public synchronized void cancelAll() {
		canceledAllBefore = nextId-1;
	}
}
