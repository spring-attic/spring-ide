/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.time.Duration;

import org.springframework.ide.eclipse.boot.core.BootActivator;

import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 */
public class ReactorUtils {

	/**
	 * Similar to Mono.get but logs a more traceable version of the exception to Eclipse's error
	 * log before 'rethrowing' it.
	 * <p>
	 * This is useful because the actual exception is pretty hard to trace. It doesn't even 'point'
	 * to the line where 'get' was called.
	 */
	public static <T> T get(Mono<T> mono) throws Exception {
		try {
			return mono.get();
		} catch (Exception e) {
			BootActivator.log(new Exception(e));
			throw e;
		}
	}

	/**
	 * Similar to Mono.get but logs a more traceable version of the exception to Eclipse's error
	 * log before 'rethrowing' it.
	 * <p>
	 * This is useful because the actual exception is pretty hard to trace. It doesn't even 'point'
	 * to the line where 'get' was called.
	 */
	public static <T> T get(Duration timeout, Mono<T> mono) {
		try {
			return mono.get(timeout);
		} catch (Exception e) {
			BootActivator.log(new Exception(e));
			throw e;
		}
	}
}
