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
import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.Cache;
import org.springframework.ide.eclipse.boot.properties.editor.util.LimitedTimeCache;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Timer;
import reactor.core.tuple.Tuple;
import reactor.core.tuple.Tuple2;

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
public abstract class CachingValueProvider implements ValueProviderStrategy {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	{
		Timer.global(); //TODO: this shouldn't be needed in later release of reactor. Reactor version *after* 2.5.0.M3 should
						// create and use global timer automatically.
	}

	private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(2000);

	/**
	 * Content assist is called inside UI thread and so doing something lenghty things
	 * like a JavaSearch will block the UI thread completely freezing the UI. So, we
	 * only return as many results as can be obtained within this hard TIMEOUT limit.
	 */
	public static Duration TIMEOUT = DEFAULT_TIMEOUT;

	/**
	 * The maximum number of results returned for a single request. Used to limit the
	 * values that are cached per entry.
	 */
	private int MAX_RESULTS = 500;

	private Cache<Tuple2<String,String>, CacheEntry> cache = createCache();

	private class CacheEntry {
		boolean isComplete = false;
		Flux<ValueHint> values;

		public CacheEntry(String query, Flux<ValueHint> producer) {
			values = producer
			.doOnComplete(() -> {
				debug("Complete: "+query);
				isComplete = true;
			})
			.take(MAX_RESULTS)
			.log("before caching")
			.cache(MAX_RESULTS);
			values.subscribe(); // create infite demand so that we actually force cache entries to be fetched upto the max.
		}
	}

	@Override
	public final Collection<ValueHint> getValues(IJavaProject javaProject, String query) {
		Tuple2<String, String> key = key(javaProject, query);
		CacheEntry cached = cache.get(key);
		if (cached==null) {
			cache.put(key, cached = new CacheEntry(query, getValuesIncremental(javaProject, query)));
		}
		return cached.values.take(TIMEOUT).toList().get();
	}

	/**
	 * Tries to use an already cached, complete result for a query that is a prefix of the current query to speed things up.
	 * <p>
	 * Falls back on doing a full-blown search if there's no usable 'prefix-query' in the cache.
	 */
	private Flux<ValueHint> getValuesIncremental(IJavaProject javaProject, String query) {
		String subquery = query;
		while (subquery.length()>=1) {
			subquery = subquery.substring(0, subquery.length()-1);
			CacheEntry cached = cache.get(key(javaProject, subquery));
			if (cached!=null && cached.isComplete) {
				return cached.values.filter((hint) -> 0!=FuzzyMatcher.matchScore(query, hint.getValue().toString()));
			}
		}
		return getValuesAsycn(javaProject, query);
	}

	protected abstract Flux<ValueHint> getValuesAsycn(IJavaProject javaProject, String query);

	private Tuple2<String,String> key(IJavaProject javaProject, String query) {
		return Tuple.of(javaProject==null?null:javaProject.getElementName(), query);
	}

	protected <K,V> Cache<K,V> createCache() {
		return new LimitedTimeCache<>(Duration.ofMinutes(5));
	}

	public static void restoreDefaults() {
		TIMEOUT = DEFAULT_TIMEOUT;
	}

}
