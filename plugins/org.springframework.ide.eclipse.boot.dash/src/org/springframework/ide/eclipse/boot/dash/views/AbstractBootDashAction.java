/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class AbstractBootDashAction extends Action {

	private final MultiSelection<BootDashElement> selection;
	protected final UserInteractions ui;

	public AbstractBootDashAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		this.selection = selection;
		this.ui = ui;
		selection.getElements().addListener(new ValueListener<Set<BootDashElement>>() {
			public void gotValue(LiveExpression<Set<BootDashElement>> exp,
					Set<BootDashElement> selecteds) {
				updateEnablement();
			}
		});
	}

	/**
	 * Subclass can override to compuet enablement differently.
	 * The default implementation enables if a single element is selected.
	 */
	public void updateEnablement() {
		Collection<BootDashElement> selecteds = getSelectedElements();
		this.setEnabled(selecteds.size()==1);
	}

	protected Collection<BootDashElement> getSelectedElements() {
		return selection.getValue();
	}

	public void dispose() {
	}
}
