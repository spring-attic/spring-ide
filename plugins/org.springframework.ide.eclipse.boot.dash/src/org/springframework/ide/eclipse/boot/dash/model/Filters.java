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
package org.springframework.ide.eclipse.boot.dash.model;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Helper methods for creating common filters.
 *
 * @author Kris De Volder
 */
public class Filters {

	@SuppressWarnings("rawtypes")
	private static final Filter ACCEPT_ALL = new Filter() {
		public boolean accept(Object t) {
			return true;
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> Filter<T> acceptAll() {
		return ACCEPT_ALL;
	}

	public static <T> Filter<T> compose(final Filter<T> f1, final Filter<T> f2) {
		if (f1==ACCEPT_ALL) {
			return f2;
		} else if (f2==ACCEPT_ALL) {
			return f1;
		}
		return new Filter<T>() {
			public boolean accept(T t) {
				return f1.accept(t) && f2.accept(t);
			}

		};
	}

	public static <T> LiveExpression<Filter<T>> compose(final LiveExpression<Filter<T>> f1, final LiveExpression<Filter<T>> f2) {
		final Filter<T> initial = acceptAll();
		return new LiveExpression<Filter<T>>(initial) {
			{
				dependsOn(f1);
				dependsOn(f2);
			}
			protected Filter<T> compute() {
				return Filters.compose(f1.getValue(), f2.getValue());
			}
		};
	}
}
