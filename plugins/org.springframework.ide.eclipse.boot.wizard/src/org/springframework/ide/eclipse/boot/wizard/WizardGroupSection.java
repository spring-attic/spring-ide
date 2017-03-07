/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class WizardGroupSection extends WizardCompositeSection {

	private String groupTitle;

	/**
	 * If title is null then it creates a normal composite without a box around
	 * it. Otherwise it creates a 'group' and uses the title as label for the
	 * group.
	 */
	public WizardGroupSection(IPageWithSections owner, String title, WizardPageSection... _sections) {
		super(owner, _sections);
		this.groupTitle = title;
	}

	@Override
	protected Composite createComposite(Composite page) {
		Group group = new Group(page, SWT.NONE);
		if (groupTitle != null) {
			group.setText(groupTitle);
		}

		applyLayout(group);
		applyLayoutData(group);
		return group;
	}
}
