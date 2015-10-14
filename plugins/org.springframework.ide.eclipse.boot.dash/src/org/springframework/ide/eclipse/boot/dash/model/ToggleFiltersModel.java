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

import org.eclipse.core.resources.IProject;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;

/**
 * The 'toggle filters' are fixed set of view filters that can be toggled on/off by the user.
 * This model element tracks the currently selected 'toggle' filters and the corresponding composite
 * filter that results from composing them.
 *
 * @author Kris De Volder
 */
public class ToggleFiltersModel {

	private static final Filter<BootDashElement> HIDE_NON_WORKSPACE_ELEMENTS = new Filter<BootDashElement>() {
		public boolean accept(BootDashElement t) {
			if (t!=null) {
				IProject p = t.getProject();
				return p!=null && p.exists();
			}
			return false;
		}
	};
	private static final FilterChoice[] FILTERS = {
			new FilterChoice("Hide non-workspace elements", HIDE_NON_WORKSPACE_ELEMENTS)
	};

	public static class FilterChoice implements Ilabelable {
		private final String label;
		private final Filter<BootDashElement> filter;

		public FilterChoice(String label, Filter<BootDashElement> filter) {
			this.label = label;
			this.filter = filter;
		}

		@Override
		public String toString() {
			return "FilterChoice("+label+")";
		}

		@Override
		public String getLabel() {
			return label;
		}

		public Filter<BootDashElement> getFilter() {
			return filter;
		}

	}

	private LiveSet<FilterChoice> selectedFilters = new LiveSet<FilterChoice>();
	private LiveExpression<Filter<BootDashElement>> compositeFilter;
	{
		final Filter<BootDashElement> initial = Filters.acceptAll();
		compositeFilter = new LiveExpression<Filter<BootDashElement>>() {
			{
				dependsOn(selectedFilters);
			}
			@Override
			protected Filter<BootDashElement> compute() {
				Filter<BootDashElement> composed = initial;
				for (FilterChoice chosen : selectedFilters.getValues()) {
					composed = Filters.compose(composed, chosen.getFilter());
				}
				return composed;
			}
		};
	}
	/**
	 * @return The filter that is defined by composing all the selected toggle filters.
	 */
	public LiveExpression<Filter<BootDashElement>> getFilter() {
		return compositeFilter;
	}
	public FilterChoice[] getAvailableFilters() {
		return FILTERS;
	}
	public LiveSet<FilterChoice> getSelectedFilters() {
		return selectedFilters;
	}

}
