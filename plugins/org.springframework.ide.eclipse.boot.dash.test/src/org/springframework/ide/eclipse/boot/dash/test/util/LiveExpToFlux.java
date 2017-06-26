/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.util;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import reactor.core.publisher.Flux;

public class LiveExpToFlux {

	public static <T> Flux<T> toFlux(LiveExpression<T> exp) {
		return Flux.create(sink -> {
			ValueListener<T> valueListener = (e, v) -> {
				if (v!=null) {
					sink.next(v);
				}
			};
			sink.onDispose(() -> exp.removeListener(valueListener));
			exp.addListener(valueListener);
			exp.onDispose((d) -> sink.complete());
		});
	}

}
