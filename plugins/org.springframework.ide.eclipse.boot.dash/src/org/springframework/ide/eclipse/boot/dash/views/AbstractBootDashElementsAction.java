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

import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Abstract super class for BootDash actions that operate on selections
 * of elements.
 *
 * @author Kris De Volder
 */
public class AbstractBootDashElementsAction extends AbstractBootDashAction {

	private final MultiSelection<BootDashElement> selection;
	private ValueListener<Set<BootDashElement>> selectionListener;

	public AbstractBootDashElementsAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(ui);
		this.selection = selection;
		selection.getElements().addListener(selectionListener = new ValueListener<Set<BootDashElement>>() {
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

	protected BootDashElement getSingleSelectedElement() {
		return selection.getSingle();
	}

	public void dispose() {
		if (selectionListener!=null) {
			selection.getElements().removeListener(selectionListener);
		}
	}
}
