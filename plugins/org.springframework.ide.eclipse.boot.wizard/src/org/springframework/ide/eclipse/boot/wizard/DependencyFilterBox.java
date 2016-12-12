/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

public class DependencyFilterBox extends FilterBoxModel<CheckBoxModel<Dependency>> {

	@Override
	protected Filter<CheckBoxModel<Dependency>> createFilterForInput(String _text) {
		if (StringUtils.isNotBlank(_text)) {
			final String text = _text.toLowerCase();
			return new Filter<CheckBoxModel<Dependency>>() {
				public boolean accept(CheckBoxModel<Dependency> cb) {
					return isTrue(cb.getSelection())
						|| matches(text, cb.getLabel())
						|| matches(text, cb.getValue());
				}
			};
		}
		return Filters.acceptAll();
	}

	protected boolean matches(String pattern, Dependency dep) {
		return matches(pattern, dep.getName())
			|| matches(pattern, dep.getDescription());
	}

	protected boolean matches(String pattern, String text) {
		if (text!=null) {
			return text.toLowerCase().contains(pattern);
		}
		return false;
	}

	private static boolean isTrue(LiveVariable<Boolean> selection) {
		Boolean v = selection.getValue();
		return v!=null && v;
	}

}
