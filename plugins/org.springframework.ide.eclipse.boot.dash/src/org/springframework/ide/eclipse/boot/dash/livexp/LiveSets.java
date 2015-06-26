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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * @author Kris De Volder
 */
public class LiveSets {

	@SuppressWarnings("rawtypes")
	private static final LiveExpression EMPTY_SET = LiveExpression.constant(Collections.EMPTY_SET);

	@SuppressWarnings("unchecked")
	public static <T> LiveExpression<Set<T>> emptySet(Class<T> t) {
		return EMPTY_SET;
	}

	public static <T> LiveExpression<Set<T>> union(LiveExpression<Set<T>> e1, LiveExpression<Set<T>> e2) {
		if (e1==EMPTY_SET) {
			return e2;
		} else if (e2==EMPTY_SET) {
			return e1;
		} else {
			return new LiveUnion<T>(e1, e2);
		}
	}

	//////////////////////////////////////////////////////////////////////

	private static class LiveUnion<T> extends LiveExpression<Set<T>> {

		private LiveExpression<Set<T>> e1;
		private LiveExpression<Set<T>> e2;

		@SuppressWarnings("unchecked")
		public LiveUnion(LiveExpression<Set<T>> e1, LiveExpression<Set<T>> e2) {
			super((Set<T>)Collections.emptySet());
			this.e1 = e1;
			this.e2 = e2;
			this.dependsOn(e1);
			this.dependsOn(e2);
		}

		@Override
		protected Set<T> compute() {
			Set<T> s1 = e1.getValue();
			Set<T> s2 = e2.getValue();
			int estimatedSize = s1.size() + s2.size();
			HashSet<T> union = new HashSet<T>(estimatedSize);
			union.addAll(s1);
			union.addAll(s2);
			return union;
		}

	}

	public static <S,T> LiveExpression<Set<T>> filter(final LiveExpression<Set<S>> source, final Class<T> retainType) {
		ObservableSet<T> filtered = new ObservableSet<T>() {
			@SuppressWarnings("unchecked")
			@Override
			protected Set<T> compute() {
				Set<S> sourceElements = source.getValue();
				Set<T> targetElements = new HashSet<T>();
				for (S s : sourceElements) {
					if (retainType.isAssignableFrom(s.getClass())) {
						targetElements.add((T) s);
					}
				}
				return targetElements;
			}
		};
		filtered.dependsOn(source);
		return filtered;
	}

	public static <T> LiveExpression<Set<T>> singletonOrEmpty(final LiveExpression<T> exp) {
		return new ObservableSet<T>() {
			{
				dependsOn(exp);
			}
			protected Set<T> compute() {
				T val = exp.getValue();
				if (val==null) {
					return Collections.emptySet();
				} else {
					return Collections.singleton(val);
				}
			}
		};
	}



}
