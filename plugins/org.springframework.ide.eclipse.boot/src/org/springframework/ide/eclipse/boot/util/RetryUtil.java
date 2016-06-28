/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Helper class to implement simple retry logic that allows retrying a task
 * a set number of times at an interval until it succeeds or until the timelimit
 * is exceeded.
 *
 * @author Kris De Volder
 */
public class RetryUtil {

	public static void retryWhen(String name, int tries, Predicate<Throwable> when, Thunk task) throws Exception {
		boolean success = false;
		Throwable error = null;
		while (!success && tries>0) {
			tries--;
			try {
				task.call();
				success = true;
			} catch (Throwable e) {
				error = e;
				if (name!=null) {
					if (tries>0 && when.test(e)) {
						System.out.println(name+" failed: "+ExceptionUtil.getMessage(e));
						System.out.println("Retrying!");
					}
				}
			}
		}
		if (!success) {
			throw ExceptionUtil.exception(error);
		}
	}

	public static void retryTimes(String name, int tries, Thunk task) throws Exception {
		retryWhen(name, tries, (e) -> true, task);
	}

	/**
	 * Call a given callable. If it throws then we retry it after a given interval.
	 * We keep retrying it periodically until either the call completes successfully
	 * or the timelimit is exceeded.
	 * <p>
	 * If the time limit is exceeded without reaching a succesful call, the last thrown
	 * exception is rethrown.
	 */
	public static <T> T retry(long interval, long timelimit, Callable<T> task) throws Exception {
		T result = null;
		boolean success = false;
		Throwable error = null;
		long endTime = System.currentTimeMillis() + timelimit;
		do {
			try {
				result = task.call();
				success = true;
			} catch (Throwable e) {
				error = e;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException ignore) {
				}
			}
		} while (!success && System.currentTimeMillis() < endTime);
		if (success) {
			return result;
		} else {
			if (error instanceof Exception) {
				throw (Exception)error;
			} else {
				throw new InvocationTargetException(error);
			}
		}
	}

	public static Predicate<Throwable> errorWithMsg(String msgFrag) {
		return (error) -> {
			String msg = ExceptionUtil.getMessage(error);
			return msg.contains(msgFrag);
		};
	}

}
