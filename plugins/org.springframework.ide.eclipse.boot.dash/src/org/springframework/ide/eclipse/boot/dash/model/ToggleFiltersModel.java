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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

import com.google.common.collect.ImmutableSet;

/**
 * The 'toggle filters' are fixed set of view filters that can be toggled on/off by the user.
 * This model element tracks the currently selected 'toggle' filters and the corresponding composite
 * filter that results from composing them.
 *
 * @author Kris De Volder
 */
public class ToggleFiltersModel {

	private static final Filter<BootDashElement> HIDE_SOLITARY_CONFS = new Filter<BootDashElement>() {
		public boolean accept(BootDashElement e) {
			if (e instanceof LaunchConfDashElement) {
				LaunchConfDashElement conf = (LaunchConfDashElement) e;
				return conf.getParent().getCurrentChildren().size()!=1;
			}
			return true;
		}
	};

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
			new FilterChoice("Hide non-workspace elements", HIDE_NON_WORKSPACE_ELEMENTS),
			new FilterChoice("Hide solitary launch configs", HIDE_SOLITARY_CONFS, true)
	};

	public static class FilterChoice implements Ilabelable {
		private final String label;
		private final Filter<BootDashElement> filter;
		private final boolean defaultEnable;

		public FilterChoice(String label, Filter<BootDashElement> filter) {
			this(label, filter, false);
		}

		public FilterChoice(String label, Filter<BootDashElement> filter, boolean defaultEnable) {
			this.label = label;
			this.filter = filter;
			this.defaultEnable = defaultEnable;
		}

		@Override
		public String toString() {
			return "FilterChoice("+getLabel()+")";
		}

		@Override
		public String getLabel() {
			return label;
		}

		public Filter<BootDashElement> getFilter() {
			return filter;
		}

	}

	private LiveSetVariable<FilterChoice> selectedFilters = new LiveSetVariable<FilterChoice>(getDefaultFilters());
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
	public LiveSetVariable<FilterChoice> getSelectedFilters() {
		return selectedFilters;
	}

	private Set<FilterChoice> getDefaultFilters() {
		Set<FilterChoice> builder = new HashSet<>();
		for (FilterChoice f : FILTERS) {
			if (f.defaultEnable) {
				builder.add(f);
			}
		}
		return builder;
	}
}
