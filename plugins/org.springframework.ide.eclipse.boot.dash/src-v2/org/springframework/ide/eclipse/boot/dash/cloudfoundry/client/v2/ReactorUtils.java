package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import org.springframework.ide.eclipse.boot.core.BootActivator;

import reactor.core.publisher.Mono;

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

}
