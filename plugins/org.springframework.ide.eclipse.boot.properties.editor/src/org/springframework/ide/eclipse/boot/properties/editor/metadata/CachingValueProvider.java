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
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.Cache;
import org.springframework.ide.eclipse.boot.properties.editor.util.LimitedTimeCache;

/**
 * A abstract {@link ValueProviderStrategy} that is mean to help speedup successive invocations of
 * content assist with a similar 'query' string.
 * <p>
 * This implementation is meant to be used for providers that use potentially lenghty/expensive searches
 * to determine hints. Since content assist hints are requested by Eclipse CA framework directly on
 * the UI thread, they can not simply perform a lengthy search and block UI thread until it finished.
 * <p>
 * This implementation therefore does the following:
 * <ul>
 *   <li>Limit the duration of time spent on the UI thread.
 *   <li>Cache results of searches for a limited time.
 *   <li>When the time spent on UI thread waiting for a current search exceeds the allowed time limit,
 *       return immediately with whatever results have been found so far.
 * </ul>
 *
 *
 * @author Kris De Volder
 *
 */
public class CachingValueProvider implements ValueProviderStrategy {

	public interface HintCollector {
		void next(ValueHint hint);
		void complete();
	}

	public class ResultsBucket implements HintCollector {
		Collection<ValueHint> collected = new ArrayList<>();
		boolean complete = false;

		@Override
		public synchronized void next(ValueHint hint) {
			collected.add(hint);
		}

		@Override
		public synchronized void complete() {
			complete = true;
		}
	}

	private Cache<String, ResultsBucket> cache = createCache();

	@Override
	public Collection<ValueHint> getValues(IJavaProject javaProject, String query) {
		return null;
	}

	protected <V> Cache<String, V> createCache() {
		return new LimitedTimeCache(Duration.ofMinutes(10));
	}

}
