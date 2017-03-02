/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

import com.google.common.collect.ImmutableSet;

public class SelectedDependenciesSection extends FilteredDependenciesSection {

	public SelectedDependenciesSection(IPageWithSections owner, NewSpringBootWizardModel model) {
		super(owner, model, createFilter(model));
	}

	private static LiveExpression<Filter<Dependency>> createFilter(NewSpringBootWizardModel model) {
		LiveExpression<Filter<Dependency>> filter = new LiveExpression<Filter<Dependency>>() {

			@Override
			protected Filter<Dependency> compute() {
				ImmutableSet<Dependency> currentSelection = ImmutableSet
						.copyOf(model.dependencies.getCurrentSelection());
				return (dependency) -> currentSelection.contains(dependency);
			}
		};

		ValueListener<Boolean> selectionListener = (exp, val) -> {
			filter.refresh();
		};
		
		for (String cat : model.dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
			dependencyGroup.addSelectionListener(selectionListener);
		}

		return filter;
	}
}
