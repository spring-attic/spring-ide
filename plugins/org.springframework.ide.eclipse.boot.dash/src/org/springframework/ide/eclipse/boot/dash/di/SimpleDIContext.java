/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.di;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;

/**
 * A very simple DI framework.
 * <p>
 * Can we use something else? Maybe guice, spring, or the DI framework built-into Eclipse 4?
 */
public class SimpleDIContext {

	public interface BeanFactory<T> {
		T create(SimpleDIContext context) throws Exception;
	}

	private static final class Definition<T> {
		private final Class<T> type;
		private final BeanFactory<T> factory;
		private CompletableFuture<T> instance;
		public Definition(Class<T> type, BeanFactory<T> factory) {
			super();
			this.type = type;
			this.factory = factory;
		}
		public boolean satisfies(Class<?> requested) {
			return requested.isAssignableFrom(type);
		}
		public synchronized T get(SimpleDIContext context) throws Exception {
			if (instance==null) {
				instance = new CompletableFuture<>();
				try {
					instance.complete(factory.create(context));
				}catch (Throwable e) {
					instance.completeExceptionally(e);
				}
			}
			return instance.get();
		}
	}

	private Map<Class<?>, Definition<?>> resolveCache = new HashMap<>();

	private List<Definition<?>> definitions = new ArrayList<>();

	public <T> void def(Class<T> type, BeanFactory<T> factory) {
		definitions.add(new Definition<>(type, factory));
	}

	public synchronized <T> T getBean(Class<T> type) throws Exception {
		lockdown();
		return resolveDefinition(type).get(this);
	}

	@SuppressWarnings("unchecked")
	protected <T> Definition<T> resolveDefinition(Class<T> type) {
		//TODO: cache instance definition resolution?
		for (int i = definitions.size()-1; i>=0; i--) {
			Definition<?> d = definitions.get(i);
			if (d.satisfies(type)) {
				return (Definition<T>) d;
			}
		}
		throw new IllegalStateException("No definition for bean of type "+type);
	}

	/**
	 * Prevents additional definitions from being added. The idea is that using an injection
	 * context proceeds in two separate stages. Stage 1 initializes the context with bean definitions.
	 * Stage 2 allows a client to request beans that are then created on demand. Once stage 2 is
	 * started, which happens automatically when the first bean is requested, the context becomes
	 * immmutable and no longer allows adding definitions.
	 */
	private void lockdown() {
		definitions = ImmutableList.copyOf(definitions);
	}

}
