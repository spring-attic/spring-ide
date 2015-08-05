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

import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Abstract superclass for actions that operate on a selection of a single
 * 'section' element.
 *
 * @author Kris De Volder
 */
public abstract class AbstractBootDashModelAction extends AbstractBootDashAction {

	protected final LiveExpression<BootDashModel> sectionSelection;
	private ValueListener<BootDashModel> sectionListener;

	protected AbstractBootDashModelAction(LiveExpression<BootDashModel> section, UserInteractions ui) {
		super(ui);
		this.sectionSelection = section;
		this.sectionSelection.addListener(sectionListener = new ValueListener<BootDashModel>() {
			public void gotValue(LiveExpression<BootDashModel> exp, BootDashModel value) {
				updateVisibility();
				updateEnablement();
			}
		});
	}

	public void updateEnablement() {
		this.setEnabled(sectionSelection.getValue()!=null);
	}

	public void updateVisibility() {
		this.setVisible(sectionSelection.getValue()!=null);
	}

	@Override
	public void dispose() {
		if (sectionListener!=null) {
			sectionSelection.removeListener(sectionListener);
			sectionListener = null;
		}
		super.dispose();
	}

}
