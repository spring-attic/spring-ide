/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.wizard;

import org.eclipse.jface.viewers.LabelProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public abstract class ChooseOneSection extends WizardPageSection {

	protected LabelProvider labelProvider = new LabelProvider();

	public ChooseOneSection(IPageWithSections owner) {
		super(owner);
	}

	public ChooseOneSection setLabelProvider(LabelProvider p) {
		this.labelProvider = p;
		return this;
	}

}
