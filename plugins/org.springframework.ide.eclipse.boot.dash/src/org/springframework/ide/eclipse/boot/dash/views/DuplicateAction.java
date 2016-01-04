/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Duplicatable;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * @author Kris De Volder
 */
public class DuplicateAction extends AbstractBootDashElementsAction {

	public DuplicateAction(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(model, selection, ui);
		this.setText("Duplicate");
		this.setToolTipText("Make a copy of this element");
	}

	@Override
	public void updateEnablement() {
		BootDashElement element = getSingleSelectedElement();
		setEnabled(element!=null &&
				element instanceof Duplicatable &&
				((Duplicatable<?>)element).canDuplicate());
	}

	@Override
	public void updateVisibility() {
		setVisible(getSingleSelectedElement() instanceof Duplicatable);
	}

	@Override
	public void run() {
		BootDashElement _e = getSingleSelectedElement();
		if (_e instanceof Duplicatable<?>) {
			Duplicatable<?> e = (Duplicatable<?>) _e;
			if (e.canDuplicate()) {
				e.duplicate(ui);
			}
		}
	}

}
