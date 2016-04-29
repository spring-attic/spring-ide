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
import java.util.concurrent.CancellationException;
import java.util.function.Function;

import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 */
public class ReactorUtils {

	/**
	 * Convert a {@link CancelationToken} into a Mono that raises
	 * an {@link OperationCanceledException} when the token is canceled.
	 */
	public static <T> Mono<T> toMono(CancelationToken cancelToken) {
		return Mono.delay(Duration.ofSeconds(1))
		.then((ping) ->
			cancelToken.isCanceled()
				? Mono.<T>error(new OperationCanceledException())
				: Mono.empty()
		)
		.repeatWhenEmpty((x) -> x);
	}

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
	public static <T> T get(Duration timeout, CancelationToken cancelationToken, Mono<T> mono) throws Exception {
		return Mono.any(mono, toMono(cancelationToken))
		.otherwise(errorFilter(cancelationToken))
		.get(timeout);
	}

	/**
	 * A 'filter' to use as a Mono.otherwise hanlder. It transforms any exception into {@link OperationCanceledException}
	 * when cancelationToken has been canceled.
	 */
	private static <T> Function<Throwable, Mono<T>> errorFilter(CancelationToken cancelationToken) {
		return (Throwable e) -> cancelationToken.isCanceled()?Mono.error(new OperationCanceledException()):Mono.error(e);
	}

	/**
	 * Deprecated because this is really the same as Mono.justOrEmpty, so use that instead.
	 */
	@Deprecated
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

	/**
	 * Build a Mono<Void> that executes a given number of Mono<Void> one after the
	 * other.
	 */
	@SafeVarargs
	public static Mono<Void> sequence(Mono<Void>... tasks) {
		Mono<Void> seq = Mono.empty();
		for (Mono<Void> t : tasks) {
			seq = seq.after(t);
		}
		return seq;
	}
}
