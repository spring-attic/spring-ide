/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support.internal.support;

import org.springframework.util.Assert;

/**
 * Wrapper retry template. This class does specialized retries using a given
 * callback and lock.
 * 
 * @author Costin Leau
 */
public class RetryTemplate {

	private static final int hashCode = RetryTemplate.class.hashCode() * 13;

	public static final long DEFAULT_WAIT_TIME = 1000;

	private final Object monitor = new Object();
	private final Object notificationLock;

	private long waitTime = DEFAULT_WAIT_TIME;

	// wait threshold (in millis)
	private static final long WAIT_THRESHOLD = 3;


	public RetryTemplate(long waitTime, Object notificationLock) {
		Assert.isTrue(waitTime >= 0, "waitTime must be positive");
		Assert.notNull(notificationLock, "notificationLock must be non null");

		synchronized (monitor) {
			this.waitTime = waitTime;
			this.notificationLock = notificationLock;
		}
	}

	public RetryTemplate(Object notificationLock) {
		this(DEFAULT_WAIT_TIME, notificationLock);
	}

	/**
	 * Main retry method. Executes the callback until it gets completed. The
	 * callback will get executed the number of {@link #retryNumbers} while
	 * waiting in-between for the {@link #DEFAULT_WAIT_TIME} amount.
	 * 
	 * Before bailing out, the callback will be called one more time. Thus, in
	 * case of being unsuccessful, the default value of the callback is
	 * returned.
	 * 
	 * @param callback
	 * @return
	 */
	public <T> T execute(RetryCallback<T> callback) {
		long waitTime;

		synchronized (monitor) {
			waitTime = this.waitTime;
		}

		boolean retry = false;

		long initialStart = 0, start = 0, stop = 0;
		long waitLeft = waitTime;

		boolean startWaiting = false;

		do {
			T result = callback.doWithRetry();

			if (callback.isComplete(result)) {

				if (startWaiting) {
					callbackSucceeded(stop);
				}
				return result;
			}

			if (!startWaiting) {
				startWaiting = true;
				onMissingTarget();
				// initial wait
				initialStart = System.currentTimeMillis();
			}

			if (waitLeft > 0) {
				try {
					start = System.currentTimeMillis();
					synchronized (notificationLock) {
						// Do NOT use Thread.sleep() here - it does not release
						// locks.
						notificationLock.wait(waitTime);
					}
					// local wait timer
					stop = System.currentTimeMillis();
					waitLeft -= (stop - start);
					// total wait timer
					stop -= initialStart;
				}
				catch (InterruptedException ex) {
					stop = System.currentTimeMillis() - initialStart;
					callbackFailed(stop);
					throw new RuntimeException("Retry failed; interrupted while waiting", ex);
				}
			}

			retry = false;

			// handle reset cases
			synchronized (monitor) {
				// has there been a reset in place ?
				if (waitTime != this.waitTime) {
					// start counting again
					retry = true;
					waitTime = this.waitTime;
					waitLeft = waitTime;
				}
			}
		} while (retry || waitLeft > WAIT_THRESHOLD);

		T result = callback.doWithRetry();
		stop = System.currentTimeMillis() - initialStart;

		if (callback.isComplete(result)) {
			callbackSucceeded(stop);
			return result;
		}
		else {
			callbackFailed(stop);
			return null;
		}
	}

	/**
	 * Template method invoked if the backing service is missing.
	 */
	protected void onMissingTarget() {
	}

	/**
	 * Template method invoked when the retry succeeded.
	 * 
	 * @param stop the time it took to execute the call (including waiting for
	 *        the service)
	 */
	protected void callbackSucceeded(long stop) {
	}

	/**
	 * Template method invoked when the retry has failed.
	 * 
	 * @param stop the time it took to execute the call (including waiting for
	 *        the service)
	 */
	protected void callbackFailed(long stop) {
	}

	/**
	 * Reset the retry template, by applying the new values. Any in-flight
	 * waiting is interrupted and restarted using the new values.
	 * 
	 * @param retriesNumber
	 * @param waitTime
	 */
	public void reset(long waitTime) {
		synchronized (monitor) {
			this.waitTime = waitTime;
		}

		synchronized (notificationLock) {
			notificationLock.notifyAll();
		}
	}

	public long getWaitTime() {
		synchronized (monitor) {
			return waitTime;
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof RetryTemplate) {
			RetryTemplate oth = (RetryTemplate) other;

			return (getWaitTime() == oth.getWaitTime());
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}