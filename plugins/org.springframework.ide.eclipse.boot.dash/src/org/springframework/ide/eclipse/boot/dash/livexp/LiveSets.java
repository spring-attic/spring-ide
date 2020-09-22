/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;

import static org.springsource.ide.eclipse.commons.livexp.core.LiveSets.EMPTY_SET;

import org.springframework.ide.eclipse.boot.dash.model.ButtonModel;

/**
 * @author Kris De Volder
 */
public class LiveSets {

	/**
	 * @deprecated Use {@link org.springsource.ide.eclipse.commons.livexp.core.LiveSets#filter(ObservableSet<S>,Class<T>)} instead
	 */
	public static <S,T> ObservableSet<T> filter(final ObservableSet<S> source, final Class<T> retainType) {
		return org.springsource.ide.eclipse.commons.livexp.core.LiveSets.filter(source, retainType);
	}

	/**
	 * @deprecated Use {@link org.springsource.ide.eclipse.commons.livexp.core.LiveSets#singletonOrEmpty(LiveExpression<T>)} instead
	 */
	public static <T> ObservableSet<T> singletonOrEmpty(final LiveExpression<T> exp) {
		return org.springsource.ide.eclipse.commons.livexp.core.LiveSets.singletonOrEmpty(exp);
	}

	/**
	 * @deprecated Use {@link org.springsource.ide.eclipse.commons.livexp.core.LiveSets#map(ObservableSet<A>,AsyncMode,AsyncMode,Function<A, R>)} instead
	 */
	@SuppressWarnings("unchecked")
	public static <A,R> ObservableSet<R> map(ObservableSet<A> input, AsyncMode asyncRefresh, AsyncMode asyncEvents, Function<A, R> function) {
		return org.springsource.ide.eclipse.commons.livexp.core.LiveSets.map(input, asyncRefresh, asyncEvents,
				function);
	}

	/**
	 * Creates a {@link ObservableSet} by applying a mapping function to another ObservableSet.
	 * <p>
	 * The resulting set is synchronously updated when the input set changes.
	 */
	public static <A,R> ObservableSet<R> mapSync(ObservableSet<A> input, Function<A, R> function) {
		return org.springsource.ide.eclipse.commons.livexp.core.LiveSets.map(input, AsyncMode.SYNC, AsyncMode.SYNC, function);
	}

	/**
	 * Creates an observable, sorted set by applying a mapping function to each value of an ObservableSet of LiveExps.
	 */
	@SuppressWarnings("unchecked")
	public static <T, R extends Comparable<?>> ObservableSet<R> sortedMappedValues(ObservableSet<LiveExpression<T>> input, final Function<T,R> mappingFunction) {
		if (input==EMPTY_SET) {
			return EMPTY_SET;
		}
		return new MappedValuesSet<T, R>(input) {

			@Override
			protected R applyFun(T arg) {
				return mappingFunction.apply(arg);
			}

			@Override
			protected ImmutableSortedSet.Builder<R> immutableSetBuilder() {
				return ImmutableSortedSet.naturalOrder();
			}
		};
	}

}
