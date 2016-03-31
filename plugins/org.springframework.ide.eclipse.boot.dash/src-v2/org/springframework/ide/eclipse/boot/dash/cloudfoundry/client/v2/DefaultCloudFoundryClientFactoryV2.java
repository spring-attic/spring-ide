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

import java.util.function.Function;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import reactor.core.publisher.Mono;

public class DefaultCloudFoundryClientFactoryV2 extends CloudFoundryClientFactory {

	private static <T> Function<Mono<T>, Mono<T>> debugMono(String msg) {
		return (mono) -> {
			return mono
			.then((value) -> {
				System.out.println(msg+" => "+value);
				return Mono.just(value);
			})
			.otherwise((error) -> {
				System.out.println(msg+" ERROR => "+ ExceptionUtil.getMessage(error));
				return Mono.error(error);
			});
		};
	}

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return new DefaultClientRequestsV2(params);
	}


}
