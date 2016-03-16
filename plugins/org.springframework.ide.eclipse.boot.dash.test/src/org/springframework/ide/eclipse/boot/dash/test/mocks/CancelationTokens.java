package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.test.mocks.CancelationTokens.CancelationToken;

/**
 * Manages a set of CancelationTokens.
 *
 * @author Kris De Volder
 */
public class CancelationTokens {

	/**
	 * Holds on to all 'active' tokens. A token is active from the time it
	 * is created until the time it is either canceled or disposed.
	 */
	private Set<CancelationToken> tokens = new HashSet<>();

	public synchronized CancelationToken create() {
		CancelationToken token = new CancelationToken();
		tokens.add(token);
		return token;
	}

	public class CancelationToken {
		private boolean isCanceled = false;
		private boolean isDisposed = false;

		private CancelationToken() {}

		public boolean isCanceled() {
			synchronized (tokens) {
				return isCanceled;
			}
		}

		public void cancel() {
			synchronized (tokens) {
				Assert.isLegal(!isDisposed); //Disposed tokens can not be canceled anymore.
				this.isCanceled = true;
				dispose();
			}
		}

		public void dispose() {
			synchronized (tokens) {
				isDisposed = true;
				remove(this);
			}
		}
	}

	private void remove(CancelationToken cancelationToken) {
		tokens.remove(cancelationToken);
	}

	public synchronized void cancelAll() {
		for (CancelationToken t : tokens) {
			t.cancel();
		}
	}
}
