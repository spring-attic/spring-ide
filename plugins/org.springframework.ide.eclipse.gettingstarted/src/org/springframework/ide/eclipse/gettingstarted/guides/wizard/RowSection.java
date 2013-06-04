/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.guides.wizard;

import org.eclipse.swt.layout.GridLayout;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class RowSection extends GroupSection {

	private int cols;

	public RowSection(WizardPageWithSections owner,
			WizardPageSection[] _sections) {
		super(owner, null, _sections);
		this.cols = _sections.length;
	}

	
	@Override
	protected GridLayout createLayout() {
		return new GridLayout(cols, false);
	}

}
