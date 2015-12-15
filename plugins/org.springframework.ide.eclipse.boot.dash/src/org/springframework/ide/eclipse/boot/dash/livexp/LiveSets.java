/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Kris De Volder
 */
public class LiveSets {

	@SuppressWarnings("rawtypes")
	private static final ObservableSet EMPTY_SET = (ObservableSet) ObservableSet.constant(ImmutableSet.of());

	@SuppressWarnings("unchecked")
	public static <T> ObservableSet<T> emptySet(Class<T> t) {
		return EMPTY_SET;
	}

	public static <T> ObservableSet<T> union(ObservableSet<T> e1, ObservableSet<T> e2) {
		if (e1==EMPTY_SET) {
			return e2;
		} else if (e2==EMPTY_SET) {
			return e1;
		} else {
			return new LiveUnion<T>(e1, e2);
		}
	}

	//////////////////////////////////////////////////////////////////////

	private static class LiveUnion<T> extends ObservableSet<T> {

		private ObservableSet<T> e1;
		private ObservableSet<T> e2;

		public LiveUnion(ObservableSet<T> e1, ObservableSet<T> e2) {
			this.e1 = e1;
			this.e2 = e2;
			this.dependsOn(e1);
			this.dependsOn(e2);
		}

		@Override
		protected ImmutableSet<T> compute() {
			return ImmutableSet.copyOf(Sets.union(e1.getValue(), e2.getValue()));
		}
	}

	public static <S,T> ObservableSet<T> filter(final ObservableSet<S> source, final Class<T> retainType) {
		ObservableSet<T> filtered = new ObservableSet<T>() {
			@SuppressWarnings("unchecked")
			@Override
			protected ImmutableSet<T> compute() {
				return (ImmutableSet<T>) ImmutableSet.copyOf(
					Sets.filter(source.getValue(), new Predicate<S>() {
						@Override
						public boolean apply(S input) {
							return retainType.isAssignableFrom(input.getClass());
						}
					})
				);
			}
		};
		filtered.dependsOn(source);
		return filtered;
	}

	public static <T> ObservableSet<T> singletonOrEmpty(final LiveExpression<T> exp) {
		return new ObservableSet<T>() {
			{
				dependsOn(exp);
			}
			protected ImmutableSet<T> compute() {
				T val = exp.getValue();
				if (val==null) {
					return ImmutableSet.of();
				} else {
					return ImmutableSet.of(val);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <A,R> ObservableSet<R> map(ObservableSet<A> input, Function<A, R> function) {
		if (input==EMPTY_SET) {
			return EMPTY_SET;
		}
		return new MapSet<>(input, function);
	}



}
