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

import java.io.IOException;
import java.time.Duration;
import java.util.function.Function;

import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;

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
			return mono.get(Duration.ofMinutes(2));
		} catch (Exception e) {
//			BootActivator.log(new Exception(e));
			throw new IOException(e);
		}
	}

	/**
	 * Similar to Mono.get but logs a more traceable version of the exception to Eclipse's error
	 * log before 'rethrowing' it.
	 * <p>
	 * This is useful because the actual exception is pretty hard to trace. It doesn't even 'point'
	 * to the line where 'get' was called.
	 */
	public static <T> T get(Duration timeout, Mono<T> mono) throws Exception {
		try {
			return mono.get(timeout);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static <T> Mono<T> just(T it) {
		return it == null ? Mono.empty() : Mono.just(it);
	}

	/**
	 * @return A function that can be passed to Mono.otherwise to convert a specific exception type into
	 * Mono.empty().
	 */
	public static <T> Function<Throwable, Mono<T>> suppressException(Class<? extends Throwable> exceptionType) {
		return (Throwable caught) -> {
			if (exceptionType.isAssignableFrom(caught.getClass())) {
				return Mono.empty();
			} else {
				return Mono.error(caught);
			}
		};
	}
}
