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
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

import org.springframework.ide.eclipse.boot.dash.model.Filter;

/**
 * Model for a 'filter' box.
 *
 * @author Kris De Volder
 */
public abstract class FilterBoxModel<T> {

	/**
	 * Represents the text widget where user can type something.
	 */
	private final LiveVariable<String> text = new LiveVariable<String>("");

	private LiveExpression<Filter<T>> filter = null; // lazy created

	public synchronized LiveExpression<Filter<T>> getFilter() {
		if (filter==null) {
			Filter<T> initialFilter = Filters.acceptAll();
			filter = new LiveExpression<Filter<T>>(initialFilter) {
				protected Filter<T> compute() {
					return createFilterForInput(text.getValue());
				}
			};
		}
		filter.dependsOn(text);
		return filter;
	}

	public LiveVariable<String> getText() {
		return text;
	}


	protected abstract Filter<T> createFilterForInput(String text);
}
